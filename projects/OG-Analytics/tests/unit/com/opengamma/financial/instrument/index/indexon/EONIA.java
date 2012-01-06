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
 * Class describing the EONIA interest rate index.
 */
public class EONIA extends IndexON {

  /**
   * Constructor.
   * @param calendar A EUR calendar.
   */
  public EONIA(Calendar calendar) {
    super("EONIA", Currency.EUR, DayCountFactory.INSTANCE.getDayCount("Actual/360"), 0, calendar);
  }

}
