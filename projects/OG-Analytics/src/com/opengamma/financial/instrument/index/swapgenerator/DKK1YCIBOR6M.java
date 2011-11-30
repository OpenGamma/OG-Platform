/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index.swapgenerator;

import javax.time.calendar.Period;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.SwapGenerator;
import com.opengamma.financial.instrument.index.iborindex.DKKCIBOR6M;

/**
 * Swap generator for the DKK Annual 30/360 vs Cibor 6M.
 */
public class DKK1YCIBOR6M extends SwapGenerator {

  /**
   * Constructor.
   * @param calendar A DKK calendar.
   */
  public DKK1YCIBOR6M(Calendar calendar) {
    super(Period.ofMonths(12), DayCountFactory.INSTANCE.getDayCount("30/360"), new DKKCIBOR6M(calendar));
  }

}
