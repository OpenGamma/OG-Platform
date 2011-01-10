/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.util.PublicSPI;

/**
 * A listener interface for live data snapshots
 */
@PublicSPI
public interface LiveDataSnapshotListener {
  
  /**
   * Notifies of a successful live data subscription. 
   * 
   * @param requirement the requirement that was successfully subscribed to, not {@code null}
   */
  void subscriptionSucceeded(ValueRequirement requirement);
  
  /**
   * Notifies of a failed live data subscription.
   * 
   * @param requirement the requirement that could not be subscribed to, not {@code null}
   * @param msg the error message, not {@code null}
   */
  void subscriptionFailed(ValueRequirement requirement, String msg);
  
  /**
   * Notifies of a terminated live data subscription.
   * 
   * @param requirement the requirement that is no longer subscribed to, not {@code null}
   */
  void subscriptionStopped(ValueRequirement requirement);
  
  /**
   * Notifies of a live data value that has changed.
   * <p>
   * This method must execute quickly and not block. It will be called from within the live data
   * client thread. If the execution of this method is slow, for example
   * because it uses external resources such as files or the network
   * in some way, or executes a complicated algorithm, then 
   * market data updates for other market data lines will not be received
   * until this method returns. Thus, if you need to execute a
   * non-trivial operation when you receive new market data, offload to a different
   * thread (e.g. an executor service) to avoid blocking the live data client.
   * 
   * @param requirement the requirement that has changed
   */
  void valueChanged(ValueRequirement requirement);

}
