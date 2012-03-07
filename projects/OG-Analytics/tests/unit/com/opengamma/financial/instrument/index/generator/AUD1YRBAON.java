/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index.generator;

import javax.time.calendar.Period;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.GeneratorOIS;
import com.opengamma.financial.instrument.index.indexon.EONIA;

/**
 * OIS generator for AUD annual payments with RBA rates.
 */
public class AUD1YRBAON extends GeneratorOIS {

  /**
   * Constructor of the OIS generator AUD annual payment for RBA ON rates.
   * @param calendar A AUD calendar.
   */
  public AUD1YRBAON(final Calendar calendar) {
    super("AUD1YRBAON", new EONIA(calendar), Period.ofMonths(12), DayCountFactory.INSTANCE.getDayCount("Actual/365"), BusinessDayConventionFactory.INSTANCE
        .getBusinessDayConvention("Modified Following"), true, 2, 1);
    // TODO: Check the spot lag.
  }

}
