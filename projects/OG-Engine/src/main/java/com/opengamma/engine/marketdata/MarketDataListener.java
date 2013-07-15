/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Collection;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.PublicSPI;

/**
 * A listener interface for changes that affect live data snapshots.
 */
@PublicSPI
public interface MarketDataListener {

  /**
   * Notifies of successful live data subscriptions.
   * 
   * @param specifications the market data that was successfully subscribed to, not null and not containing nulls
   */
  void subscriptionsSucceeded(Collection<ValueSpecification> specifications);

  /**
   * Notifies of a failed live data subscription.
   * 
   * @param specification the market data that could not be subscribed to, not null
   * @param msg the error message, not null
   */
  void subscriptionFailed(ValueSpecification specification, String msg);

  /**
   * Notifies of a terminated live data subscription.
   * 
   * @param specification the market data that is no longer subscribed to, not null
   */
  void subscriptionStopped(ValueSpecification specification);

  /**
   * Notifies the listener that one or more market data values have changed.
   * <p>
   * This method must execute quickly and not block; it may be called from within a market data receiver thread.
   * 
   * @param specifications the specifications whose values have changed, not null
   */
  void valuesChanged(Collection<ValueSpecification> specifications);

}
