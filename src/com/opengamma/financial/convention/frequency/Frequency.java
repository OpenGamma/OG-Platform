/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.frequency;

/**
 * Convention for frequency.
 */
public interface Frequency {
  // TODO: hold some proper data to get the dates for the cash flows
  // TODO: consider using PeriodUnit

  /**
   * Gets the name of the convention.
   * @return the name, not null
   */
  String getConventionName();
  int getPeriodsPerYear();

}
