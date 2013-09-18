/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

public class SubscriptionInfo {

  private final int _subscriberCount;
  private final String _state;
  private final String _currentValue;

  public SubscriptionInfo(int subscriberCount, String state, Object currentValue) {
    _subscriberCount = subscriberCount;
    _state = state;
    _currentValue = currentValue == null ? null : currentValue.toString();
  }

  public int getSubscriberCount() {
    return _subscriberCount;
  }

  public String getState() {
    return _state;
  }

  public String getCurrentValue() {
    return _currentValue;
  }
}
