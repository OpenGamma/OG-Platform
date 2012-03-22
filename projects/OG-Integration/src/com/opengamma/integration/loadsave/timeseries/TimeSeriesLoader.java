/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.loadsave.timeseries;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.loadsave.timeseries.reader.SingleSheetMultiTimeSeriesReader;
import com.opengamma.integration.loadsave.timeseries.reader.TimeSeriesReader;
import com.opengamma.integration.loadsave.timeseries.writer.DummyTimeSeriesWriter;
import com.opengamma.integration.loadsave.timeseries.writer.MasterTimeSeriesWriter;
import com.opengamma.integration.loadsave.timeseries.writer.TimeSeriesWriter;

/**
 * Provides standard time series loader functionality
 */
public class TimeSeriesLoader {

  public void run(String fileName, String dataSource, String dataProvider, String dataField, 
      String observationTime, String idScheme, String dateFormat, boolean persist, ToolContext toolContext) {
    
    // Set up writer
    TimeSeriesWriter timeSeriesWriter = constructTimeSeriesWriter(
        persist,
        toolContext);
    
     // Set up reader
    TimeSeriesReader timeSeriesReader = constructTimeSeriesReader(
        fileName,
        dataSource,
        dataProvider,
        dataField,
        observationTime,
        idScheme,
        dateFormat,
        toolContext);
    
    // Load in and write the securities, positions and trades
    timeSeriesReader.writeTo(timeSeriesWriter);
    
    // Flush changes to portfolio master
    timeSeriesWriter.flush();
    
  }
  
  private static TimeSeriesWriter constructTimeSeriesWriter(boolean write, ToolContext toolContext) {
    if (write) {      
      // Create a portfolio writer to persist imported positions, trades and securities to the OG masters
      return new MasterTimeSeriesWriter(toolContext);
    } else {
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new DummyTimeSeriesWriter();         
    }

  }
  
  private static TimeSeriesReader constructTimeSeriesReader(String filename, 
      String dataSource, String dataProvider, String dataField, String observationTime, 
      String idScheme, String dateFormat, ToolContext toolContext) {
    
    String extension = filename.substring(filename.lastIndexOf('.'));
    
    // Single CSV or XLS file extension
    if (extension.equalsIgnoreCase(".csv") || extension.equalsIgnoreCase(".xls")) {
      return new SingleSheetMultiTimeSeriesReader(filename, dataSource, dataProvider, dataField, observationTime, idScheme, dateFormat);
    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV or .ZIP");
    }
  }
  
}
