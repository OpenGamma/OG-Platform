/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.timeseries;

import java.net.URI;

import com.opengamma.master.timeseries.TimeSeriesDocument;

/**
 * URIs for web-based time series.
 */
public class WebTimeSeriesUris {

  /**
   * The data.
   */
  private final WebTimeSeriesData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebTimeSeriesUris(WebTimeSeriesData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the URI.
   * @return the URI
   */
  public URI allTimeSeries() {
    return WebAllTimeSeriesResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI oneTimeSeries() {
    return WebOneTimeSeriesResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param timeSeries  the time series, not null
   * @return the URI
   */
  public URI oneTimeSeries(final TimeSeriesDocument<?> timeSeries) {
    return WebOneTimeSeriesResource.uri(_data, timeSeries.getUniqueId());
  }

}
