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

import static com.harlap.test.http.MockHttpServer.Method.GET;
import static com.harlap.test.http.MockHttpServer.Method.POST;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.harlap.test.http.MockHttpServer;

public class AppaloosaClientTest {

	private static final int PORT = 45678;
	private static final String BASE_URL = "http://localhost";
	private static String ORGANISATION_TOKEN = "FAKETOKEN";
	AppaloosaClient appaloosaClient;

	MockHttpServer server;

	private String sampleBinaryFormResponse =
		"{\"policy\":\"eyJleH=\"," +
			"\"success_action_status\":200," +
			"\"Content-Type\":\"\"," +
			"\"url\": \"" + BASE_URL + ":" + PORT + "/\"," +
			"\"x-amz-signature\":\"LL/ZXXNCl+0NtI8=\"," +
			"\"x-amz-credential\":\"JTJYYJ\"," +
			"\"x-amz-algorithm\":\"AWS4-HMAC-SHA256\"," +
			"\"x-amz-date\":\"20230404T161626Z\"," +
			"\"prefix\":\"5/uploads\"," +
			"\"key\":\"5/uploads/${filename}\"}";

	private String sampleOnBinaryUploadResponse = "{\"id\":590,\"activation_date\":null, \"other\":\"test\"}";

	@Before
	public void setup() throws Exception {
		appaloosaClient = new AppaloosaClient(ORGANISATION_TOKEN);
		appaloosaClient.setBaseUrl(BASE_URL);
		appaloosaClient.setPort(PORT);
		appaloosaClient.setWaitDuration(10);
		appaloosaClient.resetHttpConnection();

		server = new MockHttpServer(PORT);
		server.start();
	}

	@After
	public void tearDown() throws Exception {
		server.stop();
	}

	@Test
	public void appaloosaClientShouldTrimToken()
			throws AppaloosaDeployException {
		appaloosaClient.setStoreToken("   " + ORGANISATION_TOKEN + " \t ");

		String url = "/api/upload_binary_form_signature_v4.json?token=" + ORGANISATION_TOKEN;
		server.expect(GET, url)
				.respondWith(200, null, sampleBinaryFormResponse);

		appaloosaClient.getUploadForm();
	}

	@Test
	public void deployShouldUseDescription() throws AppaloosaDeployException {
		server.expect(GET,
				"/api/upload_binary_form_signature_v4.json?token=" + ORGANISATION_TOKEN)
				.respondWith(200, null, sampleBinaryFormResponse);

		server.expect(POST, "/").respondWith(200, null, "");

		server.expect(POST, "/api/on_binary_upload").respondWith(200, null,
				sampleOnBinaryUploadResponse);

		server.expect(
				GET,
				"/mobile_application_updates/590.json?token="
						+ ORGANISATION_TOKEN)
				.respondWith(200, null,
						"{\"id\":590, \"status\":1,\"application_id\":\"com.appaloosa.sampleapp\"}");

		server.expect(POST, "/api/publish_update.json")
				.respondWith(200, null,
						"{\"id\":590, \"status\":4,\"application_id\":\"com.appaloosa.sampleapp\"}");

		appaloosaClient.deployFile(getTestFile("fake.ipa"));

		server.verify();
	}

	
	@Test
	public void getUploadFormShouldCallAppaloosaAndReturnsObject()
			throws AppaloosaDeployException {
		String url = "/api/upload_binary_form_signature_v4.json?token=" + ORGANISATION_TOKEN;
		server.expect(GET, url)
				.respondWith(200, null, sampleBinaryFormResponse);

		UploadBinaryForm uploadForm = appaloosaClient.getUploadForm();

		server.verify();
		assertEquals("eyJleH=", uploadForm.getPolicy());
		assertTrue(200 == uploadForm.getSuccessActionStatus());
		assertEquals("", uploadForm.getContentType());
		assertEquals("LL/ZXXNCl+0NtI8=", uploadForm.getXAMZSignature());
		assertEquals("20230404T161626Z", uploadForm.getXAMZDate());
		assertEquals("JTJYYJ", uploadForm.getXAMZCredentials());
		assertEquals("AWS4-HMAC-SHA256", uploadForm.getXAMZAlgorithm());
		assertEquals(BASE_URL + ":" + PORT + "/", uploadForm.getUrl());
		assertEquals("5/uploads/${filename}", uploadForm.getKey());
		assertEquals("5/uploads", uploadForm.getPrefix());
	}

