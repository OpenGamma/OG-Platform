/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.tool;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.opengamma.bbg.BloombergIdentifierProvider;
import com.opengamma.bbg.loader.hts.BloombergHistoricalTimeSeriesLoader;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesInfoSearchIterator;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.scripts.Scriptable;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;

/**
 * Replaces given timeseries data provider with a given one
 */
@Scriptable
public class TimeSeriesDataProviderReplaceTool extends AbstractTool<IntegrationToolContext> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(TimeSeriesDataProviderReplaceTool.class);

  private static final String REPLACE_WITH_PROVIDER_OPTION = "replaceWith";
  private static final String FIND_PROVIDER_OPTION = "find";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(String[] args) { //CSIGNORE
    new TimeSeriesDataProviderReplaceTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    CommandLine commandLine = getCommandLine();
    String findProvider = getCommandLineOption(commandLine, FIND_PROVIDER_OPTION);
    String replaceWithProvider = getCommandLineOption(commandLine, REPLACE_WITH_PROVIDER_OPTION);
    
    HistoricalTimeSeriesMaster htsMaster = getToolContext().getHistoricalTimeSeriesMaster();
    HistoricalTimeSeriesProvider htsProvider = getToolContext().getHistoricalTimeSeriesProvider();
    
    BloombergHistoricalTimeSeriesLoader loader = new BloombergHistoricalTimeSeriesLoader(htsMaster, htsProvider, 
        new BloombergIdentifierProvider(getToolContext().getBloombergReferenceDataProvider()));
    
    for (HistoricalTimeSeriesInfoDocument infoDoc : HistoricalTimeSeriesInfoSearchIterator.iterable(htsMaster, getHistoricalSearchRequest(findProvider))) {
      ExternalIdBundle bundle = infoDoc.getInfo().getExternalIdBundle().toBundle();
      ExternalId buid = bundle.getExternalId(ExternalSchemes.BLOOMBERG_BUID);
      ExternalId ticker = bundle.getExternalId(ExternalSchemes.BLOOMBERG_TICKER);
      String dataField = infoDoc.getInfo().getDataField();
      if (buid != null) {
        s_logger.info("replacing {} {}", buid, ticker);
        try {
          LocalDateDoubleTimeSeries hts = htsProvider.getHistoricalTimeSeries(ExternalIdBundle.of(buid),
              BLOOMBERG_DATA_SOURCE_NAME, replaceWithProvider, dataField);
          if (hts != null) {
            Map<ExternalId, UniqueId> addedTS = loader.loadTimeSeries(Sets.newHashSet(buid), replaceWithProvider, dataField, null, null);
            if (addedTS.get(buid) != null) {
              htsMaster.remove(infoDoc.getUniqueId());
              s_logger.info("removed TS with buid={}, ticker={}, dataProvider={}, dataField={}", new Object[] {buid, ticker, findProvider, dataField});
            }
          }
        } catch (Exception ex) {
          s_logger.warn("Error trying to load TS for " + buid, ex);
        }
      }
    }
  }

  private HistoricalTimeSeriesInfoSearchRequest getHistoricalSearchRequest(String findProvider) {
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setDataProvider(findProvider);
    request.setDataSource(BLOOMBERG_DATA_SOURCE_NAME);
    return request;
  }

  private String getCommandLineOption(CommandLine commandLine, String optionName) {
    return commandLine.getOptionValue(optionName);
  }

  protected Options createOptions(boolean mandatoryConfigResource) {
    Options options = super.createOptions(mandatoryConfigResource);
    options.addOption(createFromDataProviderOption());
    options.addOption(createToDataProviderOption());
    return options;
  }

  @SuppressWarnings("static-access")
  private Option createFromDataProviderOption() {
    return OptionBuilder.isRequired(true)
        .hasArgs()
        .withArgName("find provider")
        .withDescription("The previous data provider")
        .withLongOpt(FIND_PROVIDER_OPTION)
        .create("f");
  }
  
  @SuppressWarnings("static-access")
  private Option createToDataProviderOption() {
    return OptionBuilder.isRequired(true)
        .hasArgs()
        .withArgName("replace with provider")
        .withDescription("The new data provider")
        .withLongOpt(REPLACE_WITH_PROVIDER_OPTION)
        .create("r");
  }

}
