/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.trigger;

/**
 * Enumerates the levels of eligibility to perform a view cycle.
 */
public enum ViewCycleEligibility {

  /**
   * Indicates that a view cycle is eligible to be performed if necessary.
   */
  ELIGIBLE,
  
  /**
   * Indicates that no view cycle should be performed.
   */
  PREVENT,
  
  /**
   * Indicates that a view cycle should be performed.
   */
  FORCE;
  
  //-------------------------------------------------------------------------
  public static ViewCycleEligibility merge(ViewCycleEligibility a, ViewCycleEligibility b) {
    if (a == null) {
      return b;
    }
    if (b == null) {
      return a;
    }
    // Declared in increasing order of importance
    return values()[Math.max(a.ordinal(), b.ordinal())];
  }
  
}
