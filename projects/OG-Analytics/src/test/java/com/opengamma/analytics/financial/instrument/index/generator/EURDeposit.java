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
 * Deposit generator with the standard EUR conventions.
 */
public class EURDeposit extends GeneratorDeposit {

  /**
   * Constructor.
   * @param calendar A EUR calendar.
   */
  public EURDeposit(final Calendar calendar) {
    super("EUR Deposit", Currency.EUR, calendar, 2, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING, true);
  }

}
