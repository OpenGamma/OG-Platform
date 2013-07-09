
OpenGamma examples scripts
==========================
These scripts are intended for use when OpenGamma is packaged up into a distribution.

 init-examples-bloomberg-db - initialises the dummy HSQLDB database
 examples-bloomberg debug  - starts the server in the foreground and output logs to the terminal
 examples-bloomberg start  - starts the server in the background (not supported in Windows)
 examples-bloomberg stop   - stops the server (not supported in Windows)


The scripts will not work when running from Eclipse or a simple Git checkout.

The key difference is the properties file that is invoked:
      Scripts - /config/fullstack/fullstack-examplesbloomberg-bin.properties
  Development - /config/fullstack/fullstack-examplesbloomberg-dev.properties

For Eclipse, run the main method in ExamplesBloombergComponentServerDev
For Maven, run the maven command "mvn opengamma:server-run -DconfigFile=fullstack/fullstack-examplesbloomberg-dev.properties"
