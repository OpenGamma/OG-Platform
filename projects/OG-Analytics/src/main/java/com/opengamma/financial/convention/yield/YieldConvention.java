/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.yield;

/**
 * Convention for yields.
 */
public interface YieldConvention {
  // TODO: supply whatever is needed to do a proper calculation

  /**
   * Gets the name of the convention.
   * 
   * @return the name, not null
   */
  String getConventionName();

}
