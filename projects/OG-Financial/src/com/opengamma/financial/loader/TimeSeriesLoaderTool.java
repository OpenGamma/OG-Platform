/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.loader.timeseries.DummyTimeSeriesWriter;
import com.opengamma.financial.loader.timeseries.MasterTimeSeriesWriter;
import com.opengamma.financial.loader.timeseries.SingleSheetMultiTimeSeriesReader;
import com.opengamma.financial.loader.timeseries.TimeSeriesReader;
import com.opengamma.financial.loader.timeseries.TimeSeriesWriter;
import com.opengamma.financial.tool.ToolContext;

/**
 * Provides standard time series loader functionality
 */
public class TimeSeriesLoaderTool {

  private static final Logger s_logger = LoggerFactory.getLogger(TimeSeriesLoaderTool.class);

  /** Tool name */
  private static final String TOOL_NAME = "OpenGamma Time Series Importer";
  /** File name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Time series data source option flag*/
  private static final String TIME_SERIES_DATASOURCE_OPT = "s";
  private static final String TIME_SERIES_DATAPROVIDER_OPT = "p";
  private static final String TIME_SERIES_DATAFIELD_OPT = "d";
  private static final String TIME_SERIES_OBSERVATIONTIME_OPT = "o";
  private static final String TIME_SERIES_IDSCHEME_OPT = "i";
  private static final String TIME_SERIES_DATEFORMAT_OPT = "t";
  /** Write option flag */
  private static final String WRITE_OPT = "w";

  /**
   * ENTRY POINT FOR COMMAND LINE TOOL
   * @param args  Command line args
   * @param toolContext  the loader context
   */
  public void run(String[] args, ToolContext toolContext) {
    s_logger.info(TOOL_NAME + " is initialising...");
    s_logger.info("Current working directory is " + System.getProperty("user.dir"));
    
    // Parse command line arguments
    CommandLine cmdLine = getCmdLine(args, true);
    
    run(cmdLine, toolContext);
  }

  private void run(CommandLine cmdLine, ToolContext toolContext) {
    // Set up writer
    TimeSeriesWriter timeSeriesWriter = constructTimeSeriesWriter(
        cmdLine.hasOption(WRITE_OPT),
        toolContext);
    
     // Set up reader
    TimeSeriesReader timeSeriesReader = constructTimeSeriesReader(
        cmdLine.getOptionValue(FILE_NAME_OPT),
        cmdLine.getOptionValue(TIME_SERIES_DATASOURCE_OPT),
        cmdLine.getOptionValue(TIME_SERIES_DATAPROVIDER_OPT),
        cmdLine.getOptionValue(TIME_SERIES_DATAFIELD_OPT),
        cmdLine.getOptionValue(TIME_SERIES_OBSERVATIONTIME_OPT),
        cmdLine.getOptionValue(TIME_SERIES_IDSCHEME_OPT),
        cmdLine.getOptionValue(TIME_SERIES_DATEFORMAT_OPT),
        toolContext);
    
    // Load in and write the securities, positions and trades
    timeSeriesReader.writeTo(timeSeriesWriter);
    
    // Flush changes to portfolio master
    timeSeriesWriter.flush();
    
    s_logger.info(TOOL_NAME + " is finished.");
  }
  
  
  private static CommandLine getCmdLine(String[] args, boolean contextProvided) {
    final Options options = getOptions(contextProvided);
    try {
      return new PosixParser().parse(options, args);
    } catch (ParseException e) {
      s_logger.warn(e.getMessage());
      (new HelpFormatter()).printHelp(" ", options);
      throw new OpenGammaRuntimeException("Could not parse the command line");
    }        
  }

  private static Options getOptions(boolean contextProvided) {
    Options options = new Options();
    Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file containing data to import (CSV or ZIP)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);
    
    Option timeSeriesDataSourceOption = new Option(
        TIME_SERIES_DATASOURCE_OPT, "source", true, "The name of the time series data source");
    options.addOption(timeSeriesDataSourceOption);
    
    Option timeSeriesDataProviderOption = new Option(
        TIME_SERIES_DATAPROVIDER_OPT, "provider", true, "The name of the time series data provider");
    options.addOption(timeSeriesDataProviderOption);
    
    Option timeSeriesDataFieldOption = new Option(
        TIME_SERIES_DATAFIELD_OPT, "field", true, "The name of the time series data field");
    options.addOption(timeSeriesDataFieldOption);
    
    Option timeSeriesObservationTimeOption = new Option(
        TIME_SERIES_OBSERVATIONTIME_OPT, "time", true, "The time series observation time");
    options.addOption(timeSeriesObservationTimeOption);
    
    Option timeSeriesIdSchemeOption = new Option(
        TIME_SERIES_IDSCHEME_OPT, "scheme", true, "The time series ID scheme (e.g. RIC)");
    options.addOption(timeSeriesIdSchemeOption);
    
    Option timeSeriesDateFormatOption = new Option(
        TIME_SERIES_DATEFORMAT_OPT, "date", true, "The JodaTime date format (e.g. yyyyMMdd)");
    options.addOption(timeSeriesDateFormatOption);
    
    Option writeOption = new Option(
        WRITE_OPT, "write", false, 
        "Actually persists the time series to the database if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);
        
    return options;
  }

  private static TimeSeriesWriter constructTimeSeriesWriter(boolean write, ToolContext toolContext) {
    if (write) {  
      s_logger.info("Write option specified, will persist to OpenGamma masters");
    
      // Create a portfolio writer to persist imported positions, trades and securities to the OG masters
      return new MasterTimeSeriesWriter(toolContext);
  
    } else {
      s_logger.info("Write option omitted, will pretty-print instead of persisting to OpenGamma masters");
      
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new DummyTimeSeriesWriter();         
    }

  }
  
  private static TimeSeriesReader constructTimeSeriesReader(String filename, 
      String dataSource, String dataProvider, String dataField, String observationTime, String idScheme, String dateFormat, ToolContext toolContext) {
    
    String extension = filename.substring(filename.lastIndexOf('.'));
    
    // Single CSV or XLS file extension
    if (extension.equalsIgnoreCase(".csv") || extension.equalsIgnoreCase(".xls")) {
      return new SingleSheetMultiTimeSeriesReader(filename, dataSource, dataProvider, dataField, observationTime, idScheme, dateFormat);
    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV or .ZIP");
    }
  }
  
}
