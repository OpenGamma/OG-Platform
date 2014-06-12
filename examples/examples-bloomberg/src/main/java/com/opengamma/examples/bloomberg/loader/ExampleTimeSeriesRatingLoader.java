/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.bloomberg.loader;

import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DATA_PROVIDER_NAME;
import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DATA_SOURCE_NAME;
import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.bbg.BloombergConstants;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.integration.tool.IntegrationToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingRule;
import com.opengamma.scripts.Scriptable;

/**
 * Example code to create a timeseries rating document
 * <p>
 * It is designed to run against the HSQLDB example database.
 * It should be possible to run this class with no extra command line parameters.
 */
@Scriptable
public class ExampleTimeSeriesRatingLoader extends AbstractTool<IntegrationToolContext> {

  /**
   * Main method to run the tool.
   * 
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {  // CSIGNORE
    new ExampleTimeSeriesRatingLoader().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    final List<HistoricalTimeSeriesRatingRule> rules = new ArrayList<>();
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_SOURCE_NAME, "BLOOMBERG", 2));
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_SOURCE_NAME, BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME, 1));
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_PROVIDER_NAME, "UNKNOWN", 2));
    rules.add(HistoricalTimeSeriesRatingRule.of(DATA_PROVIDER_NAME, BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME, 1));
    final HistoricalTimeSeriesRating ratingConfig = HistoricalTimeSeriesRating.of(rules);
    final ConfigItem<HistoricalTimeSeriesRating> configDoc = ConfigItem.of(ratingConfig, DEFAULT_CONFIG_NAME, HistoricalTimeSeriesRating.class);
    ConfigMasterUtils.storeByName(configMaster, configDoc);
  }

}
