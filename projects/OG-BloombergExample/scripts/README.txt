
OpenGamma examples scripts
==========================
These scripts are intended for use when OpenGamma is packaged up into a distribution.

 run - initialises the example and runs the server
 init-example-db - initialises the dummy HSQLDB database
 start-jetty - starts the server
 stop-jetty - stops the server


The scripts will not work when running from Eclipse or a simple Git checkout.

The key difference is the properties file that is invoked:
      Scripts - /OG Examples/config/fullstack/bloombergexample-bin.properties
  Development - /OG Examples/config/fullstack/bloombergexample-dev.properties

For Eclipse, run the main method in ExampleComponentServerDev
For Ant, run the ant command "jetty" or "jetty-debug"
