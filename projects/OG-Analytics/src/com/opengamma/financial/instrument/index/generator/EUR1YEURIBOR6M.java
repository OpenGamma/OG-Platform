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
import com.opengamma.financial.instrument.index.iborindex.EURIBOR6M;

/**
 * Swap generator for the EUR Annual 30/360 vs Euribor 6M.
 */
public class EUR1YEURIBOR6M extends SwapGenerator {

  /**
   * Constructor.
   * @param calendar A EUR calendar.
   */
  public EUR1YEURIBOR6M(Calendar calendar) {
    super(Period.ofMonths(12), DayCountFactory.INSTANCE.getDayCount("30/360"), new EURIBOR6M(calendar));
  }

}
