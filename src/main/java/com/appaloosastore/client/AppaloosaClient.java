/**
 *
 * The MIT License
 *
 * Copyright (c) 2013 OCTO Technology <blafontaine@octo.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.appaloosastore.client;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * Client for appaloosa, https://www.appaloosa-store.com. Usage : <br>
 * <code>
 * 	AppaloosaClient client = new AppaloosaClient("my_store_token"); <br>
 *  try {                                                              <br>
 *    client.deployFile("/path/to/archive", "optional description",    <br>
 *    					"some group | another group | also optional",  <br>
*    					"optional changelog);                          <br>
 *    System.out.println("Archive deployed");                          <br>
 *  } catch (AppaloosaDeployException e) {                             <br>
 *  	System.err.println("Something went wrong");                    <br>
 *  }                                                                  <br>
 * </code> Store token is available on settings page.
 *
 * @author Benoit Lafontaine
 */
public class AppaloosaClient {

	public static int MAX_RETRIES = 30;

	private String storeToken;

	private PrintStream logger;
	private HttpClient httpClient;
	private String appaloosaUrl = "https://www.appaloosa-store.com";
	private int appaloosaPort = 443;
	private int waitDuration = 2000;
	private String proxyHost;
	private String proxyUser;
	private String proxyPass;
	private int    proxyPort;

	public AppaloosaClient() {
		logger = System.out;
	}

	public AppaloosaClient(String storeToken) {
		this();
		setStoreToken(storeToken);
	}

	public AppaloosaClient(String storeToken, String proxyHost,
			int proxyPort, String proxyUser, String proxyPass) {
		this(storeToken);

		this.proxyHost = proxyHost;
		this.proxyUser = proxyUser;
		this.proxyPass = proxyPass;
		this.proxyPort = proxyPort;
	}

	/**
	 * {@link #deployFile(String, String, List, String)} will all params set to null.
	 * @param filePath Path to the binary
	 * @throws AppaloosaDeployException
	 * */
	public void deployFile(String filePath) throws AppaloosaDeployException {
		deployFile(filePath, null, (List<String>) null, null);
	}

	/**
	 * {@link #deployFile(String, String, String, String)} with groupNames as String.
	 * @param filePath Path to the binary
	 * @param description Text changelog for this update. Can be empty.
	 * @param groupNames
	 *            List of group names in a string format, group names should be
	 *            separated by '|'. Example: "Group 1 | Group 2"
     * @param changelog Changelog of the update
	 * @throws AppaloosaDeployException
	 * */
	public void deployFile(String filePath, String description, String groupNames, String changelog)
                throws AppaloosaDeployException {
		deployFile(filePath, description, parseGroupNames(groupNames), changelog);
	}

	List<String> parseGroupNames(String groupNames) {
		ArrayList<String> names = new ArrayList<String>();
		if (groupNames != null) {
			for (String groupName : groupNames.split("\\|")) {
				String trimedGroupName = groupName.trim();
				if (!trimedGroupName.isEmpty()) {
					names.add(trimedGroupName);
				}
			}
		}
		return names;
	}

	/**
	 * @param filePath physical path of the file to upload
	 * @param description Text description for this update. Use null if you want to use the previous description.
	 * @param groupNames List of group names that will be allowed to see and install this update.
	 * 			When null or empty, the update will be publish to previous allowed groups if a previous update exists,
	 * 			otherwise it will be published to default group "everybody".
	 * 			You can also specify to publish your file to the default group "everybody", you have to use the name "everybody" even in French.
     * @param changelog Text changelog for this update. Can be empty.
	 * @throws AppaloosaDeployException
	 *             when something went wrong
	 * */
	public void deployFile(String filePath, String description, List<String> groupNames, String changelog)
            throws AppaloosaDeployException {
		log("== Appaloosa Client V2.0");
		log("== Deploy file " + filePath + " to Appaloosa");
		log("== reseting http connection");
		resetHttpConnection();

		// Retrieve details from Appaloosa to do the upload
		log("==   Ask for upload information");
		UploadBinaryForm uploadForm = getUploadForm();

		// Upload the file on cloud provider
		log("==   Upload file " + filePath);
		uploadFile(filePath, uploadForm);

		// Notify Appaloosa that the file is available
		log("==   Start remote processing file");
		MobileApplicationUpdate update = notifyAppaloosaForFile(filePath,
				uploadForm);

		// Wait for Appaloosa to process the file
		update = waitForAppaloosaToProcessFile(update);

		// publish update
		if (update.hasError() == false) {
			log("==   Publish uploaded file");
			setUpdateParameters(update, description, groupNames, changelog);
			publish(update);
			log("== File deployed and published successfully");
		} else {
			log("== Impossible to publish file: "
					+ update.statusMessage);
			throw new AppaloosaDeployException(update.statusMessage);
		}
	}

