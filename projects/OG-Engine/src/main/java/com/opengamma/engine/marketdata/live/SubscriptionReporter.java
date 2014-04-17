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
   * Gets the total number of unique market data subscriptions that have been requested.
   *
   * @return the number of subscriptions requested
   */
  int getRequestedLiveDataSubscriptionCount();

  /**
   * Gets the total number of subscriptions on value specifications that are currently active.
   *
   * @return the number of unique active subscriptions
   */
  int getActiveValueSpecificationSubscriptionCount();

  /**
   * Return the user for these market data subscriptions.
   *
   * @return the user
   */
  String getMarketDataUser();
  
}
