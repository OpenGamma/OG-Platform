/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import java.util.Map;

import javax.management.MXBean;

/**
 * An MXBean allowing querying of the current state of subscriptions.
 */
@MXBean
public interface SubscriptionStateQuery {

  /**
   * Return the number of failed subscriptions. These are the subscriptions
   * which were unable to be fulfilled by the market data provider.
   *
   * @return the number of failed subscriptions
   */
  int getFailedSubscriptionCount();

  /**
   * Return the number of pending subscriptions. These are the subscriptions
   * which have been requested from the market data provider but for which
   * there has been no reply indicating success or failure.
   *
   * @return the number of pending subscriptions
   */

  int getPendingSubscriptionCount();

  /**
   * Return the number of removed subscriptions. These are the subscriptions
   * which were requested but were subsequently no longer required.
   *
   * @return the number of removed subscriptions
   */

  int getRemovedSubscriptionCount();

  /**
   * Return the number of active subscriptions. These are the subscriptions
   * which have been successfully fulfilled by the market data provider.
   *
   * @return the number of active subscriptions
   */

  int getActiveSubscriptionCount();

  /**
   * Retries all failed subscriptions.
   * 
   * @return the number of subscriptions being retried
   */
  int retryFailedSubscriptions();
  
  /**
   * Return the mapping (ticker -> SubscriptionStatus) of all failed subscriptions.
   * These are the subscriptions which were unable to be fulfilled by the market
   * data provider.
   * Not exposed as an attribute as there is likely to be some work to actually
   * calculate the required set.
   *
   * @return the mapping of all failed subscriptions.
   */
  Map<String, MarketDataManager.SubscriptionStatus> queryFailedSubscriptions();

  /**
   * Return the mapping (ticker -> SubscriptionStatus) of all pending subscriptions.
   * These are the subscriptions which have been requested from the market data
   * provider but for which there has been no reply indicating success or failure.
   * Not exposed as an attribute as there is likely to be some work to actually
   * calculate the required set.
   *
   * @return the mapping of all pending subscriptions.
   */
  Map<String, MarketDataManager.SubscriptionStatus> queryPendingSubscriptions();

  /**
   * Return the mapping (ticker -> SubscriptionStatus) of all removed subscriptions.
   * These are the subscriptions which were requested but were subsequently no
   * longer required.
   * Not exposed as an attribute as there is likely to be some work to actually
   * calculate the required set.
   *
   * @return the mapping of all removed subscriptions.
   */
  Map<String, MarketDataManager.SubscriptionStatus> queryRemovedSubscriptions();

  /**
   * Return the mapping (ticker -> SubscriptionStatus) of all active subscriptions.
   * These are the subscriptions which have been successfully fulfilled by the
   * market data provider.
   * Not exposed as an attribute as there is likely to be some work to actually
   * calculate the required set.
   *
   * @return the mapping of all active subscriptions.
   */
  Map<String, MarketDataManager.SubscriptionStatus> queryActiveSubscriptions();

  /**
   * Return the mapping (ticker -> SubscriptionStatus) of all subscriptions for
   * which the ticker matches the supplied value. The match will work on any part
   * of the ticker string so "AAPL" will match "AAPL." and also "AAPL/G4NHG.O".
   * If the ticker is null or empty, all subscriptions will be returned.
   *
   * @param ticker the ticker to use to find subscriptions.
   * @return the mapping of all matching subscriptions
   */
  Map<String, MarketDataManager.SubscriptionStatus> querySubscriptionState(String ticker);

  /**
   * Represents the current state of a market data subscription.
   */
  public enum SubscriptionState {
    /**
     * Subscriptions which has been requested from the market data provider but for
     * which there has been no reply indicating success or failure.
     */
    PENDING,
    /**
     * Subscription which has been successfully fulfilled by the market data provider.
     */
    ACTIVE,
    /**
     * Subscriptions which was unable to be fulfilled by the market data provider.
     */
    FAILED,
    /**
     * Subscription which was requested but was subsequently no longer required.
     */
    REMOVED
  }
  
}
