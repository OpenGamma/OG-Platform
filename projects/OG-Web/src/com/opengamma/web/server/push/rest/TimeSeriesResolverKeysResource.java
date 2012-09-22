/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.json.JSONArray;

import com.google.common.collect.Lists;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating;
import com.opengamma.util.ArgumentChecker;

/**
 * REST resource for looking up a list of valid values for time series resolver keys to use in a
 * {@link HistoricalMarketDataSpecification}. These correspond to the names of {@link HistoricalTimeSeriesRating}
 * instances stored in the configuration database.
 * @see com.opengamma.web.server.push.analytics.MarketDataSpecificationJsonReader
 */
@Path("timeseriesresolverkeys")
public class TimeSeriesResolverKeysResource {

  private final ConfigMaster _configMaster;

  public TimeSeriesResolverKeysResource(ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    _configMaster = configMaster;
  }

  /**
   * @return JSON array of resolver keys: {@code ["KEY1", "KEY2", ...]}
   */
  @GET
  public String getResolverKeys() {
    ConfigSearchRequest<HistoricalTimeSeriesRating> request =
        new ConfigSearchRequest<HistoricalTimeSeriesRating>(HistoricalTimeSeriesRating.class);
    ConfigSearchResult<HistoricalTimeSeriesRating> result = _configMaster.search(request);
    List<ConfigDocument<HistoricalTimeSeriesRating>> documents = result.getDocuments();
    List<String> keyNames = Lists.newArrayListWithCapacity(documents.size());
    for (ConfigDocument<HistoricalTimeSeriesRating> document : documents) {
      keyNames.add(document.getName());
    }
    return new JSONArray(keyNames).toString();
  }
}