	void setUpdateParameters(MobileApplicationUpdate update,
							 String description,
							 List<String> groupNames,
							 String changelog) {
		update.description = description;
		update.changelog = changelog;
		update.groupNames.clear();
		if (groupNames != null) {
			update.groupNames.addAll(groupNames);
		}
	}

	protected MobileApplicationUpdate waitForAppaloosaToProcessFile(
			MobileApplicationUpdate update) throws AppaloosaDeployException {
		int retries = 0;
		while (!update.isProcessed() && retries < MAX_RETRIES) {
			smallWait();
			log("==  Check that appaloosa has processed the uploaded file (extract useful information and do some verifications)");
			try{
				update = getMobileApplicationUpdateDetails(update.id);
				retries = 0;
			}catch (Exception e) {
				retries++;
			}
		}
		if (retries >= MAX_RETRIES) {
			throw new AppaloosaDeployException("Appaloosa servers seems to be down. Please retry later. Sorry for breaking your build...");
		}
		return update;
	}

	private void log(String string) {
		if (logger != null)
			logger.println(string);
	}

	protected MobileApplicationUpdate publish(MobileApplicationUpdate update)
			throws AppaloosaDeployException {
		HttpPost httpPost = new HttpPost(publishUpdateUrl());

		List<NameValuePair> parameters = constructParametersFromUpdate(update);

		try {
			return makePublishCall(httpPost, parameters);
		} catch (AppaloosaDeployException e) {
			throw e;
		} catch (Exception e) {
            log(ExceptionUtils.getStackTrace(e));
			throw new AppaloosaDeployException(
					"Error during publishing update (id=" + update.id + ")", e);
		} finally {
			resetHttpConnection();
		}
	}

	MobileApplicationUpdate makePublishCall(HttpPost httpPost,
			List<NameValuePair> parameters)
			throws UnsupportedEncodingException, IOException,
			ClientProtocolException, AppaloosaDeployException {
		httpPost.setEntity(new UrlEncodedFormEntity(parameters));
		HttpResponse response = httpClient.execute(httpPost);
		String json = readBodyResponse(response);

		return MobileApplicationUpdate.createFrom(json);
	}

