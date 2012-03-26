/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.integration.loadsave.portfolio.ResolvingPortfolioLoader;
import com.opengamma.integration.tool.AbstractIntegrationTool;
import com.opengamma.integration.tool.IntegrationToolContext;

/**
 * The portfolio loader tool
 */
public class ResolvingPortfolioLoaderTool extends AbstractIntegrationTool {

  /** File name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Portfolio name option flag*/
  private static final String PORTFOLIO_NAME_OPT = "n";
  /** Write option flag */
  private static final String WRITE_OPT = "w";
  /** Time series data source option flag*/
  private static final String TIME_SERIES_DATASOURCE_OPT = "s";
  /** Time series data provider option flag*/
  private static final String TIME_SERIES_DATAPROVIDER_OPT = "p";
  /** Time series data field option flag*/
  private static final String TIME_SERIES_DATAFIELD_OPT = "d";
  /** Time series observation time option flag*/
  private static final String TIME_SERIES_OBSERVATIONTIME_OPT = "o";
  
  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new ResolvingPortfolioLoaderTool().initAndRun(args);
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the portfolio into the position master.
   */
  @Override
  protected void doRun() {      
    IntegrationToolContext context = getToolContext();
    ResolvingPortfolioLoader loader = new ResolvingPortfolioLoader(context.getBloombergSecuritySource(),
                                                                   context.getHistoricalTimeSeriesMaster(),
                                                                   context.getBloombergHistoricalTimeSeriesSource(),
                                                                   context.getBloombergReferenceDataProvider(),
                                                                   context.getPortfolioMaster(),
                                                                   context.getPositionMaster(),
                                                                   context.getSecurityMaster());
    String[] dataFields = getCommandLine().getOptionValues(TIME_SERIES_DATAFIELD_OPT);
    if (dataFields == null) {
      dataFields = new String[]{"PX_LAST"};
    }
    // Call the portfolio loader with the supplied arguments
    String fileName = getCommandLine().getOptionValue(FILE_NAME_OPT);
    try {
      loader.loadPortfolio(
          getCommandLine().getOptionValue(PORTFOLIO_NAME_OPT),
          fileName,
          new BufferedInputStream(new FileInputStream(fileName)),
          getOptionValue(TIME_SERIES_DATASOURCE_OPT, "BLOOMBERG"),
          getOptionValue(TIME_SERIES_DATAPROVIDER_OPT, "CMPL"),
          dataFields,
          getOptionValue(TIME_SERIES_OBSERVATIONTIME_OPT, "LONDON_CLOSE"),
          getCommandLine().hasOption(WRITE_OPT)
      );
    } catch (FileNotFoundException e) {
      throw new OpenGammaRuntimeException("Portfolio file not found: " + fileName, e);
    }
  }

  private String getOptionValue(String optionName, String defaultValue) {
    return getCommandLine().getOptionValue(optionName) == null ? defaultValue : getCommandLine().getOptionValue(optionName);
  }

  @Override
  protected Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);
    
    Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file containing data to import (CSV, XLS or ZIP)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);
    
    Option portfolioNameOption = new Option(
        PORTFOLIO_NAME_OPT, "name", true, "The name of the destination OpenGamma portfolio");
    options.addOption(portfolioNameOption);
    
    Option writeOption = new Option(
        WRITE_OPT, "write", false, 
        "Actually persists the portfolio to the database if specified, otherwise pretty-prints without persisting");
    options.addOption(writeOption);
       
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

    return options;
  }

}
