/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.historicaltimeseries;

import java.net.URI;

import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;

/**
 * URIs for web-based historical time-series.
 */
public class WebHistoricalTimeSeriesUris {

  /**
   * The data.
   */
  private final WebHistoricalTimeSeriesData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebHistoricalTimeSeriesUris(WebHistoricalTimeSeriesData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the base URI.
   * @return the URI
   */
  public URI base() {
    return allTimeSeries();
  }
  
  /**
   * Gets the URI.
   * @return the URI
   */
  public URI allTimeSeries() {
    return WebAllHistoricalTimeSeriesResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI oneTimeSeries() {
    return WebHistoricalTimeSeriesResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param info  the historical data, not null
   * @return the URI
   */
  public URI oneTimeSeries(final HistoricalTimeSeriesInfoDocument info) {
    return WebHistoricalTimeSeriesResource.uri(_data, info.getUniqueId());
  }

}
