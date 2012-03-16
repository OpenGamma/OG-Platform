/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.production.tool.config;

import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DATA_PROVIDER_NAME;
import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DATA_SOURCE_NAME;
import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.DEFAULT_CONFIG_NAME;
import static com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingFieldNames.STAR_VALUE;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRatingRule;

/**
 * Puts an entry in the TSS configuration for default MetaData lookup.
 * See {@link TimeSeriesConfigDocumentLoaderTool}.
 */
public class TimeSeriesConfigDocumentLoader {

  private static final HistoricalTimeSeriesRating DEFAULT_CONFIG = buildDefaultConfig();
  private ConfigMaster _configMaster;

  public TimeSeriesConfigDocumentLoader(final ConfigMaster configMaster) {
    _configMaster = configMaster;
  }

  private static HistoricalTimeSeriesRating buildDefaultConfig() {
    List<HistoricalTimeSeriesRatingRule> rules = new ArrayList<HistoricalTimeSeriesRatingRule>();
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_SOURCE_NAME, "BLOOMBERG", 2));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_SOURCE_NAME, "REUTERS", 1));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_SOURCE_NAME, STAR_VALUE, 0));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_PROVIDER_NAME, "CMPL", 3));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_PROVIDER_NAME, "CMPN", 2));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_PROVIDER_NAME, "CMPT", 1));
    rules.add(new HistoricalTimeSeriesRatingRule(DATA_PROVIDER_NAME, STAR_VALUE, 0));
    return new HistoricalTimeSeriesRating(rules);
  }

  public ConfigMaster getConfigMaster() {
    return _configMaster;
  }

  /**
   * Runs the tool.
   */
  public void run() {
    final ConfigDocument<HistoricalTimeSeriesRating> configDocument = new ConfigDocument<HistoricalTimeSeriesRating>(HistoricalTimeSeriesRating.class);
    configDocument.setName(DEFAULT_CONFIG_NAME);
    configDocument.setValue(DEFAULT_CONFIG);
    final ConfigSearchRequest<HistoricalTimeSeriesRating> req = new ConfigSearchRequest<HistoricalTimeSeriesRating>();
    req.setName(configDocument.getName());
    final ConfigSearchResult<HistoricalTimeSeriesRating> res = getConfigMaster().search(req);
    if (res.getDocuments().isEmpty()) {
      getConfigMaster().add(configDocument);
    } else {
      configDocument.setUniqueId(res.getDocuments().get(0).getUniqueId());
      getConfigMaster().update(configDocument);
    }
  }

}
