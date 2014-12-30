======================================
Running the example remote view client
======================================

Once you have set up the OpenGamma marketdata and fullstack servers, and added the configuration and supporting snapshot/time-series data, you are ready to run the example remote view client.

Running in the IDE
==================

The **ExampleRemoteClientTool** is an example of a client application that can be run against a remote server. This tool is set up to output Present Value and Bucketed PV01 into the console for a number of example interest rate swaps defined in **RemoteViewUtils**

You can run this in your IDE **ExampleRemoteClientTool** with the following example arguments:

+ -c http://your-server-ip:8080
+ -l com/opengamma/util/warn-logback.xml 
+ -d 20140122 
+ -ef "USD-GBP-FF-1"
+ -s DbSnp~1000
+ -ld Bloomberg

The **-d** date option accepts the format *YYYYMMDD*

The **-ef** option refers to the name of the exposure function from your uploaded configuration. Visit http://devsvr-lx-3:8080/jax/configs?type=ExposureFunctions&name= for a full list of exposure functions.

The **-s** and **-ld** options refers to the market data source. -s is the ID of an existing snapshot. -ld is the name of the live data provider. These arguments are optional and default to Bloomberg live data if no snapshot or live data is specified.

Available snapshots can be seen here http://your-server-ip:8080/jax/snapshots

Running in the command line
===========================

It is also possible to take the compiled jar of OG-Solutions and place it in *{OG install location}/lib/* then from within the root of the OG installation run the following::

      java -cp lib/*:platform/lib/* com.opengamma.solutions.ExampleRemoteClientTool -l com/opengamma/util/warn-logback.xml -c http://your-server-ip:8080 -d 20140122 -ef "USD-GBP-FF-1" -ld Bloomberg
