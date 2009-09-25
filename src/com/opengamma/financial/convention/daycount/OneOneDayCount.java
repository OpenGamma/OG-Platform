/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

/**
 * Definition for the 1/1 day count convention. The 1/1 day count always returns
 * one as the fraction of a year.
 * 
 * @author emcleod
 */

public class OneOneDayCount implements DayCount {

  /**
   * 
   * @returns Year fraction is always one.
   */
  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    return 1;
  }

  /**
   * 
   * @throws UnsupportedOperationException
   */
  @Override
  public double getBasis(final ZonedDateTime date) {
    throw new UnsupportedOperationException();
  }

}
