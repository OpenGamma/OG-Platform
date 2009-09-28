/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.util.time.DateUtil;

/**
 * Definition for the Actual/360 day count convention. The day count fraction is
 * the actual number of days in the calculation period divided by 360.
 * <p>
 * This convention is also known as "Act/360" or "A/360".
 * 
 * @author emcleod
 */

public class ActualThreeSixtyDayCount implements DayCount {

  @Override
  public double getBasis(final ZonedDateTime date) {
    return 360;
  }

  @Override
  public double getDayCountFraction(final ZonedDateTime firstDate, final ZonedDateTime secondDate) {
    return DateUtil.getDaysBetween(firstDate, false, secondDate, true) / getBasis(firstDate);
  }

}
