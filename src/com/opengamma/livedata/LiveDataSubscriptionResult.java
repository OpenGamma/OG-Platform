/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

/**
 * A code containing the ultimate result of a live data
 * subscription request.
 *
 * @author kirk
 */
public enum LiveDataSubscriptionResult {
  /**
   * The subscription request was successful and data will begin
   * flowing.
   */
  SUCCESS,
  /**
   * The requested piece of live data could not be found.
   */
  NOT_PRESENT,
  /**
   * The requested piece of live data was found, but the specified user
   * was not entitled to receive it.
   */
  NOT_AUTHORIZED;
}
