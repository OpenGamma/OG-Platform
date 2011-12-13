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
import com.opengamma.financial.instrument.index.iborindex.USDLIBOR3M;

/**
 * Swap generator for the USD Annual ACT/360 vs Libor 3M.
 */
public class USD1YLIBOR3M extends SwapGenerator {

  /**
   * Constructor.
   * @param calendar A USD calendar.
   */
  public USD1YLIBOR3M(Calendar calendar) {
    super(Period.ofMonths(12), DayCountFactory.INSTANCE.getDayCount("ACT/360"), new USDLIBOR3M(calendar));
  }

}
