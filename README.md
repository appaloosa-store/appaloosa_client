Publish your mobile applications (Android, iOS, ...) to the http://www.appaloosa-store.com platform.

You will find the jars in the github repo: https://github.com/joel1di1/joel1di1-mvn-repo/raw/master/releases/

Use this project in yours to provide your user a integration with Appaloosa.
Ex: https://github.com/joel1di1/joel1di1-mvn-repo/raw/master/releases/

Or use the command line directly with the shaded jar (https://github.com/joel1di1/joel1di1-mvn-repo/raw/master/releases/com/appaloosa-store/appaloosa-client/1.1.0/appaloosa-client-1.1.0.jar).

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

Option                                  Description                            
------                                  -----------                            
--proxyHost                             The proxy hostname                     
--proxyPass                             The proxy user password                
--proxyPort [Integer]                   The proxy port                         
--proxyUser                             The proxy username                     
--token                                 Store token. Find it on your store's settings page.