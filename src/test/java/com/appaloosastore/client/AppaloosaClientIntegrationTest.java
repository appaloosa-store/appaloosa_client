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

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class AppaloosaClientIntegrationTest {

	private static final String STORE_TOKEN = "8srd3gj8rmcfquo8g8bdacp25q456jn5";
	AppaloosaClient appaloosaClient;
	
	private AppaloosaClient constructAppaloosaClient(String proxyHost, Integer proxyPort, String proxyUsername, String proxyPassword) {
		AppaloosaClient appaloosaClient = new AppaloosaClient(STORE_TOKEN);
		appaloosaClient.setBaseUrl("http://localhost");
		appaloosaClient.setPort(3000);
		return appaloosaClient;
	}
	private AppaloosaClient constructAppaloosaClient() {
		return constructAppaloosaClient(null,  null,  null, null);
	}

	@Before
	public void setup() throws Exception{
		appaloosaClient = constructAppaloosaClient();
	}
	
	@Test
	@Ignore
	public void deployFile() throws AppaloosaDeployException{
		appaloosaClient.deployFile("/path/to/my/app-1.3.ipa", null,
				"Test Group");
	}

	@Test
	@Ignore
	public void deployFileWithProxy() throws AppaloosaDeployException{
		appaloosaClient = constructAppaloosaClient("localhost", 8888, null, null);
		appaloosaClient.deployFile("/path/to/my/app-1.3.ipa");
	}

}
