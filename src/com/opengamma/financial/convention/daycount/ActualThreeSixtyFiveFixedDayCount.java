/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.time.DateUtil;

/**
 * Definition for the Actual/365 (Fixed) day count convention. The day count
 * fraction is defined as the actual number of days in the period divided by
 * 365.
 * 
 * <p>
 * This convention is also known as "Act/365 (Fixed)", "A/365 (Fixed)" or
 * "A/365F".
 * 
 * @author emcleod
 */

public class ActualThreeSixtyFiveFixedDayCount implements DayCount {

  @Override
  public double getBasis(final ZonedDateTime date) {
    return 365;
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    return DateUtil.getDaysBetween(firstDate, false, secondDate, true) / getBasis(firstDate);
  }

}
