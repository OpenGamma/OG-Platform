/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

/**
 * Convention for calculating the day count.
 */
public interface DayCount {

  /**
   * Gets the day count between the specified dates.
   * <p>
   * Given two dates, this method returns the fraction of a year between these dates
   * according to the convention.
   * 
   * @param firstDate  the earlier date, not null
   * @param secondDate  the later date, not null
   * @return the day count fraction
   */
  double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate);

  double getAccruedInterest(final ZonedDateTime previousCouponDate, final ZonedDateTime date, final ZonedDateTime nextCouponDate, final double coupon, final int paymentsPerYear);

  /**
   * Gets the name of the convention.
   * @return the name, not null
   */
  String getConventionName();

}
