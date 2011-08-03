/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.batch;

import java.net.URI;

import com.opengamma.id.UniqueId;

/**
 * URIs for web-based batch management.
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
   * @param batchId  the override batch id, not null
   * @return the URI
   */
  public URI batch(final UniqueId batchId) {
    return WebBatchResource.uri(_data, batchId);
  }

}
