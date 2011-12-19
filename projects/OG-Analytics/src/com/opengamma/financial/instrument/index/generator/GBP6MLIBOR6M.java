/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index.generator;

import javax.time.calendar.Period;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.SwapGenerator;
import com.opengamma.financial.instrument.index.iborindex.GBPLIBOR6M;

/**
 * Swap generator for the GBP Semi-annual ACT/365 vs Libor 6M.
 */
public class GBP6MLIBOR6M extends SwapGenerator {

  /**
   * Constructor.
   * @param calendar A GBP calendar.
   */
  public GBP6MLIBOR6M(Calendar calendar) {
    super(Period.ofMonths(6), DayCountFactory.INSTANCE.getDayCount("ACT/365"), new GBPLIBOR6M(calendar));
  }

}
