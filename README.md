Appaloosa Client
================

Publish your mobile applications (Android, iOS, ...) to the https://www.appaloosa-store.com platform.

Embedd the Appaloosa client
---------------------------
Use this project in yours to provide your user a integration with Appaloosa.

The jars are available on the maven repo: https://mvnrepository.com/artifact/com.appaloosa-store/appaloosa-client/

For exemple, see the [appaloosa plugin](https://wiki.jenkins-ci.org/display/JENKINS/Appaloosa+Plugin) code: https://github.com/jenkinsci/appaloosa-plugin


Use the command line
---------------------------
-Use the command line directly with the **shaded** jar downloaded from http://central.maven.org/maven2/com/appaloosa-store/appaloosa-client. For example https://oss.sonatype.org/service/local/repositories/releases/content/com/appaloosa-store/appaloosa-client/1.12/appaloosa-client-1.12-shaded.jar)

Usage: appaloosa-deploy -t <store_token> /file/to/deploy [options]
Use -t instead of --token.
Deploy several file in one command.

> java -jar appaloosa-client --token <store_token> /file/to/deploy

> java -jar appaloosa-client -t <store_token> /file/to/deploy

> java -jar appaloosa-client -t <store_token> /file/to/deploy /another/file/to/deploy

Exemples:
> java -jar appaloosa-client --token er355fgfvc23 /tmp/my_app.apk

> java -jar appaloosa-client -t er355fgfvc23 /tmp/my_app.ipa

> java -jar appaloosa-client -t er355fgfvc23 /tmp/my_app.ipa /tmp/my_app.apk

> java -jar appaloosa-client --description 'Brand new version' --groups 'Group 1 | Group 3' -t er355fgfvc23 /tmp/my_app.ipa

> java -jar appaloosa-client --description 'Brand new version' --groups 'Group 1 | Group 3' -t er355fgfvc23 --changelog 'remove deprecated UISearchDisplayController' /tmp/my_app.ipa

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
1. [Fork it](http://help.github.com/fork-a-repo/)
2. Add your feature
3. Send a [pull request](http://help.github.com/send-pull-requests/)