	@Test(expected = AppaloosaDeployException.class)
	public void getUploadFormShouldDisplayErrorFormServer()
			throws AppaloosaDeployException {
		String url = "/api/upload_binary_form_signature_v4.json?token=" + ORGANISATION_TOKEN;
		server.expect(GET, url).respondWith(422, null,
				"{\"errors\":[\"invalid token\"]}");

		appaloosaClient.getUploadForm();

		server.verify();
	}

	@Test
	public void shouldHandleAmazonError() {
		UploadBinaryForm uploadForm = createFakeUploadForm();

		server.expect(POST, "/")
				.respondWith(
						400,
						null,
						"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<Error><Code>InvalidArgument</Code><Message>Bucket POST must contain a field named 'policy'.  If it is specified, please check the order of the fields.</Message><ArgumentValue></ArgumentValue><ArgumentName>policy</ArgumentName><RequestId>558E6EDC9C16FB53</RequestId><HostId>5oA4Tr7bZoZN6W5eM9f</HostId></Error>");

		try {
			appaloosaClient.uploadFile(getTestFile("fake.ipa"), uploadForm);
			fail("Should send exception on amazon");
		} catch (AppaloosaDeployException e) {
			assertEquals(
					"Impossible to upload file src/test/resources/fake.ipa: Bucket POST must contain a field named 'policy'.  If it is specified, please check the order of the fields.",
					e.getMessage());
		}
	}

	private UploadBinaryForm createFakeUploadForm() {
		UploadBinaryForm form = new UploadBinaryForm();
		form.setContentType("");
		form.setKey("56/uploads/${filename}");
		form.setPolicy("asdfghjk");
		form.setXAMZSignature("ssssss");
		form.setXAMZDate("efzfeez");
		form.setXAMZCredentials("tgegre");
		form.setXAMZAlgorithm("cwcds");
		form.setSuccessActionStatus(200);
		form.setUrl(BASE_URL + ":" + PORT);
		return form;
	}

	@Test
	public void notifyAppaloosaForFileShouldCallAppaloosaServer()
			throws AppaloosaDeployException {
		UploadBinaryForm uploadForm = new UploadBinaryForm();
		uploadForm.setKey("54/uploads/{filename}");

		String url = "/api/on_binary_upload";
		server.expect(POST, url).respondWith(200, null,
				"{\"id\":590,\"activation_date\":null, \"other\":\"test\"}");

		MobileApplicationUpdate update = appaloosaClient
				.notifyAppaloosaForFile(getTestFile("fake.ipa"), uploadForm);

		assertTrue(590 == update.id);

		server.verify();
	}

	@Test
	public void constructKeyTest() {
		assertEquals("54/uploads/test.ipa", appaloosaClient.constructKey(
				"54/uploads/${filename}", "/tmp/test.ipa"));
		assertEquals("/54/uploads/test.ipa", appaloosaClient.constructKey(
				"/54/uploads/${filename}", "/tmp/test.ipa"));
		assertEquals("elsewhere/youpi.apk", appaloosaClient.constructKey(
				"elsewhere/${filename}", "/tmp/other/youpi.apk"));
	}

