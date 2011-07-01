/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata;

import java.util.concurrent.ExecutorService;

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
   * @param requirement  the requirement that was successfully subscribed to, not {@code null}
   */
  void subscriptionSucceeded(ValueRequirement requirement);
  
  /**
   * Notifies of a failed live data subscription.
   * 
   * @param requirement  the requirement that could not be subscribed to, not {@code null}
   * @param msg  the error message, not {@code null}
   */
  void subscriptionFailed(ValueRequirement requirement, String msg);
  
  /**
   * Notifies of a terminated live data subscription.
   * 
   * @param requirement  the requirement that is no longer subscribed to, not {@code null}
   */
  void subscriptionStopped(ValueRequirement requirement);
  
  /**
   * Notifies of a live data value that has changed.
   * <p>
   * This method must execute quickly and not block. It will be called from within the live data client thread. If the
   * execution of this method is slow, for example because it uses external resources such as files or the network in
   * some way, or executes a complicated algorithm, then market data updates for other market data lines will not be
   * received until this method returns. Thus, if you need to execute a non-trivial operation when you receive new 
   * market data, offload this operation to a different thread (e.g. using an {@link ExecutorService}) to avoid
   * blocking the live data client.
   *  
   * @param requirement  the requirement whose value has changed, not {@code null}
   */
  void valueChanged(ValueRequirement requirement);

}
