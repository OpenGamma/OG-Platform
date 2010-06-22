/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swap;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.analytics.model.schedule.ScheduleCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.swap.SwapLeg;
import com.opengamma.financial.security.swap.SwapSecurity;

/**
 * 
 *
 */
public class SwapScheduleCalculator {

  public static double[] getPayLegPaymentTimes(final SwapSecurity security, final Calendar calendar) {
    Validate.notNull(security);
    Validate.notNull(calendar);
    return getPaymentTimes(security.getEffectiveDate(), security.getMaturityDate(), security.getPayLeg(), calendar);
  }

  public static double[] getReceiveLegPaymentTimes(final SwapSecurity security, final Calendar calendar) {
    Validate.notNull(security);
    Validate.notNull(calendar);
    return getPaymentTimes(security.getEffectiveDate(), security.getMaturityDate(), security.getReceiveLeg(), calendar);
  }

  //TODO include accrual date as well
  public static double[] getPaymentTimes(final ZonedDateTime effectiveDate, final ZonedDateTime maturityDate,
      final SwapLeg leg, final Calendar calendar) {
    Validate.notNull(effectiveDate);
    Validate.notNull(maturityDate);
    Validate.notNull(leg);
    Validate.notNull(calendar);
    final Frequency payFrequency = leg.getFrequency();
    final ZonedDateTime[] adjusted = ScheduleCalculator
        .getAdjustedDateSchedule(ScheduleCalculator
            .getUnadjustedDateSchedule(effectiveDate, maturityDate, payFrequency), leg.getBusinessDayConvention(),
            calendar);
    return ScheduleCalculator.getTimes(adjusted, leg.getDaycount());
  }

}
