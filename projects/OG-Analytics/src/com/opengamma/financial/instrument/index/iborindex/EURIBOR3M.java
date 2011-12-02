/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index.iborindex;

import javax.time.calendar.Period;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IborIndex;
import com.opengamma.util.money.Currency;

/**
 * Class describing the EURIBOR 3M index.
 */
public final class EURIBOR3M extends IborIndex {

  /**
   * Constructor.
   * @param calendar A EUR calendar.
   */
  public EURIBOR3M(Calendar calendar) {
    super(Currency.EUR, Period.ofMonths(3), 2, calendar, DayCountFactory.INSTANCE.getDayCount("Actual/360"), 
        BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), true);
  }
}
