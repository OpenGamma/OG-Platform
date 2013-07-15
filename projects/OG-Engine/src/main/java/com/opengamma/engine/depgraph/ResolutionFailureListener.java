/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

/**
 * Callback interface for receiving {@link ResolutionFailure} notifications.
 */
public interface ResolutionFailureListener {
  
  /**
   * Receive resolution failure notification.
   * @param resolutionFailure resolution failure instance
   */
  void notifyFailure(ResolutionFailure resolutionFailure);
  
}
