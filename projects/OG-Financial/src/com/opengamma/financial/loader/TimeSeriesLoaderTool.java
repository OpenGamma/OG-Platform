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
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.loader.timeseries.DummyTimeSeriesWriter;
import com.opengamma.financial.loader.timeseries.MasterTimeSeriesWriter;
import com.opengamma.financial.loader.timeseries.SingleSheetMultiTimeSeriesReader;
import com.opengamma.financial.loader.timeseries.TimeSeriesReader;
import com.opengamma.financial.loader.timeseries.TimeSeriesWriter;
import com.opengamma.util.PlatformConfigUtils;

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
  /** Run mode option flag */
  private static final String RUN_MODE_OPT = "r";
  /** Write option flag */
  private static final String WRITE_OPT = "w";

  /**
   * ENTRY POINT FOR COMMAND LINE TOOL
   * @param args  Command line args
   */
  public void run(String[] args) { 
    s_logger.info(TOOL_NAME + " is initialising...");
    s_logger.info("Current working directory is " + System.getProperty("user.dir"));
    
    // Parse command line arguments
    CommandLine cmdLine = getCmdLine(args, false);
    
    // Configure the OG platform
    PlatformConfigUtils.configureSystemProperties(cmdLine.getOptionValue(RUN_MODE_OPT));
    AbstractApplicationContext applicationContext = 
        new ClassPathXmlApplicationContext("com/opengamma/financial/loader/loaderContext.xml");
    
    // Get an OG loader context, which will provide access to any required masters/sources
    applicationContext.start();
    LoaderContext loaderContext = (LoaderContext) applicationContext.getBean("loaderContext");
    
    run(cmdLine, loaderContext);
    
    // Clean up and shut down
    applicationContext.close();
  }

  /**
   * ENTRY POINT FOR COMMAND LINE TOOL
   * @param args  Command line args
   * @param loaderContext  the loader context
   */
  public void run(String[] args, LoaderContext loaderContext) {
    s_logger.info(TOOL_NAME + " is initialising...");
    s_logger.info("Current working directory is " + System.getProperty("user.dir"));
    
    // Parse command line arguments
    CommandLine cmdLine = getCmdLine(args, true);
    
    run(cmdLine, loaderContext);
  }

  private void run(CommandLine cmdLine, LoaderContext loaderContext) {
    // Set up writer
    TimeSeriesWriter timeSeriesWriter = constructTimeSeriesWriter(
        cmdLine.hasOption(WRITE_OPT),
        loaderContext);
    
     // Set up reader
    TimeSeriesReader timeSeriesReader = constructTimeSeriesReader(
        cmdLine.getOptionValue(FILE_NAME_OPT),
        cmdLine.getOptionValue(TIME_SERIES_DATASOURCE_OPT),
        cmdLine.getOptionValue(TIME_SERIES_DATAPROVIDER_OPT),
        cmdLine.getOptionValue(TIME_SERIES_DATAFIELD_OPT),
        cmdLine.getOptionValue(TIME_SERIES_OBSERVATIONTIME_OPT),
        loaderContext);
    
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
        TIME_SERIES_DATASOURCE_OPT, "name", true, "The name of the time series data source");
    options.addOption(timeSeriesDataSourceOption);
    Option timeSeriesDataProviderOption = new Option(
        TIME_SERIES_DATAPROVIDER_OPT, "name", true, "The name of the time series data provider");
    options.addOption(timeSeriesDataProviderOption);
    Option timeSeriesDataFieldOption = new Option(
        TIME_SERIES_DATAFIELD_OPT, "name", true, "The name of the time series data field");
    options.addOption(timeSeriesDataFieldOption);
    Option timeSeriesObservationTimeOption = new Option(
        TIME_SERIES_OBSERVATIONTIME_OPT, "name", true, "The time series observation time");
    options.addOption(timeSeriesObservationTimeOption);
    
    if (contextProvided == false) {
      Option runModeOption = new Option(
          RUN_MODE_OPT, "runmode", true, "The OpenGamma run mode: shareddev, standalone");
      runModeOption.setRequired(true);
      options.addOption(runModeOption);
    }
    
    Option writeOption = new Option(
        WRITE_OPT, "write", false, 
        "Actually persists the time series to the database if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);
        
    return options;
  }

  private static TimeSeriesWriter constructTimeSeriesWriter(boolean write,
      LoaderContext loaderContext) {
    
    if (write) {  
      s_logger.info("Write option specified, will persist to OpenGamma masters");
    
      // Create a portfolio writer to persist imported positions, trades and securities to the OG masters
      return new MasterTimeSeriesWriter(loaderContext);
  
    } else {
      s_logger.info("Write option omitted, will pretty-print instead of persisting to OpenGamma masters");
      
      // Create a dummy portfolio writer to pretty-print instead of persisting
      return new DummyTimeSeriesWriter();         
    }

  }
  
  private static TimeSeriesReader constructTimeSeriesReader(String filename, 
      String dataSource, String dataProvider, String dataField, String observationTime, LoaderContext loaderContext) {
    
    String extension = filename.substring(filename.lastIndexOf('.'));
    
    // Single CSV or XLS file extension
    if (extension.equalsIgnoreCase(".csv") || extension.equalsIgnoreCase(".xls")) {
      return new SingleSheetMultiTimeSeriesReader(filename, dataSource, dataProvider, dataField, observationTime);
    } else {
      throw new OpenGammaRuntimeException("Input filename should end in .CSV or .ZIP");
    }
  }
  
}
