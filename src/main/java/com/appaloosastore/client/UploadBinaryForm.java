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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;

@JsonIgnoreProperties(ignoreUnknown=true)
public class UploadBinaryForm {
	private static ObjectMapper jsonMapper = new ObjectMapper();
	@JsonProperty(value="success_action_status")
	private Integer successActionStatus;
	@JsonProperty(value="Content-Type")
	private String contentType;
	@JsonProperty(value="x-amz-date")
	private String XAMZDate;
	@JsonProperty(value="x-amz-credential")
	private String XAMZCredentials;
	@JsonProperty(value="x-amz-algorithm")
	private String XAMZAlgorithm;
	@JsonProperty(value="x-amz-signature")
	private String XAMZSignature;
	private String prefix;
	private String url;
	private String key;
	private String policy;
	
	public static UploadBinaryForm createFormJson(String json) throws AppaloosaDeployException {
		try {
			return jsonMapper.readValue(json, UploadBinaryForm.class);
		} catch (Exception e) {
			throw new AppaloosaDeployException("Impossible to read response form appaloosa", e);
		}
	}

	public String getPolicy() {
		return policy;
	}

	public void setPolicy(String policy) {
		this.policy = policy;
	}
	

	public Integer getSuccessActionStatus() {
		return successActionStatus;
	}

	public void setSuccessActionStatus(Integer successActionStatus) {
		this.successActionStatus = successActionStatus;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
		}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getXAMZDate() {
		return XAMZDate;
	}

	public void setXAMZDate(String XAMZDate) {
		this.XAMZDate = XAMZDate;
	}

	public String getXAMZCredentials() {
		return XAMZCredentials;
	}

	public void setXAMZCredentials(String XAMZCredentials) {
		this.XAMZCredentials = XAMZCredentials;
	}

	public String getXAMZAlgorithm() {
		return XAMZAlgorithm;
	}

	public void setXAMZAlgorithm(String XAMZAlgorithm) {
		this.XAMZAlgorithm = XAMZAlgorithm;
	}

	public String getXAMZSignature() {
		return XAMZSignature;
	}

	public void setXAMZSignature(String XAMZSignature) {
		this.XAMZSignature = XAMZSignature;
	}

	public String getPrefix() {
		return prefix;
	}
}
