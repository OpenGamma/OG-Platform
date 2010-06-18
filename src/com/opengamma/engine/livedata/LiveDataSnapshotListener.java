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
   * @param requirement Not null
   */
  void valueChanged(ValueRequirement requirement);

}