	@Test
	public void getMobileApplicationUpdateDetailsShouldAskAppaloosa()
			throws AppaloosaDeployException {
		String url = "/mobile_application_updates/772.json?token="
				+ ORGANISATION_TOKEN;
		server.expect(GET, url)
				.respondWith(
						200,
						null,
						"{\"id\":772,\"activation_date\":null, \"status\":1,\"application_id\":\"com.appaloosa.sampleapp\", \"other\":\"test\"}");

		MobileApplicationUpdate update = appaloosaClient
				.getMobileApplicationUpdateDetails(772);

		assertNotNull(update);

		server.verify();
	}

	@Test
	public void getMobileApplicationUpdateDetailsShouldThrowExceptionWhen404() {
		server.expect(
				GET,
				"/mobile_application_updates/345.json?token="
						+ ORGANISATION_TOKEN).respondWith(404, null, null);

		try {
			appaloosaClient.getMobileApplicationUpdateDetails(345);
			fail();
		} catch (AppaloosaDeployException e) {
			assertEquals(
					"Impossible to get details for application update 345, cause: resource not found (404)",
					e.getMessage());
		}
	}

	@Test
	public void getMobileApplicationUpdateDetailsShouldThrowExceptionWhenStatusIsError()
			throws AppaloosaDeployException {
		server.expect(
				GET,
				"/mobile_application_updates/345.json?token="
						+ ORGANISATION_TOKEN)
				.respondWith(200, null,
						"{\"id\":345, \"status\":6, \"status_message\":\"test message\"}");

		MobileApplicationUpdate update = appaloosaClient
				.getMobileApplicationUpdateDetails(345);
		assertTrue(6 == update.status);
		assertEquals("test message", update.statusMessage);
	}

	String getTestFile(String filename) {
		return "src/test/resources/" + filename;
	}

	@Test
	public void createExceptionWithAppaloosaErrorResponseShouldHanlde422Errors()
			throws ParseException, IOException {

		BasicHttpResponse response = new BasicHttpResponse(new ProtocolVersion(
				"HTTP", 1, 1), 422, null);
		response.setEntity(new StringEntity(
				"{\"errors\": [\"invalid token\"]}", Charset.forName("UTF-8")));

		AppaloosaDeployException e = appaloosaClient
				.createExceptionWithAppaloosaErrorResponse(response, "PREFIX ");

		assertEquals("PREFIX invalid token", e.getMessage());
	}

	@Test
	public void waitForAppaloosaToProcessFileShouldRetryOnError()
			throws AppaloosaDeployException {
		MobileApplicationUpdate update = new MobileApplicationUpdate();
		update.id = 1;

		AppaloosaClient mockedAppaloosaClient = EasyMock
				.createMockBuilder(AppaloosaClient.class)
				.addMockedMethod("getMobileApplicationUpdateDetails",
						Integer.class).addMockedMethod("smallWait")
				.createMock();
		mockedAppaloosaClient.setStoreToken(ORGANISATION_TOKEN);

		MobileApplicationUpdate returnedUpdate = new MobileApplicationUpdate();
		returnedUpdate.id = 1;
		returnedUpdate.status = 1;
		returnedUpdate.applicationId = "com.appaloosa-store.test";

		mockedAppaloosaClient.smallWait();
		mockedAppaloosaClient.smallWait();
		mockedAppaloosaClient.smallWait();
		expect(
				mockedAppaloosaClient.getMobileApplicationUpdateDetails(1))
				.andThrow(new RuntimeException("error test")).times(2);
		expect(
				mockedAppaloosaClient.getMobileApplicationUpdateDetails(1))
				.andReturn(returnedUpdate);

		replay(mockedAppaloosaClient);

		update = mockedAppaloosaClient.waitForAppaloosaToProcessFile(update);

		verify(mockedAppaloosaClient);
		assertTrue(update.isProcessed());
	}

