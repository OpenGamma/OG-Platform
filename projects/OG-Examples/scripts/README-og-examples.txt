
OpenGamma examples scripts
==========================
These scripts are intended for use when OpenGamma is packaged up into a distribution.

 init-example-db - initialises the dummy HSQLDB database
 examples debug  - starts the server in the foreground and output logs to the terminal
 examples start  - starts the server in the background (not supported in Windows)
 examples stop   - stops the server (not supported in Windows)


The scripts will not work when running from Eclipse or a simple Git checkout.

The key difference is the properties file that is invoked:
      Scripts - /OG Examples/config/fullstack/example-bin.properties
  Development - /OG Examples/config/fullstack/example-dev.properties

For Eclipse, run the main method in ExampleComponentServerDev
For Ant, run the ant command "jetty" or "jetty-debug"
