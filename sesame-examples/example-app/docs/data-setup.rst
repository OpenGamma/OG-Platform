============================================================
Adding curve configuration and a sample market data snapshot
============================================================

Once you have set up the OpenGamma marketdata and fullstack servers, you can add the configuration and supporting data needed to run the example view tool.

Configuration and data
======================

Sample configuration, and supporting data is provided in the *resource/import-data* folder. This can be uploaded to your running server with the **DatabaseRestoreTool**. Locate this class from within og-integration in your IDE and run it with the following arguments:

+ -c http://your-server-ip:8080
+ -l com/opengamma/util/warn-logback.xml
+ -d /path/to/your/example-app/resources/Import-data/

Alternatively, run the **database-restore-tool** from *{OG install location}/platform/scripts* with the same arguments.

Navigate to http://your-server-ip:8080/jax/configs to see your uploaded configurations

Loading time-series
===================

The timeseries needed top run the example are included in the import-data. When you wish to load more you can go to http://your-server-ip:8080/jax/timeseries to add time-series:

+ Select **BLOOMBERG_TICKER** for the *Scheme type*
+ Add **DEFAULT** for *Data provider*
+ Add **PX_LAST** for *Data field*
+ Add **US0003M Index** for *Identifiers*

This step can be repeated for any ticker you wish to add

Bulk loading time-series
========================

The following command will update all the time series for all the securities listed in the system.

Command::

  bloomberg-hts-master-updater-tool.bat -c http://localhost:8080

Substituting .bat for .sh and localhost as necessary

