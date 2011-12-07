/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index.indexon;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.IndexON;
import com.opengamma.util.money.Currency;

/**
 * Class describing the SONIA interest rate index.
 */
public class SONIA extends IndexON {

  /**
   * Constructor.
   * @param calendar A GBP calendar.
   */
  public SONIA(Calendar calendar) {
    super("SONIA", Currency.GBP, DayCountFactory.INSTANCE.getDayCount("Actual/365"), 0, calendar);
  }

}
