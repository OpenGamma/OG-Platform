/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index.indexon;

import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.money.Currency;

/**
 * Class describing the RBA ON (Reserve Bank of Australia - Overnight) interest rate index.
 */
public class RBAON extends IndexON {

  /**
   * Constructor.
   * @param calendar A AUD calendar.
   */
  public RBAON(Calendar calendar) {
    super("RBA ON", Currency.AUD, DayCountFactory.INSTANCE.getDayCount("Actual/365"), 0, calendar);
  }

}
