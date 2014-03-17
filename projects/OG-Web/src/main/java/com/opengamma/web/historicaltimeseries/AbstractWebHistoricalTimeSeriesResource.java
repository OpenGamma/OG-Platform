/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.historicaltimeseries;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractPerRequestWebResource;

/**
 * Abstract base class for RESTful historical time-series resources.
 */
public abstract class AbstractWebHistoricalTimeSeriesResource
    extends AbstractPerRequestWebResource<WebHistoricalTimeSeriesData> {

  /**
   * HTML ftl directory
   */
  protected static final String HTML_DIR = "timeseries/html/";
  /**
   * JSON ftl directory
   */
  protected static final String JSON_DIR = "timeseries/json/";

  /**
   * Creates the resource.
   * 
   * @param master  the historical data master, not null
   * @param loader  the historical data loader, not null
   * @param configSource  the configuration source, not null
   */
  protected AbstractWebHistoricalTimeSeriesResource(final HistoricalTimeSeriesMaster master, final HistoricalTimeSeriesLoader loader, final ConfigSource configSource) {
    super(new WebHistoricalTimeSeriesData());
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(loader, "loader");
    ArgumentChecker.notNull(configSource, "configSource");
    data().setHistoricalTimeSeriesMaster(master);
    data().setHistoricalTimeSeriesLoader(loader);
    data().setConfigSource(configSource);
  }

  /**
   * Creates the resource.
   * 
   * @param parent  the parent resource, not null
   */
  protected AbstractWebHistoricalTimeSeriesResource(final AbstractWebHistoricalTimeSeriesResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * 
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    out.put("uris", new WebHistoricalTimeSeriesUris(data()));
    return out;
  }

}
