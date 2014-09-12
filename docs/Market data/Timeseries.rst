Time series
===========

Historical time series data are used in OpenGamma to store a variety of information, including:

  * Price series of financial instruments (e.g. a series of close prices for an instrument)
  * Series of index values, e.g. published Libor fixing rates

In OpenGamma time series are stored and retrieved using the ``HistoricalTimeSeriesMaster`` & ``HistoricalTimeSeriesSource``
interfaces.

Time series properties
----------------------

  * ExternalIdBundle - the bundle of ids that the timeseries can be located with, e.g. the security ticker.
  * Name/description - a display name for the timeseries.
  * Data source - the supplier of the data, e.g. BLOOMBERG
  * Data field - the data this series contains - generally this should either be set to Market_Value (which is used by OpenGamma to look for security prices) or a data provider specific field that OpenGamma has been configured to search for instead - the corresponding value for Bloomberg is PX_LAST.
  * Data provider - the underlying data provider, e.g an exchange, commonly set to DEFAULT.
  * Observation time - the time the value was observed, commonly set to DEFAULT (LONDON_CLOSE etc also common).


Adding and updating timeseries
------------------------------

To add a timeseries you need to specify its properties and then add the series points.


.. code-block:: java

      // add new time series
      htsMaster = new InMemoryHistoricalTimeSeriesMaster();
      ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
      info.setDataField("Market_Value");
      info.setDataSource("OG");
      info.setDataProvider("DEFAULT");
      info.setObservationTime("CLOSE");
      info.setExternalIdBundle(ExternalIdBundleWithDates.of(ExternalIdBundle.of("BLOOMBERG_TICKER", "US0003M Index));
      info.setName("US0003M");
      HistoricalTimeSeriesInfoDocument htsInfoDoc = new HistoricalTimeSeriesInfoDocument();
      htsInfoDoc.setInfo(info);

      HistoricalTimeSeriesInfoDocument addedInfoDoc = _htsMaster.add(htsInfoDoc);
      s_logger.debug("Adding time series " + externalIdBundle + " from " + timeSeries.getEarliestTime() + " to " + timeSeries.getLatestTime());
      UniqueId id = htsMaster.updateTimeSeriesDataPoints(addedInfoDoc.getInfo().getTimeSeriesObjectId(), timeSeries);

There is a utility class to aid creating and updating a time series:

.. code-block:: java

      htsMaster = new InMemoryHistoricalTimeSeriesMaster();
      htsWriter = new HistoricalTimeSeriesMasterUtils(htsMaster);
      htsWriter.writeTimeSeries("US0003M", "OG", "DEFAULT", "Market_Value", "CLOSE", ExternalIdBundle.of("BLOOMBERG_TICKER", "US0003M Index)), timeseries);


Using historical time series as a source of spot market data to a view
----------------------------------------------------------------------

When a piece of market data is requested by the engine, you can configure it to obtain those values from its time series instead of a live market data connection or snapshot.
This can be configured to either:

  * Take the latest value from each time series for each required piece of market data (See ``LatestHistoricalMarketDataSpecification``).
  * Take the value as of a given time from the series for each piece of requested mark data (this can be used to value with data as of a time in the past - see ``FixedHistoricalMarketDataSpecification``).



