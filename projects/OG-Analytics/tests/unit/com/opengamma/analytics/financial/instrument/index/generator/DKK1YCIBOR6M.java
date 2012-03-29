/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index.generator;

import javax.time.calendar.Period;

import com.opengamma.analytics.financial.instrument.index.GeneratorSwap;
import com.opengamma.analytics.financial.instrument.index.iborindex.IndexIborTestsMaster;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * Swap generator for the DKK Annual 30/360 vs Cibor 6M.
 */
public class DKK1YCIBOR6M extends GeneratorSwap {

  /**
   * Constructor.
   * @param calendar A DKK calendar.
   */
  public DKK1YCIBOR6M(Calendar calendar) {
    super(Period.ofMonths(12), DayCountFactory.INSTANCE.getDayCount("30/360"), IndexIborTestsMaster.getInstance().getIndex("DKKCIBOR6M", calendar));
  }

}
