/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value;

import com.opengamma.util.PublicAPI;

/**
 * Standard names used to refer to, and to scope, groups of value properties.
 */
@PublicAPI
public final class ValuePropertyGroups {

  /**
   * Hidden constructor.
   */
  private ValuePropertyGroups() {
  }
  
  /**
   * Group of properties relating to curves.
   */
  public static final String CURVE = "Curve";
  /**
   * Group of properties relating to time-series.
   */
  public static final String TIME_SERIES = "TimeSeries";
  
}
