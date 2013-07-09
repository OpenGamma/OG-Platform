
OpenGamma examples scripts
==========================
These scripts are intended for use when OpenGamma is packaged up into a distribution.

 init-examples-simulated-db - initialises the dummy HSQLDB database
 examples-simulated debug  - starts the server in the foreground and output logs to the terminal
 examples-simulated start  - starts the server in the background (not supported in Windows)
 examples-simulated stop   - stops the server (not supported in Windows)


The scripts will not work when running from Eclipse or a simple Git checkout.

The key difference is the properties file that is invoked:
      Scripts - /config/fullstack/fullstack-examplessimulated-bin.properties
  Development - /config/fullstack/fullstack-examplessimulated-dev.properties

For Eclipse, run the main method in ExamplesSimulatedComponentServerDev
For Maven, run the maven command "mvn opengamma:server-run -DconfigFile=fullstack/fullstack-examplessimulated-dev.properties"
