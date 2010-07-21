/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.livedata;

import com.opengamma.engine.value.ValueRequirement;

/**
 *  
 */
public interface LiveDataSnapshotListener {
  
  /**
   * 
   * @param requirement Not null
   */
  void subscriptionSucceeded(ValueRequirement requirement);
  
  /**
   * 
   * @param requirement Not null
   * @param msg Not null
   */
  void subscriptionFailed(ValueRequirement requirement, String msg);
  
  /**
   * @param requirement Not null
   */
  void subscriptionStopped(ValueRequirement requirement);
  
  /**
   * Important implementation note to listener implementors:
   * THIS METHOD MUST EXECUTE QUICKLY. The way the Live Data Client works,
   * several market data lines may be handled by the same Live Data Client
   * thread. If the execution of this method is slow, for example
   * because it uses external resources such as files or the network
   * in some way, or executes a complicated algorithm, then 
   * market data updates for OTHER market data lines will not received
   * until this method returns. Thus, if you need to execute a
   * non-trivial operation when you receive new market data, 
   * do it in a new thread.      

   * @param requirement Not null
   */
  void valueChanged(ValueRequirement requirement);

}