	List<NameValuePair> constructParametersFromUpdate(
			MobileApplicationUpdate update) {
		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("token", storeToken));
		parameters.add(new BasicNameValuePair("id", update.id.toString()));
        if (update.description != null){
            parameters.add(new BasicNameValuePair("mobile_application_update[description]", update.description));
        }
        if (update.changelog != null){
            parameters.add(new BasicNameValuePair("mobile_application_update[changelog]", update.changelog));
        }
		for (String  groupName : update.groupNames) {
			parameters.add(new BasicNameValuePair(
					"mobile_application_update[group_names][]", groupName));
		}
		return parameters;
	}

	protected String readBodyResponse(HttpResponse response)
			throws ParseException, IOException {
		return EntityUtils.toString(response.getEntity(), "UTF-8");
	}

	protected String publishUpdateUrl() {
		return getAppaloosaBaseUrl() + "api/publish_update.json";
	}

	public MobileApplicationUpdate getMobileApplicationUpdateDetails(
			Integer id) throws AppaloosaDeployException {
		HttpGet httpGet = new HttpGet(updateUrl(id));

		HttpResponse response;
		try {
			response = httpClient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == 200) {
				String json = readBodyResponse(response);
				return MobileApplicationUpdate.createFrom(json);
			} else {
				throw createExceptionWithAppaloosaErrorResponse(response,
						"Impossible to get details for application update "
								+ id + ", cause: ");
			}
		} catch (AppaloosaDeployException e) {
			throw e;
		} catch (Exception e) {
            log(ExceptionUtils.getStackTrace(e));
			throw new AppaloosaDeployException(
					"Error while get details for update id = " + id, e);
		} finally {
			resetHttpConnection();
		}
	}

	protected AppaloosaDeployException createExceptionWithAppaloosaErrorResponse(
			HttpResponse response, String prefix) throws ParseException,
			IOException {
		int statusCode = response.getStatusLine().getStatusCode();
		String cause = "";
		switch (statusCode) {
		case 404:
			cause = "resource not found (404)";
			break;
		case 422:
			String json;
			json = readBodyResponse(response);
			try {
				AppaloosaErrors errors = AppaloosaErrors.createFromJson(json);
				cause = errors.toString();
			} catch (Exception e) {
				cause = json;
			}
			break;
		default:
			break;
		}
		return new AppaloosaDeployException(prefix + cause);
	}

	protected String updateUrl(Integer id) {
		return getAppaloosaBaseUrl() + "mobile_application_updates/" + id
				+ ".json?token=" + storeToken;
	}

	protected void smallWait() {
		try {
			Thread.sleep(waitDuration);
		} catch (InterruptedException e) {
		}
	}

	protected MobileApplicationUpdate notifyAppaloosaForFile(String filePath,
			UploadBinaryForm uploadForm) throws AppaloosaDeployException {

		HttpPost httpPost = new HttpPost(onBinaryUploadUrl());

		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
		parameters.add(new BasicNameValuePair("token", storeToken));
		String key = constructKey(uploadForm.getKey(), filePath);
		parameters.add(new BasicNameValuePair("key", key));
		parameters.add(new BasicNameValuePair("prefix", uploadForm.getPrefix()));

		try {
			return makePublishCall(httpPost, parameters);
		} catch (AppaloosaDeployException e) {
			throw e;
		} catch (Exception e) {
			throw new AppaloosaDeployException(
					"Error during appaloosa notification", e);
		} finally {
			resetHttpConnection();
		}
	}

	protected String constructKey(String key, String filePath) {
		String filename = new File(filePath).getName();
		return StringUtils.replace(key, "${filename}", filename);
	}

	protected void uploadFile(String filePath, UploadBinaryForm uploadForm)
			throws AppaloosaDeployException {
		try {
			File file = new File(filePath);
			HttpPost httppost = createHttpPost(uploadForm, file);
			HttpResponse response = httpClient.execute(httppost);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode != uploadForm.getSuccessActionStatus()) {
				String message = readErrorFormAmazon(IOUtils.toString(response
						.getEntity().getContent()));
				throw new AppaloosaDeployException("Impossible to upload file "
						+ filePath + ": " + message);
			}
		} catch (AppaloosaDeployException e) {
			throw e;
		} catch (Exception e) {
            log(ExceptionUtils.getStackTrace(e));
			throw new AppaloosaDeployException("Error while uploading "
					+ filePath + " : " + e.getMessage(), e);
		} finally {
			resetHttpConnection();
		}
	}

	protected HttpPost createHttpPost(UploadBinaryForm uploadForm, File file)
			throws UnsupportedEncodingException {
		MultipartEntity entity = new MultipartEntity();
		ContentBody cbFile = new FileBody(file);
		addParam(entity, "policy", uploadForm.getPolicy());

		addParam(entity, "success_action_status", uploadForm
				.getSuccessActionStatus().toString());
		addParam(entity, "key", uploadForm.getKey());
		addParam(entity, "Content-Type", uploadForm.getContentType());
		addParam(entity, "X-Amz-Signature", uploadForm.getXAMZSignature());
		addParam(entity, "X-Amz-Credential", uploadForm.getXAMZCredentials());
		addParam(entity, "X-Amz-Date", uploadForm.getXAMZDate());
		addParam(entity, "X-Amz-Algorithm", uploadForm.getXAMZAlgorithm());

		entity.addPart("file", cbFile);

		HttpPost httppost = new HttpPost(uploadForm.getUrl());
		httppost.setEntity(entity);
		return httppost;
	}

	protected void addParam(MultipartEntity entity, String paramName,
			String paramValue) throws UnsupportedEncodingException {
		entity.addPart(paramName, new StringBody(paramValue, "text/plain",
				Charset.forName("UTF-8")));
	}

	protected String readErrorFormAmazon(String body) {
		int start = body.indexOf("<Message>") + 9;
		int end = body.indexOf("</Message>");
		return body.substring(start, end);
	}

	protected UploadBinaryForm getUploadForm() throws AppaloosaDeployException {
		HttpGet httpGet = new HttpGet(newBinaryUrl());
		try {
			HttpResponse response = httpClient.execute(httpGet);
			int statusCode = response.getStatusLine().getStatusCode();
			switch (statusCode) {
			case 422:
				throw createExceptionWithAppaloosaErrorResponse(response, "");
			default:
				String json = IOUtils.toString(response.getEntity().getContent());
				UploadBinaryForm uploadForm = UploadBinaryForm.createFormJson(json);
				return uploadForm;
			}
		} catch (AppaloosaDeployException e) {
			throw e;
		} catch (Exception e) {
            log(ExceptionUtils.getStackTrace(e));
			throw new AppaloosaDeployException(
					"impossible to retrieve upload information from "
							+ appaloosaUrl, e);
		} finally {
			resetHttpConnection();
		}
	}

	void resetHttpConnection() {
		if (httpClient != null)
			httpClient.getConnectionManager().shutdown();
		httpClient = new DefaultHttpClient();

		if (proxyHost != null && !proxyHost.isEmpty() && proxyPort > 0) {
			Credentials cred = null;
			if (proxyUser != null && !proxyUser.isEmpty()){
				cred = new UsernamePasswordCredentials(proxyUser, proxyPass);

			((DefaultHttpClient) httpClient).getCredentialsProvider()
					.setCredentials(new AuthScope(proxyHost, proxyPort), cred);
			}
			HttpHost proxy = new HttpHost(proxyHost, proxyPort);
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
					proxy);
		}
	}

	public void useLogger(PrintStream logger) {
		this.logger = logger;
	}

	protected String onBinaryUploadUrl() {
		String url = getAppaloosaBaseUrl();
		url = url + "api/on_binary_upload";
		return url;
	}

	protected String newBinaryUrl() {
		String url = getAppaloosaBaseUrl();
		url = url + "api/upload_binary_form_signature_v4.json?token=" + storeToken;
		return url;
	}

	protected String getAppaloosaBaseUrl() {
		String url = appaloosaUrl;
		if (appaloosaPort != 443) {
			url = url + ":" + appaloosaPort;
		}
		if (!url.endsWith("/")) {
			url = url + "/";
		}
		return url;
	}

	/**
	 * To change the url of appaloosa server. Mostly for tests usage or for
	 * future evolutions.
	 *
	 * @param appaloosaUrl
	 */
	public void setBaseUrl(String appaloosaUrl) {
		this.appaloosaUrl = appaloosaUrl;
	}

	/**
	 * To change port of appaloosa server. Mostly for tests usage or for future
	 * evolutions.
	 *
	 * @param port
	 */
	public void setPort(int port) {
		appaloosaPort = port;
	}

	protected void setWaitDuration(int waitDuration) {
		this.waitDuration = waitDuration;
	}

	void setStoreToken(String storeToken) {
		this.storeToken = StringUtils.trimToNull(storeToken);
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public void setProxyUser(String proxyUser) {
		this.proxyUser = proxyUser;
	}

	public void setProxyPass(String proxyPass) {
		this.proxyPass = proxyPass;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

}
