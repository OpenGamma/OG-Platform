/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.var;

import com.opengamma.analytics.financial.schedule.ScheduleCalculatorFactory;

/**
 * Scaling functions to resolve the horizon of a calculation, specified in 'business days',
 * to the sampling period, specified as a Period, e.g. 'Weekly'.
 */
public class VaRFunctionUtils {

  public static double getPeriodsPerYear(final String scheduleCalculatorName) {
    if (scheduleCalculatorName.equals(ScheduleCalculatorFactory.DAILY)) {
      return 252;
    }
    if (scheduleCalculatorName.equals(ScheduleCalculatorFactory.WEEKLY)) {
      return 52;
    }
    if (scheduleCalculatorName.equals(ScheduleCalculatorFactory.MONTHLY)) {
      return 12;
    }
    if (scheduleCalculatorName.equals(ScheduleCalculatorFactory.ANNUAL)) {
      return 1;
    }
    //TODO include all of the available calculators
    throw new IllegalArgumentException("Could not get number of periods per year for " + scheduleCalculatorName);
  }

  public static double getBusinessDaysPerPeriod(final String scheduleCalculatorName) {
    if (scheduleCalculatorName.equals(ScheduleCalculatorFactory.DAILY)) {
      return 1;
    }
    if (scheduleCalculatorName.equals(ScheduleCalculatorFactory.WEEKLY)) {
      return 5;
    }
    if (scheduleCalculatorName.equals(ScheduleCalculatorFactory.MONTHLY)) {
      return 21;
    }
    if (scheduleCalculatorName.equals(ScheduleCalculatorFactory.ANNUAL)) {
      return 252;
    }
    //TODO include all of the available calculators
    throw new IllegalArgumentException("Could not get number of periods per year for " + scheduleCalculatorName);
  }

  public static double getPeriodsPerBusinessDay(final String scheduleCalculatorName) {
    final double inverse = getBusinessDaysPerPeriod(scheduleCalculatorName);
    return 1.0 / inverse;
  }
}
