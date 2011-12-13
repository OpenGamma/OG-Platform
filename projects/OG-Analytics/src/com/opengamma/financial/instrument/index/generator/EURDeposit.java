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
 * Deposit generator with the standard EUR conventions.
 */
public class EURDeposit extends GeneratorDeposit {

  /**
   * Constructor.
   * @param calendar A EUR calendar.
   */
  public EURDeposit(final Calendar calendar) {
    super(Currency.EUR, calendar, 2, DayCountFactory.INSTANCE.getDayCount("Actual/360"), BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention("Modified Following"), true);
  }

}
