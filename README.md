Appaloosa Client
================

Publish your mobile applications (Android, iOS, ...) to the http://www.appaloosa-store.com platform.

Embedd the Appaloosa client
---------------------------
Use this project in yours to provide your user a integration with Appaloosa.

The jars are available on the maven repo: https://github.com/joel1di1/joel1di1-mvn-repo/raw/master/releases/

For exemple, see the [appaloosa plugin](https://wiki.jenkins-ci.org/display/JENKINS/Appaloosa+Plugin) code: https://github.com/jenkinsci/appaloosa-plugin


Use the command line
---------------------------
Use the command line directly with the shaded jar (https://github.com/joel1di1/joel1di1-mvn-repo/raw/master/releases/com/appaloosa-store/appaloosa-client/1.1.0/appaloosa-client-1.1.0.jar).

Usage: appaloosa-deploy -t <store_token> /file/to/deploy [options]
Use -t instead of --token.
Deploy several file in one command.

> java -jar appaloosa-client-1.1.0-shaded --token <store_token> /file/to/deploy

> java -jar appaloosa-client-1.1.0-shaded -t <store_token> /file/to/deploy

> java -jar appaloosa-client-1.1.0-shaded -t <store_token> /file/to/deploy /another/file/to/deploy

Exemples:
> java -jar appaloosa-client-1.1.0-shaded --token er355fgfvc23 /tmp/my_app.apk

> java -jar appaloosa-client-1.1.0-shaded -t er355fgfvc23 /tmp/my_app.ipa

> java -jar appaloosa-client-1.1.0-shaded -t er355fgfvc23 /tmp/my_app.ipa /tmp/my_app.apk


Options                             
* --proxyHost                             The proxy hostname                     
* --proxyPass                             The proxy user password                
* --proxyPort [Integer]                   The proxy port                         
* --proxyUser                             The proxy username                     
* --token                                 Store token. Find it on your store's settings page.


Contributing
------------
1. Fork it [WTF?](http://help.github.com/fork-a-repo/)
2. Add your feature
3. Send a pull request [WTF?](http://help.github.com/send-pull-requests/)

