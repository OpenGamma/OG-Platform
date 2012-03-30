/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index.generator;

import javax.time.calendar.Period;

import com.opengamma.analytics.financial.instrument.index.GeneratorOIS;
import com.opengamma.analytics.financial.instrument.index.indexon.EONIA;
import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;

/**
 * OIS generator for EUR annual payments with EONIA rates.
 */
public class EUR1YEONIA extends GeneratorOIS {

  /**
   * Constructor of the OIS generator EUR annual payment for Eonia.
   * @param calendar A EUR calendar.
   */
  public EUR1YEONIA(final Calendar calendar) {
    super("EUR1YEONIA", new EONIA(calendar), Period.ofMonths(12), DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE
        .getBusinessDayConvention("Modified Following"), true, 2, 2);
  }

}
