/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Converts dates to 'Analytic Time'. The latter are stored as doubles, 
 * and typically represent the fraction of years between some date and the current one.
 * TODO Discuss whether this makes more sense as a singleton pattern as, say, PresentValueCalculator  
 */
public abstract class TimeCalculator {

  public static double getTimeBetween(ZonedDateTime date1, ZonedDateTime date2) {
    Validate.notNull(date1, "date1");
    Validate.notNull(date1, "date2");
    
    // TODO Confirm the following behaviour, allowing negative values of  time, is desired
    boolean timeIsNegative = date1.isAfter(date2); // date1 >= date2
    
    // TODO Decide on preferred functional form of t := date2 - date1
    // I am not a fan of the current setup because it is difficult to invert: dt2 = dt1 + t
    final DayCount actAct = DayCountFactory.INSTANCE.getDayCount("Actual/Actual ISDA");
    
    if(!timeIsNegative) {
      final double time = actAct.getDayCountFraction(date1, date2);
      return time;
    }
    else {
      return -1.0 * actAct.getDayCountFraction(date2, date1);
    }
  }
}
