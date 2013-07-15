/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.hts;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.loader.hts.BloombergHTSMasterUpdater;
import com.opengamma.bbg.referencedata.ReferenceDataProvider;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.time.DateUtils;

/**
 * Tool to load time-series information from Bloomberg.
 * <p>
 * This loads missing historical time-series data from Bloomberg.
 */
@Scriptable
public class BloombergHTSMasterUpdaterTool extends AbstractTool<IntegrationToolContext> {
  
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergHTSMasterUpdaterTool.class);
  
  /** Command line option. */
  private static final String RELOAD_OPTION = "reload";
  /** Command line option. */
  private static final String START_OPTION = "start";
  /** Command line option. */
  private static final String END_OPTION = "end";

  /**
   * Main method to run the tool.
   * 
   * <pre>
   * usage: java com.opengamma.bbg.loader.BloombergTimeSeriesTool [options]... [files]...
   *  -e,--end (yyyymmdd)                            End date
   *  -h,--help                                      Print this message
   *  -r,--reload                                    Reload historical data
   *  -s,--start (yyyymmdd)                          Start date
   * </pre>
   * 
   * @param args the command line arguments
   */
  public static void main(String[] args) {   // CSIGNORE
    boolean success = new BloombergHTSMasterUpdaterTool().initAndRun(args, IntegrationToolContext.class);
    System.exit(success ? 0 : 1);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    final HistoricalTimeSeriesMaster historicalTimeSeriesMaster = getToolContext().getHistoricalTimeSeriesMaster();
    if (historicalTimeSeriesMaster == null) {
      s_logger.warn("Historical timeseries master is missing in toolContext");
      return;
    }
    HistoricalTimeSeriesProvider historicalTimeSeriesProvider = getToolContext().getHistoricalTimeSeriesProvider();
    if (historicalTimeSeriesProvider == null) {
      s_logger.warn("Historical timeseries provider is missing in toolContext");
      return;
    }
    ReferenceDataProvider bloombergReferenceDataProvider = getToolContext().getBloombergReferenceDataProvider();
    if (bloombergReferenceDataProvider == null) {
      s_logger.warn("Bloomberg reference data provider is missing in toolContext");
      return;
    }
    
    BloombergHTSMasterUpdater loader = new BloombergHTSMasterUpdater(historicalTimeSeriesMaster, historicalTimeSeriesProvider, new BloombergIdentifierProvider(bloombergReferenceDataProvider));
    configureOptions(getCommandLine(), loader);
    
    loader.run();
  }

  private static void configureOptions(CommandLine line, BloombergHTSMasterUpdater dataLoader) {
    if (line.hasOption(START_OPTION)) {
      String startOption = line.getOptionValue(START_OPTION);
      try {
        LocalDate startDate = DateUtils.toLocalDate(startOption);
        dataLoader.setStartDate(startDate);
      } catch (Exception ex) {
        throw new OpenGammaRuntimeException("Unable to parse start date " + startOption, ex);
      }
    }
    if (line.hasOption(END_OPTION)) {
      String endOption = line.getOptionValue(END_OPTION);
      try {
        LocalDate endDate = DateUtils.toLocalDate(endOption);
        dataLoader.setEndDate(endDate);
      } catch (Exception ex) {
        throw new OpenGammaRuntimeException("Unable to parse end date " + endOption, ex);
      }
    }
    dataLoader.setReload(line.hasOption(RELOAD_OPTION));
  }

  //-------------------------------------------------------------------------
  @Override
  protected Options createOptions(boolean mandatoryConfigResource) {
    Options options = super.createOptions(mandatoryConfigResource);
    options.addOption(createReloadOption());
    options.addOption(createStartOption());
    options.addOption(createEndOption());
    return options;
  }

  private static Option createReloadOption() {
    return new Option("r", RELOAD_OPTION, false, "Reload historical data");
  }

  private static Option createStartOption() {
    OptionBuilder.withLongOpt(START_OPTION);
    OptionBuilder.withDescription("Start date");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("yyyymmdd");
    return OptionBuilder.create("s");
  }

  private static Option createEndOption() {
    OptionBuilder.withLongOpt(END_OPTION);
    OptionBuilder.withDescription("End date");
    OptionBuilder.hasArg();
    OptionBuilder.withArgName("yyyymmdd");
    return OptionBuilder.create("e");
  }

}
