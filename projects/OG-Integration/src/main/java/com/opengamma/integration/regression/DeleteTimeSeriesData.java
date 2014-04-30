/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.regression;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Deletes the time series data points from a dump.
 */
/* package */class DeleteTimeSeriesData {

  private static final Logger s_logger = LoggerFactory.getLogger(DeleteTimeSeriesData.class);

  private final RegressionIO _io;

  public DeleteTimeSeriesData(final RegressionIO io) {
    _io = ArgumentChecker.notNull(io, "io");
  }

  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      System.err.println("arguments: dataDirectory (containing Fudge XML files)");
      System.exit(1);
    }
    try {
      final RegressionIO io = new SubdirsRegressionIO(new File(args[0]), new FudgeXMLFormat(), false);
      (new DeleteTimeSeriesData(io)).run();
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
    System.exit(0);
  }

  public void run() throws IOException {
    _io.beginRead();
    final Map<String, Object> timeSeriesMap = _io.readAll(RegressionUtils.HISTORICAL_TIME_SERIES_MASTER_DATA);
    // Update the map entries in-situ, setting the time series data to blank
    int deleted = 0;
    int skipped = 0;
    for (Map.Entry<String, Object> timeSeriesEntry : timeSeriesMap.entrySet()) {
      final TimeSeriesWithInfo tswi = (TimeSeriesWithInfo) timeSeriesEntry.getValue();
      final ManageableHistoricalTimeSeries hts = tswi.getTimeSeries();
      final LocalDateDoubleTimeSeries ts = hts.getTimeSeries();
      if (!ts.isEmpty()) {
        s_logger.debug("Deleting data from {}", timeSeriesEntry.getKey());
        hts.setTimeSeries(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
        deleted++;
      } else {
        skipped++;
      }
    }
    _io.endRead();
    _io.beginWrite();
    // Write the modified map back out
    _io.write(RegressionUtils.HISTORICAL_TIME_SERIES_MASTER_DATA, timeSeriesMap);
    s_logger.info("Deleted data from {} time-series, skipped {}", deleted, skipped);
    _io.endWrite();
  }

}
