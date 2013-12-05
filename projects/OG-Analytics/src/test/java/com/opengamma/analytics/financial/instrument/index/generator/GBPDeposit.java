/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.instrument.index.generator;

import com.opengamma.analytics.financial.instrument.index.GeneratorDeposit;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.daycount.DayCounts;
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
    super("GBP Deposit", Currency.GBP, calendar, 0, DayCounts.ACT_365, BusinessDayConventions.MODIFIED_FOLLOWING, true);
  }

}
