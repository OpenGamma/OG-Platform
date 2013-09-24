/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import com.opengamma.util.ArgumentChecker;

/**
 * Immutable class holding data about a particular market data subscription.
 */
public class SubscriptionInfo {

  /**
   * The number of subscribers for this piece of market data.
   */
  private final int _subscriberCount;
  /**
   * The current state of the market data, not null.
   */
  private final String _state;
  /**
   * The value(s) held for this subscription, may be null.
   */
  private final String _currentValue;

  /**
   * Create the subscription information.
   *
   * @param subscriberCount the number of subscribers for this piece of market data
   * @param state the current state of the market data, not null
   * @param currentValue the value(s) held for this subscription, may be null
   */
  public SubscriptionInfo(int subscriberCount, String state, Object currentValue) {
    ArgumentChecker.notNull(state, "state");
    _subscriberCount = subscriberCount;
    _state = state;
    _currentValue = currentValue == null ? null : currentValue.toString();
  }

  /**
   * Returns the number of subscribers for this piece of market data.
   *
   * @return the number of subscribers
   */
  public int getSubscriberCount() {
    return _subscriberCount;
  }

  /**
   * Returns the current state of the market data.
   *
   * @return the current state
   */
  public String getState() {
    return _state;
  }

  /**
   * Returns the value(s) held for this subscription.
   *
   * @return the values held for the subscription, may be null
   */
  public String getCurrentValue() {
    return _currentValue;
  }
}
