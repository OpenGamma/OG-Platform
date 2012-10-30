/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.Collection;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.PublicSPI;

/**
 * A listener interface for changes that affect live data snapshots.
 */
@PublicSPI
public interface MarketDataListener {
  
  /**
   * Notifies of a successful live data subscription. 
   * 
   * @param requirement  the requirement that was successfully subscribed to, not null
   */
  void subscriptionSucceeded(ValueRequirement requirement);
  
  /**
   * Notifies of a failed live data subscription.
   * 
   * @param requirement  the requirement that could not be subscribed to, not null
   * @param msg  the error message, not null
   */
  void subscriptionFailed(ValueRequirement requirement, String msg);
  
  /**
   * Notifies of a terminated live data subscription.
   * 
   * @param requirement  the requirement that is no longer subscribed to, not null
   */
  void subscriptionStopped(ValueRequirement requirement);
  
  /**
   * Notifies the listener that one or more market data values have changed.
   * <p>
   * This method must execute quickly and not block; it may be called from within a market data receiver thread.
   * 
   * @param requirements  the requirements whose values have changed, not null
   */
  void valuesChanged(Collection<ValueRequirement> requirements);

}
