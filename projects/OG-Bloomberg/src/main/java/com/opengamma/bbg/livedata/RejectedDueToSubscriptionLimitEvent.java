/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import org.threeten.bp.Instant;

/**
 * Records information about a rejection of a subscription due to limit.
 */
public class RejectedDueToSubscriptionLimitEvent {

  private final long _subscriptionLimitInEffect;
  private final int _requestedSubscriptions;
  private final int _afterSubscriptionCount;
  private Instant _instant;

  public RejectedDueToSubscriptionLimitEvent(long subscriptionLimitInEffect, int requestedSubscriptions, int afterSubscriptionCount) {
    _instant = Instant.now();
    _subscriptionLimitInEffect = subscriptionLimitInEffect;
    _requestedSubscriptions = requestedSubscriptions;
    _afterSubscriptionCount = afterSubscriptionCount;
  }

  /**
   * Gets the subscriptionLimitInEffect at the time of the event
   * @return the subscriptionLimitInEffect
   */
  public long getSubscriptionLimitInEffect() {
    return _subscriptionLimitInEffect;
  }

  /**
   * Gets the number of requestedSubscriptions.
   * @return the requestedSubscriptions
   */
  public int getRequestedSubscriptions() {
    return _requestedSubscriptions;
  }

  /**
   * Gets the number of subscriptions which would have occurred after this request (if it hadn't been rejected).
   * @return the afterSubscriptionCount
   */
  public int getAfterSubscriptionCount() {
    return _afterSubscriptionCount;
  }

  /**
   * Gets the instant at which the event occured.
   * @return the instant
   */
  public Instant getInstant() {
    return _instant;
  }

}
