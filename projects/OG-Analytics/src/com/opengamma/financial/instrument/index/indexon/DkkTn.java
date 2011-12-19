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
 * Class describing the DKK Tomorrow/Next interest rate index.
 */
public class DkkTn extends IndexON {

  /**
   * Constructor.
   * @param calendar The DKK calendar.
   */
  public DkkTn(Calendar calendar) {
    super("DKK T/N", Currency.DKK, DayCountFactory.INSTANCE.getDayCount("Actual/360"), 1, calendar);
  }

}
