/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.marketdata;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.integration.copier.sheet.SheetFormat;
import com.opengamma.integration.copier.timeseries.TimeSeriesLoader;
import com.opengamma.scripts.Scriptable;

/**
 * The timeseries loader tool
 */
@Scriptable
public class TimeSeriesLoaderTool extends AbstractTool<ToolContext> {

  /** File name option flag */
  public static final String FILE_NAME_OPT = "f";
  /** Time series data source option flag*/
  public static final String TIME_SERIES_DATASOURCE_OPT = "s";
  /** Time series data provider option flag*/
  public static final String TIME_SERIES_DATAPROVIDER_OPT = "p";
  /** Time series data field option flag*/
  public static final String TIME_SERIES_DATAFIELD_OPT = "d";
  /** Time series observation time option flag*/
  public static final String TIME_SERIES_OBSERVATIONTIME_OPT = "o";
  /** Time series ID scheme option flag*/
  public static final String TIME_SERIES_IDSCHEME_OPT = "i";
  /** Time series date format option flag*/
  public static final String TIME_SERIES_DATEFORMAT_OPT = "t";
  /** Write option flag */
  public static final String WRITE_OPT = "w";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new TimeSeriesLoaderTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the test portfolio into the position master.
   */
  @Override 
  protected void doRun() {
    String fileName = getCommandLine().getOptionValue(FILE_NAME_OPT);
    SheetFormat sheetFormat = SheetFormat.of(fileName);
    try {
      new TimeSeriesLoader(getToolContext().getHistoricalTimeSeriesMaster()).run(
          sheetFormat,
          new BufferedInputStream(new FileInputStream(fileName)),
          getCommandLine().getOptionValue(TIME_SERIES_DATASOURCE_OPT),
          getCommandLine().getOptionValue(TIME_SERIES_DATAPROVIDER_OPT),
          getCommandLine().getOptionValue(TIME_SERIES_DATAFIELD_OPT),
          getCommandLine().getOptionValue(TIME_SERIES_OBSERVATIONTIME_OPT),
          getCommandLine().getOptionValue(TIME_SERIES_IDSCHEME_OPT),
          getCommandLine().getOptionValue(TIME_SERIES_DATEFORMAT_OPT),
          getCommandLine().hasOption(WRITE_OPT));
    } catch (FileNotFoundException e) {
      throw new OpenGammaRuntimeException("Could not find portfolio file", e);
    }
  }

  @Override
  protected  Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);
    
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

}
