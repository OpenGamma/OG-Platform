/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.timeseries;

import java.io.InputStream;

import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.copier.timeseries.reader.SingleSheetMultiTimeSeriesReader;
import com.opengamma.integration.copier.timeseries.reader.TimeSeriesReader;
import com.opengamma.integration.copier.timeseries.writer.DummyTimeSeriesWriter;
import com.opengamma.integration.copier.timeseries.writer.MasterTimeSeriesWriter;
import com.opengamma.integration.copier.timeseries.writer.TimeSeriesWriter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Provides standard time series loader functionality
 */
public class TimeSeriesLoader {

  private final HistoricalTimeSeriesMaster _htsMaster;

  public TimeSeriesLoader(HistoricalTimeSeriesMaster htsMaster) {
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    _htsMaster = htsMaster;
  }

  public void run(SheetFormat sheetFormat,
                  InputStream portfolioFileStream,
                  String dataSource,
                  String dataProvider,
                  String dataField,
                  String observationTime,
                  String idScheme,
                  String dateFormat,
                  boolean persist) {

    // Set up writer
    TimeSeriesWriter timeSeriesWriter = constructTimeSeriesWriter(persist);
    
     // Set up reader
    TimeSeriesReader timeSeriesReader = new SingleSheetMultiTimeSeriesReader(sheetFormat,
                                                                             portfolioFileStream,
                                                                             dataSource,
                                                                             dataProvider,
                                                                             dataField,
                                                                             observationTime,
                                                                             idScheme,
                                                                             dateFormat);
    
    // Load in and write the securities, positions and trades
    timeSeriesReader.writeTo(timeSeriesWriter);
    
    // Flush changes to portfolio master
    timeSeriesWriter.flush();
    
  }
  
  private TimeSeriesWriter constructTimeSeriesWriter(boolean write) {
    if (write) {      
      // Create a portfolio writer to persist imported positions, trades and securities to the OG masters
      return new MasterTimeSeriesWriter(_htsMaster);
    } else {
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new DummyTimeSeriesWriter();         
    }
  }
}
