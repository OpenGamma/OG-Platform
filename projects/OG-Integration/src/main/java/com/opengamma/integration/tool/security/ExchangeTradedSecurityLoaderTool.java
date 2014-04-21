/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.scripts.Scriptable;

/**
 * The exchange-traded security loader tool
 */
@Scriptable
public class ExchangeTradedSecurityLoaderTool extends AbstractTool<IntegrationToolContext> {
  private static final Logger s_logger = LoggerFactory.getLogger(ExchangeTradedSecurityLoaderTool.class);
  /** File name option flag */
  private static final String FILE_NAME_OPT = "f";
  /** Time series data provider option flag*/
  private static final String TIME_SERIES_DATAPROVIDER_OPT = "p";
  /** Time series data field option flag*/
  private static final String TIME_SERIES_DATAFIELD_OPT = "d";
  /** Populate time series */
  private static final String POPULATE_TIME_SERIES_OPT = "ts";
  
  private static final String DEFAULT_DATA_PROVIDER = "DEFAULT";
  private static final String DEFAULT_DATA_FIELD = "PX_LAST";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new ExchangeTradedSecurityLoaderTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  /**
   * Loads the portfolio into the position master.
   */
  @Override
  protected void doRun() {      
    IntegrationToolContext context = getToolContext();

    SecurityLoader loader = context.getSecurityLoader();
    
    Set<ExternalIdBundle> externalIdBundles = new LinkedHashSet<>();
    Set<ExternalId> externalIds = new LinkedHashSet<>();
    File file = new File(getCommandLine().getOptionValue(FILE_NAME_OPT));

    if (file.exists()) {
      try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
        String line;
        while ((line = reader.readLine()) != null) { 
          try {
            ExternalId externalId = ExternalId.parse(line);
            externalIdBundles.add(externalId.toBundle());
            externalIds.add(externalId);
          } catch (IllegalArgumentException iae) {
            s_logger.error("Couldn't parse identifier {}, skipping", line);
          }
        }
      } catch (IOException ioe) {
        s_logger.error("Problem reading file");
        System.exit(1);
      }
    } else {
      s_logger.error("File not found");
      System.exit(1);
    }

    s_logger.info("Starting to load securities");
    Map<ExternalIdBundle, UniqueId> loadSecurities = loader.loadSecurities(externalIdBundles);
    s_logger.info("Loaded {} securities", loadSecurities.size());
    s_logger.info("Finished loading securities");
    
    if (getCommandLine().hasOption(POPULATE_TIME_SERIES_OPT)) {  
      // Load time series
      HistoricalTimeSeriesLoader tsLoader = context.getHistoricalTimeSeriesLoader();
      String dataProvider = getCommandLine().getOptionValue(TIME_SERIES_DATAPROVIDER_OPT, DEFAULT_DATA_PROVIDER);
      String dataField = getCommandLine().getOptionValue(TIME_SERIES_DATAFIELD_OPT, DEFAULT_DATA_FIELD);
      
      s_logger.info("Starting to load time series from data provider {} using field {}", dataProvider, dataField);
      Map<ExternalId, UniqueId> loadTimeSeries = tsLoader.loadTimeSeries(externalIds, dataProvider, dataField, null, null);
      s_logger.info("Loaded {} time series", loadTimeSeries.size());
      s_logger.info("Finished loading time series");
    } else {
      s_logger.info("Time series load not requested, skipping");
    }
    s_logger.info("Done.");
  }
  
  @Override
  protected Options createOptions(boolean contextProvided) {
    
    Options options = super.createOptions(contextProvided);
    
    Option filenameOption = new Option(
        FILE_NAME_OPT, "filename", true, "The path to the file containing data to import (Text file, one ID per line)");
    filenameOption.setRequired(true);
    options.addOption(filenameOption);
   
    Option populateTimeSeriesOption = new Option(
        POPULATE_TIME_SERIES_OPT, "time-series");
    options.addOption(populateTimeSeriesOption);
    populateTimeSeriesOption.setRequired(false);
    
    Option timeSeriesDataProviderOption = new Option(
        TIME_SERIES_DATAPROVIDER_OPT, "provider", true, "The name of the time series data provider (default DEFAULT)");
    timeSeriesDataProviderOption.setRequired(false);
    options.addOption(timeSeriesDataProviderOption);
    
    Option timeSeriesDataFieldOption = new Option(
        TIME_SERIES_DATAFIELD_OPT, "field", true, "The name of the time series data field (default PX_LAST)");
    timeSeriesDataFieldOption.setRequired(false);
    options.addOption(timeSeriesDataFieldOption);
    
    return options;
  }


}
