/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.timeseries;

import java.net.URI;

import com.opengamma.master.timeseries.HistoricalDataDocument;

/**
 * URIs for web-based historical data.
 */
public class WebHistoricalDataUris {

  /**
   * The data.
   */
  private final WebHistoricalDataData _data;

  /**
   * Creates an instance.
   * @param data  the web data, not null
   */
  public WebHistoricalDataUris(WebHistoricalDataData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the URI.
   * @return the URI
   */
  public URI allTimeSeries() {
    return WebAllHistoricalDataResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI oneTimeSeries() {
    return WebHistoricalDataResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param timeSeries  the historical data, not null
   * @return the URI
   */
  public URI oneTimeSeries(final HistoricalDataDocument timeSeries) {
    return WebHistoricalDataResource.uri(_data, timeSeries.getUniqueId());
  }

}
