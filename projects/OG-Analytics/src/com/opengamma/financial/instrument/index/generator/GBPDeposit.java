/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.instrument.index.generator;

import com.opengamma.financial.convention.businessday.BusinessDayConventionFactory;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.instrument.index.GeneratorDeposit;
import com.opengamma.util.money.Currency;

/**
 * Deposit generator with the standard GBP conventions.
 */
public class GBPDeposit extends GeneratorDeposit {

  /**
   * Constructor.
   * @param calendar A GBP calendar.
   */
  public GBPDeposit(final Calendar calendar) {
    super(Currency.GBP, calendar, 0, DayCountFactory.INSTANCE.getDayCount("Actual/365"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), true);
  }

}