	@Test
	public void waitForAppaloosaToProcessFileShouldRetryMaxTimes()
			throws AppaloosaDeployException {
		MobileApplicationUpdate update = new MobileApplicationUpdate();
		update.id = 1;

		AppaloosaClient mockedAppaloosaClient = EasyMock
				.createMockBuilder(AppaloosaClient.class)
				.withConstructor(ORGANISATION_TOKEN)
				.addMockedMethod("getMobileApplicationUpdateDetails",
						Integer.class).addMockedMethod("smallWait")
				.createMock();

		for (int i = 0; i < AppaloosaClient.MAX_RETRIES; i++) {
			mockedAppaloosaClient.smallWait();
		}

		expect(
				mockedAppaloosaClient.getMobileApplicationUpdateDetails(1))
				.andThrow(new RuntimeException("error test"))
				.times(AppaloosaClient.MAX_RETRIES);

		replay(mockedAppaloosaClient);

		try {
			mockedAppaloosaClient.waitForAppaloosaToProcessFile(update);
			fail();
		} catch (Exception e) {
			assertEquals(AppaloosaDeployException.class, e.getClass());
		}
	}

	@Test
	public void waitForAppaloosaToProcessFileShouldResetRetriesCountWhenAppaloosaRespond()
			throws AppaloosaDeployException {
		MobileApplicationUpdate update = new MobileApplicationUpdate();
		update.id = 1;

		AppaloosaClient mockedAppaloosaClient = EasyMock
				.createMockBuilder(AppaloosaClient.class)
				.addMockedMethod("getMobileApplicationUpdateDetails",
						Integer.class).addMockedMethod("smallWait")
				.createMock();
		mockedAppaloosaClient.setStoreToken(ORGANISATION_TOKEN);

		MobileApplicationUpdate returnedUpdate = new MobileApplicationUpdate();
		returnedUpdate.id = 1;
		returnedUpdate.status = 1;
		returnedUpdate.applicationId = "com.appaloosa-store.test";

		for (int i = 0; i < AppaloosaClient.MAX_RETRIES * 2; i++) {
			mockedAppaloosaClient.smallWait();
		}

		expect(
				mockedAppaloosaClient.getMobileApplicationUpdateDetails(1))
				.andThrow(new RuntimeException("error test"))
				.times(AppaloosaClient.MAX_RETRIES - 1);
		expect(
				mockedAppaloosaClient.getMobileApplicationUpdateDetails(1))
				.andReturn(update);
		expect(
				mockedAppaloosaClient.getMobileApplicationUpdateDetails(1))
				.andThrow(new RuntimeException("error test"))
				.times(AppaloosaClient.MAX_RETRIES - 1);
		expect(
				mockedAppaloosaClient.getMobileApplicationUpdateDetails(1))
				.andReturn(returnedUpdate);

		replay(mockedAppaloosaClient);

		update = mockedAppaloosaClient.waitForAppaloosaToProcessFile(update);

		verify(mockedAppaloosaClient);
		assertTrue(update.isProcessed());
	}

	@Test
	public void publishShouldCallConstructParametersFromUpdate()
			throws AppaloosaDeployException, UnsupportedEncodingException, ClientProtocolException, IOException {
		MobileApplicationUpdate update = new MobileApplicationUpdate();
		update.id = 1;

		server.expect(POST, "/api/publish_update.json")
				.respondWith(200, null,
						"{\"id\":1, \"status\":4,\"application_id\":\"com.appaloosa.sampleapp\"}");
		
		AppaloosaClient mockedAppaloosaClient = EasyMock
				.createMockBuilder(AppaloosaClient.class)
				.addMockedMethod("constructParametersFromUpdate",
						MobileApplicationUpdate.class)
				.addMockedMethod("makePublishCall",
						HttpPost.class,
						List.class)
				.createMock();
		
		mockedAppaloosaClient.setStoreToken(ORGANISATION_TOKEN);
		mockedAppaloosaClient.resetHttpConnection();

		expect(mockedAppaloosaClient.constructParametersFromUpdate(update))
				.andReturn(new ArrayList<NameValuePair>());
		expect(
				mockedAppaloosaClient.makePublishCall(
						anyObject(HttpPost.class), anyObject(List.class)))
				.andReturn(null);
		replay(mockedAppaloosaClient);
		
		mockedAppaloosaClient.publish(update);
		
		verify(mockedAppaloosaClient);
	}

