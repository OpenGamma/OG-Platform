/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.model.irfutureoption.FutureOptionUtils;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;

/**
* Provides Expiries of IR Future Options from ordinals (i.e. nth future after valuationDate).
* This Calculator looks for Serial (Monthly) expiries for the first 6, and then quarterly from then on,
* thus n=7 will be the first quarterly expiry after the 6th monthly one.
*/
public class IRFutureAndFutureOptionExpiryCalculator implements ExchangeTradedInstrumentExpiryCalculator {

  /** Name of the calculator */
  private static final String NAME = "IRFutureAndFutureOptionExpiryCalculator";
  private static final Calendar WEEKDAYS = new MondayToFridayCalendar("MTWThF");
  
  private static final IRFutureAndFutureOptionExpiryCalculator INSTANCE = new IRFutureAndFutureOptionExpiryCalculator();
  /** @return Static instance */
  public static IRFutureAndFutureOptionExpiryCalculator getInstance() {
    return INSTANCE;
  }
  
  @Override
  /** @return n'th future expiry as described above, adjusted for holidays */
  public LocalDate getExpiryDate(int n, LocalDate today, Calendar holidayCalendar) {
    return FutureOptionUtils.getIRFutureOptionWithSerialOptionsExpiry(n, today, holidayCalendar);
  }

  @Override
  /** @return n'th future expiry as described above, un-adjusted for holidays */
  public LocalDate getExpiryMonth(int n, LocalDate today) {
    return FutureOptionUtils.getIRFutureOptionWithSerialOptionsExpiry(n, today, WEEKDAYS);
  }

  @Override
  /** Name of the calculator */
  public String getName() {
    return NAME;
  }

}
