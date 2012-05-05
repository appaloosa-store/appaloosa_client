/**
 *
 * The MIT License
 *
 * Copyright (c) 2012 OCTO Technology <blafontaine@octo.com>
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Main {

	AppaloosaClient client = new AppaloosaClient();

	static OptionParser parser = new OptionParser() {
		{
			accepts("token",
					"Store token. Find it on your store's settings page.")
					.withRequiredArg().ofType(String.class);
			accepts("proxyHost", "The proxy hostname").withOptionalArg()
					.ofType(String.class);
			accepts("proxyPort", "The proxy port").withOptionalArg().ofType(
					Integer.class);
			accepts("proxyUser", "The proxy username").withOptionalArg()
					.ofType(String.class);
			accepts("proxyPass", "The proxy user password").withOptionalArg()
					.ofType(String.class);
		}
	};

	OutputStream out = System.out;

	/**
	 * @param args
	 * @throws AppaloosaDeployException
	 * @throws IOException
	 */
	public static void main(String[] args) throws AppaloosaDeployException,
			IOException {
		Main main = new Main();
		main.execute(args);
	}

	protected void execute(String[] args) throws AppaloosaDeployException,
			IOException {
		OptionSet options = parser.parse(args);
		List<String> filenames = options.nonOptionArguments();

		if (options.has("token") && !filenames.isEmpty()) {
			client.setOrganisationToken(options.valueOf("token").toString());
			if (options.has("proxyHost")) client.setProxyHost((String) options.valueOf("proxyHost"));
			if (options.has("proxyUser")) client.setProxyUser((String) options.valueOf("proxyUser"));
			if (options.has("proxyPass")) client.setProxyPass((String) options.valueOf("proxyPass"));
			if (options.has("proxyPort")) client.setProxyPort((Integer) options.valueOf("proxyPort"));
			for (String filename : filenames) {
				client.deployFile(filename);
			}
		} else {
			showUsage();
		}
	}

	protected Object showUsage() throws IOException {
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out));
		w.write("Usage: appaloosa-deploy -t <store_token> /file/to/deploy [options]\n");
		w.write("Use -t instead of --token.\n");
		w.write("Deploy several file in one command.\n\n");

		w.write("> java -jar appaloosa-client-1.0.1 --token <store_token> /file/to/deploy\n");
		w.write("> java -jar appaloosa-client-1.0.1 -t <store_token> /file/to/deploy\n");
		w.write("> java -jar appaloosa-client-1.0.1 -t <store_token> /file/to/deploy /another/file/to/deploy\n\n");

		w.write("Exemples:\n");
		w.write("> java -jar appaloosa-client-1.0.1 --token er355fgfvc23 /tmp/my_app.apk\n");
		w.write("> java -jar appaloosa-client-1.0.1 -t er355fgfvc23 /tmp/my_app.ipa\n");
		w.write("> java -jar appaloosa-client-1.0.1 -t er355fgfvc23 /tmp/my_app.ipa /tmp/my_app.apk\n\n");

		w.flush();

		parser.printHelpOn(out);
		return null;
	}

	protected void setAppaloosaClient(AppaloosaClient appaloosaClient) {
		client = appaloosaClient;
	}

}