	@Test
	public void constructParametersFromUpdateShouldUseDescriptionIfSpecified()
			throws AppaloosaDeployException, UnsupportedEncodingException, ClientProtocolException, IOException {
		MobileApplicationUpdate update = new MobileApplicationUpdate();
		update.id = 1;

		update.description = null;
		List<NameValuePair> params = appaloosaClient.constructParametersFromUpdate(update);
		assertEquals(2, params.size());

		update.description = "";
		params = appaloosaClient.constructParametersFromUpdate(update);
		assertEquals(3, params.size());
		assertTrue(params.contains(new BasicNameValuePair("mobile_application_update[description]", update.description)));
	}

	@Test
	public void constructParametersFromUpdateShouldUseChangelogIfSpecified()
			throws AppaloosaDeployException, UnsupportedEncodingException, ClientProtocolException, IOException {
		MobileApplicationUpdate update = new MobileApplicationUpdate();
		update.id = 1;

		update.changelog = null;
		List<NameValuePair> params = appaloosaClient.constructParametersFromUpdate(update);
		assertEquals(2, params.size());

		update.changelog = "my cool changelog";
		params = appaloosaClient.constructParametersFromUpdate(update);
		assertEquals(3, params.size());
		assertTrue(params.contains(new BasicNameValuePair("mobile_application_update[changelog]", update.changelog)));
	}

	@Test
	public void constructParametersFromUpdateShouldUseGroupsIfSpecified()
			throws AppaloosaDeployException, UnsupportedEncodingException, ClientProtocolException, IOException {
		MobileApplicationUpdate update = new MobileApplicationUpdate();
		update.id = 1;
		
		update.groupNames = new ArrayList<String>();
		List<NameValuePair> params = appaloosaClient.constructParametersFromUpdate(update);
		assertEquals(2, params.size());

		update.groupNames.add("1er group");
		update.groupNames.add("2eme group");
		params = appaloosaClient.constructParametersFromUpdate(update);
		assertEquals(4, params.size());
		for (String groupName : update.groupNames) {
			assertTrue(params.contains(new BasicNameValuePair(
					"mobile_application_update[group_names][]", groupName)));
		}
	}

	@Test
	public void setUpdateParametersShouldUpdateDescriptionAndGroupNamesAndChangelog(){
		MobileApplicationUpdate update = new MobileApplicationUpdate();
		String description = "New Desc";
		String changelog = "A log of the changes";
		List<String> groupNames = new ArrayList<String>();
		groupNames.add("Group1");
		groupNames.add("Group2");

		appaloosaClient.setUpdateParameters(update, description, groupNames, changelog);

		assertEquals(description, update.description);
		assertEquals(2, update.groupNames.size());
		assertTrue(update.groupNames.contains("Group1"));
		assertTrue(update.groupNames.contains("Group2"));
		assertEquals(changelog, update.changelog);
	}
	
	@Test
	public void parseGroupNames(){
		assertParseGroupNames(new String[0], "");
		assertParseGroupNames(new String[] { "Group1" }, "Group1");
		assertParseGroupNames(new String[] { "Group with a long name" }, "Group with a long name");
		assertParseGroupNames(new String[] { "Group 1", "Group 2", "Group 3" },
				" \tGroup 1\t | Group 2 | Group 3");
	}

	private void assertParseGroupNames(String[] expected,
			String groupNamesToParse) {
		Assert.assertArrayEquals(expected,
				appaloosaClient.parseGroupNames(groupNamesToParse).toArray());
	}
}
