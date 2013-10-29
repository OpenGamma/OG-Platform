/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.expirycalc;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.model.irfutureoption.FutureOptionUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;

/**
 * Expiry calculator for IR futures.
 * <p>
* Provides Expiries of IR Future Options from ordinals (i.e. nth future after valuationDate).
* This Calculator looks for Serial (Monthly) expiries for the first 6, and then quarterly from then on,
* thus n=7 will be the first quarterly expiry after the 6th monthly one.
*/
public final class IRFutureAndFutureOptionExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {

  /** Name of the calculator */
  private static final String NAME = "IRFutureAndFutureOptionExpiryCalculator";
  /** Singleton. */
  private static final IRFutureAndFutureOptionExpiryCalculator INSTANCE = new IRFutureAndFutureOptionExpiryCalculator();
  /** Calendar for weekdays. */
  private static final Calendar WEEKDAYS = new MondayToFridayCalendar("MTWThF");

  /**
   * Gets the singleton instance.
   * 
   * @return the instance, not null
   */
  public static IRFutureAndFutureOptionExpiryCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Restricted constructor.
   */
  private IRFutureAndFutureOptionExpiryCalculator() {
  }

  //-------------------------------------------------------------------------
  @Override
  public LocalDate getExpiryDate(int n, LocalDate today, Calendar holidayCalendar) {
    return FutureOptionUtils.getIRFutureOptionWithSerialOptionsExpiry(n, today, holidayCalendar);
  }

  @Override
  public LocalDate getExpiryMonth(int n, LocalDate today) {
    return FutureOptionUtils.getIRFutureOptionWithSerialOptionsExpiry(n, today, WEEKDAYS);
  }

  @Override
  public String getName() {
    return NAME;
  }

}
