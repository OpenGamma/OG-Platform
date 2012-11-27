/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.rest;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.json.JSONArray;

import com.google.common.collect.Lists;
import com.opengamma.engine.marketdata.spec.HistoricalMarketDataSpecification;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesRating;
import com.opengamma.util.ArgumentChecker;

/**
 * REST resource for looking up a list of valid values for time series resolver keys to use in a
 * {@link HistoricalMarketDataSpecification}. These correspond to the names of {@link HistoricalTimeSeriesRating}
 * instances stored in the configuration database.
 * @see com.opengamma.web.analytics.MarketDataSpecificationJsonReader
 */
@Path("timeseriesresolverkeys")
public class TimeSeriesResolverKeysResource {

  /**
   * The config master.
   */
  private final ConfigMaster _configMaster;

  /**
   * Creates an instance.
   * 
   * @param configMaster  the config master, not null
   */
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
    List<String> keyNames = Lists.newArrayList();
    for (ConfigDocument doc : ConfigSearchIterator.iterable(_configMaster, request)) {
      keyNames.add(doc.getName());
    }
    return new JSONArray(keyNames).toString();
  }

}
