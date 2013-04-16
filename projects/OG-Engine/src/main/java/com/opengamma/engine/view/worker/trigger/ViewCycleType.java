/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker.trigger;

/**
 * Enumerates the view cycle types which can be triggered.
 */
public enum ViewCycleType {

  /**
   * Indicates a preference to perform a delta cycle where unchanged calculations in the dependency graph are reused.
   */
  DELTA,
  
  /**
   * Indicates a preference to perform a full cycle.
   */
  FULL;
  
  //-------------------------------------------------------------------------
  public static ViewCycleType merge(ViewCycleType a, ViewCycleType b) {
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
