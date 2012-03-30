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
 * Swap generator for the GBP Semi-annual ACT/365 vs Libor 6M.
 */
public class GBP6MLIBOR6M extends GeneratorSwap {

  /**
   * Constructor.
   * @param calendar A GBP calendar.
   */
  public GBP6MLIBOR6M(Calendar calendar) {
    super(Period.ofMonths(6), DayCountFactory.INSTANCE.getDayCount("ACT/365"), IndexIborTestsMaster.getInstance().getIndex("GBPLIBOR6M", calendar));
  }

}
