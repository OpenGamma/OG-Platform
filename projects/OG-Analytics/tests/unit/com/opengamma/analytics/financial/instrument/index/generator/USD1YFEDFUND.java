/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index.generator;

import javax.time.calendar.Period;

import com.opengamma.analytics.financial.instrument.index.GeneratorOIS;
import com.opengamma.analytics.financial.instrument.index.indexon.FEDFUND;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * OIS generator for USD annual payments with Federal Funds rates.
 */
public class USD1YFEDFUND extends GeneratorOIS {

  /**
   * Constructor of the OIS generator USD annual payment for Fed Funds.
   * @param calendar A US calendar.
   */
  public USD1YFEDFUND(final Calendar calendar) {
    super("USD1YFEDFUND", new FEDFUND(calendar), Period.ofMonths(12), DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE
        .getBusinessDayConvention("Modified Following"), true, 2);
  }

}
