/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdayieldcurve;

import java.util.Calendar;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.PeriodFrequency;

/**
 * 
 */
public class ISDAYieldCurve {

  public ISDAYieldCurve(
      final ZonedDateTime baseDate,
      final ZonedDateTime[] instrumentMaturities,
      final ISDAInstrumentTypes instrumentTypes,
      final double[] instrumentRates,
      final int spotDays,
      final DayCount moneyMarketDaycountConvention,
      final DayCount swapDaycountDaycountConvention,
      final DayCount floatDaycountConvention,
      final PeriodFrequency swapInterval,
      final PeriodFrequency floatInterval,
      final BusinessDayConvention badDayConvention,
      final Calendar holidayCalendar) {

  }

}
