
OpenGamma examples scripts
==========================
These scripts are intended for use when OpenGamma is packaged up into a distribution.

 init-og-bloombergexample-db - initialises the dummy HSQLDB database
 og-bloombergexample debug  - starts the server in the foreground and output logs to the terminal
 og-bloombergexample start  - starts the server in the background (not supported in Windows)
 og-bloombergexample stop   - stops the server (not supported in Windows)


The scripts will not work when running from Eclipse or a simple Git checkout.

The key difference is the properties file that is invoked:
      Scripts - /OG Examples/config/fullstack/fullstack-bloombergexample-bin.properties
  Development - /OG Examples/config/fullstack/fullstack-bloombergexample-dev.properties

For Eclipse, run the main method in BloombergExampleComponentServerDev
For Maven, run the maven command "mvn opengamma:server-run -DconfigFile=fullstack/fullstack-bloombergexample-dev.properties"
