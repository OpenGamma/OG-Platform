/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.client;

/**
 * Enumerates the result modes in which a {@link ViewClient} can operate.
 */
public enum ViewResultMode {

  /**
   * Only full results will be propagated to the listener.
   */
  FULL_ONLY,
  
  /**
   * Only delta results will be propagated to the listener.
   */
  DELTA_ONLY,
  
  /**
   * A full result will be propagated on the first cycle, followed by delta results.
   */
  FULL_THEN_DELTA,
  
  /**
   * Both full and delta results will be propagated to the listener.
   */
  BOTH,

  /**
   * Neither full nor delta results will be propagated to the listener.
   */
  NONE
}
