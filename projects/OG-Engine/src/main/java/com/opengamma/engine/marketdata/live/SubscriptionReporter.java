/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.Map;

import javax.management.MXBean;

/**
 * MXBean giving visibility of the state of a market data subscriptions.
 */
@MXBean
public interface SubscriptionReporter {

  /**
   * Get information on subscriptions for a particular ticker or partial
   * ticker. Information on any ticker which matches the supplied string
   * will be returned.
   *
   * @param ticker the ticker to search for
   * @return map (ticker -> subscription data) for each matching ticker
   */
  Map<String, SubscriptionInfo> queryByTicker(String ticker);

  /**
   * Return the total number of market data value specifications for which
   * subscriptions have been attempted.
   *
   * @return the number of specifications
   */
  int getSpecificationCount();

  /**
   * Return the total number of unique market data subscription requests
   * which are being made. i.e. number of requests that have been sent to
   * the market data server.
   *
   * @return the number of subscription requests
   */
  int getSubscriptionCount();

  /**
   * Return the user for these market data subscriptions.
   *
   * @return the user
   */
  String getMarketDataUser();
  
}
