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

> java -jar appaloosa-client-1.1.3-shaded --token <store_token> /file/to/deploy

> java -jar appaloosa-client-1.1.3-shaded -t <store_token> /file/to/deploy

> java -jar appaloosa-client-1.1.3-shaded -t <store_token> /file/to/deploy /another/file/to/deploy

Exemples:
> java -jar appaloosa-client-1.1.3-shaded --token er355fgfvc23 /tmp/my_app.apk

> java -jar appaloosa-client-1.1.3-shaded -t er355fgfvc23 /tmp/my_app.ipa

> java -jar appaloosa-client-1.1.3-shaded -t er355fgfvc23 /tmp/my_app.ipa /tmp/my_app.apk

> java -jar appaloosa-client-1.1.3-shaded --description 'Brand new version' --groups 'Group 1 | Group 3' -t er355fgfvc23 /tmp/my_app.ipa

> java -jar appaloosa-client-1.1.3-shaded --description 'Brand new version' --groups 'Group 1 | Group 3' -t er355fgfvc23 --changelog 'remove deprecated UISearchDisplayController' /tmp/my_app.ipa

Options                             
* --proxyHost                             The proxy hostname                     
* --proxyPass                             The proxy user password                
* --proxyPort [Integer]                   The proxy port                         
* --proxyUser                             The proxy username                     
* --token                                 Store token. Find it on your store's settings page.
* --description 													Text description for this update. When not specified, the previous update description will be used.
* --groupNames 														List of group names that will be allowed to see and install this update. When null or empty, the update will be publish to previous allowed groups if a previous update exists, otherwise it will be published to default group "everybody". You can also specify to publish your file to the default group "everybody", you have to use the name "everybody" even in French.
* --changelog 													    Text changelog for this update. Optional.


Contributing
------------
1. Fork it [WTF?](http://help.github.com/fork-a-repo/)
2. Add your feature
3. Send a pull request [WTF?](http://help.github.com/send-pull-requests/)

