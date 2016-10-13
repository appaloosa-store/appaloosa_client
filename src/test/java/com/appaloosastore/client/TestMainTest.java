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

import java.io.IOException;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;


public class TestMainTest {
	
	Main main;
	
	@Before
	public void setup(){
		main = new Main();
	}
	
	@Test
	public void executeShouldShowUsageWhenNoArgs() throws AppaloosaDeployException, IOException{
		main = EasyMock.createMockBuilder(Main.class).addMockedMethod("showUsage").createMock();
		EasyMock.expect(main.showUsage()).andReturn(null);
		EasyMock.replay(main);
		
		String[] args = new String[0];
		main.execute(args);
		
		EasyMock.verify(main);
	}

	@Test
	public void executeShouldShowUsageWhenOneArgs() throws AppaloosaDeployException, IOException{
		main = EasyMock.createMockBuilder(Main.class).addMockedMethod("showUsage").createMock();
		EasyMock.expect(main.showUsage()).andReturn(null);
		EasyMock.replay(main);
		
		String[] args = new String[]{"test"};
		main.execute(args);
		
		EasyMock.verify(main);
	}

	@Test
	public void executeShouldShowUsageNoFile() throws AppaloosaDeployException, IOException{
		main = EasyMock.createMockBuilder(Main.class).addMockedMethod("showUsage").createMock();
		EasyMock.expect(main.showUsage()).andReturn(null);
		EasyMock.replay(main);
		
		String[] args = new String[]{"-t", "token"};
		main.execute(args);
		
		EasyMock.verify(main);
	}


	
	@Test
	public void executeShouldCallAppaloosaClientWithParams() throws AppaloosaDeployException, IOException{
		String token = "my_token";
		String path = "/path/to/file";
		String[] args = new String[]{"-t", token, path};
		
		AppaloosaClient client = EasyMock.createMock(AppaloosaClient.class);
		client.setStoreToken(token);
		client.deployFile(path, null, (String) null, null);
		
		EasyMock.replay(client);
		main.setAppaloosaClient(client);
		
		main.execute(args);		
		
		EasyMock.verify(client);
	}

	@Test
	public void executeShouldCallAppaloosaClientWithParams2() throws AppaloosaDeployException, IOException{
		String token = "my_other_token";
		String path = "/path/to/other_file";
		String[] args = new String[]{path, "-t", token};
		
		AppaloosaClient client = EasyMock.createMock(AppaloosaClient.class);
		client.setStoreToken(token);
		client.deployFile(path, null, (String) null, null);
		
		EasyMock.replay(client);
		main.setAppaloosaClient(client);
		
		main.execute(args);		
		
		EasyMock.verify(client);
	}
	

	@Test
	public void executeShouldCallAppaloosaClientWithSeveralFiles() throws AppaloosaDeployException, IOException{
		String token = "a_token";
		String path = "/path/to/file";
		String otherPath = "/path/to/other_file";
		String[] args = new String[]{path, "-t", token, otherPath};
		
		AppaloosaClient client = EasyMock.createMock(AppaloosaClient.class);
		client.setStoreToken(token);
		client.deployFile(path, null, (String) null, null);
		client.deployFile(otherPath, null, (String) null, null);
		
		EasyMock.replay(client);
		
		main.setAppaloosaClient(client);
		main.execute(args);		
		
		EasyMock.verify(client);
	}
	
	@Test
	public void executeShouldCallAppaloosaClientWithProxySettings() throws AppaloosaDeployException, IOException{
		String token = "a_token";
		String path = "/path/to/file";
		String proxyHost = "proxyHost";
		String proxyUser = "proxyUser";
		String proxyPass = "proxyPass";
		Integer proxyPort = 324;
		String[] args = new String[]{path, "-t", token,
				"--proxyHost", proxyHost, 
				"--proxyUser", proxyUser,
				"--proxyPass", proxyPass,
				"--proxyPort", proxyPort.toString()
				};
		
		AppaloosaClient client = EasyMock.createMock(AppaloosaClient.class);
		client.setStoreToken(token);
		client.setProxyHost(proxyHost);
		client.setProxyPort(proxyPort);
		client.setProxyUser(proxyUser);
		client.setProxyPass(proxyPass);
		
		client.deployFile(path, null, (String) null, null);
		
		EasyMock.replay(client);
		
		main.setAppaloosaClient(client);
		main.execute(args);		
		
		EasyMock.verify(client);
	}
	
	@Test
	public void executeShouldCallAppaloosaClientWithDescriptionAndGroupsAndChangelog()
                throws AppaloosaDeployException, IOException{
		String token = "a_token";
		String path = "/path/to/file";
		String description = "My description";
		String changelog = "My changelog";
		String groupNames = "Group 1 | Group 2";
		String[] args = new String[]{path, "-t", token,
				"--description", description,
				"--groups", groupNames,
				"--changelog", changelog
				};
		
		AppaloosaClient client = EasyMock.createMock(AppaloosaClient.class);
		client.setStoreToken(token);
		
		client.deployFile(path, description, groupNames, changelog);
		
		EasyMock.replay(client);
		
		main.setAppaloosaClient(client);
		main.execute(args);		
		
		EasyMock.verify(client);
	}
	
}
