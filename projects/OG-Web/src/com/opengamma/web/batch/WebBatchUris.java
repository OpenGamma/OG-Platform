/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.batch;

import java.net.URI;

import javax.time.calendar.LocalDate;

/**
 * 
 */
public class WebBatchUris {
  
  /**
   * The data.
   */
  private final WebBatchData _data;

  /**
   * Creates an instance.
   * @param data  the batch data, not null
   */
  public WebBatchUris(WebBatchData data) {
    _data = data;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the URI.
   * @return the URI
   */
  public URI batches() {
    return WebBatchesResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @return the URI
   */
  public URI batch() {
    return WebBatchResource.uri(_data);
  }

  /**
   * Gets the URI.
   * @param date batch date, not null
   * @param observationTime date batch time, e.g., LDN_CLOSE, not null
   * @return the URI
   */
  public URI batch(final LocalDate date, final String observationTime) {
    return WebBatchResource.uri(_data, date, observationTime);
  }

}
