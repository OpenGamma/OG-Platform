============================================================
Adding curve configuration and a sample market data snapshot
============================================================

Once you have set up the OpenGamma marketdata and fullstack servers, you can add the configuration and supporting data needed to run the example view tool.

Curve configuration
===================

Sample configuration is provided in the *resource/configuration* folder. Create a zip of the configuration folder and then upload to your running server with the **SingleConfigImportTool**. Locate this class from within og-integration in your IDE and run it with the following arguments:

+ -c http://your-server-ip:8080
+ -l com/opengamma/util/warn-logback.xml 
+ -load /path/to/your/configuration.zip

Alternatively, run the **single-config-import-tool** from *{OG install location}/platform/scripts* with the same arguments. 

Navigate to http://your-server-ip:8080/jax/configs to see your uploaded configurations

Loading a snapshot 
==================

A sample market data snapshot is provided in the *resource/snapshot* folder. Locate the **MarketDataSnapshotImportTool** class from within og-integration in your IDE and run it with the following arguments:

+ -c http://your-server-ip:8080
+ -f path/to/your/resources/snapshot/marketdata_snapshot_import.xls

Alternatively, run the **market-data-snapshot-import-tool** from *{OG install location}/platform/scripts* with the same arguments.

Navigate to http://your-server-ip:8080/jax/snapshots to see your uploaded snapshot

Loading time-series
===================

Next go to http://your-server-ip:8080/jax/timeseries to add time-series:

+ Select **BLOOMBERG_TICKER** for the *Scheme type*
+ Add **DEFAULT** for *Data provider*
+ Add **PX_LAST** for *Data field*
+ Add **US0003M Index** for *Identifiers*

Repeat this for US0006M Index

This step can be repeated for any ticker you wish to add

Bulk loading time-series
========================

The following command will update all the time series for all the securities listed in the system.

Command::

  bloomberg-hts-master-updater-tool.bat -c http://localhost:8080

Substituting .bat for .sh and localhost as necessary
