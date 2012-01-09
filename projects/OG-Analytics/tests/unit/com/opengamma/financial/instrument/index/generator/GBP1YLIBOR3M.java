/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index.generator;

import javax.time.calendar.Period;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.GeneratorSwap;
import com.opengamma.financial.instrument.index.iborindex.GBPLIBOR3M;

/**
 * Swap generator for the GBP Annual ACT/365 vs Libor 3M.
 */
public class GBP1YLIBOR3M extends GeneratorSwap {

  /**
   * Constructor.
   * @param calendar A GBP calendar.
   */
  public GBP1YLIBOR3M(Calendar calendar) {
    super(Period.ofMonths(12), DayCountFactory.INSTANCE.getDayCount("ACT/365"), new GBPLIBOR3M(calendar));
  }

}
