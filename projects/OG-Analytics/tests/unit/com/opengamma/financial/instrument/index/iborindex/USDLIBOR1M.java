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
 * Class describing the USD LIBOR 1M index.
 */
public final class USDLIBOR1M extends IborIndex {

  /**
   * Constructor.
   * @param calendar A USD calendar.
   */
  public USDLIBOR1M(Calendar calendar) {
    super(Currency.USD, Period.ofMonths(1), 2, calendar, DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"),
        true, "USDLIBOR1M");
  }

}
