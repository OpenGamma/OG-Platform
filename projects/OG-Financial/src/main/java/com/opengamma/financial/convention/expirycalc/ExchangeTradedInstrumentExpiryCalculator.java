/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.expirycalc;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.id.ExternalScheme;

/**
 * Calculates the expiry date of the nth option for an option type and an exchange.
 * <p>
 * For example, IMM future options use the HMUZ schedule and expire two business days
 * before the third Wednesday of the month, so on 1/1/2012, the expiry date of the
 * third option is the 17th of September.
 */
public interface ExchangeTradedInstrumentExpiryCalculator {

  /**
   * The scheme for the external ids of expiry calculators.
   */
  ExternalScheme SCHEME = ExternalScheme.of("EXPIRY_CONVENTION");

  /**
   * Gets the expiry date of a future.
   * 
   * @param n  the n'th expiry date after today, greater than zero
   * @param today  the valuation date, not null
   * @param holidayCalendar  the holiday calendar, not null
   * @return the expiry date, not null
   */
  LocalDate getExpiryDate(int n, LocalDate today, Calendar holidayCalendar);

  /**
   * Gets the expiry month of a future.
   * <p>
   * Given a LocalDate representing the valuation date and an integer representing
   * the n'th expiry after that date, returns a date in the expiry month.
   * 
   * @param n  the n'th expiry date after today, greater than zero
   * @param today  the valuation date, not null
   * @return a date in the expiry month, not null
   */
  LocalDate getExpiryMonth(int n, LocalDate today);

  /**
   * Gets the name of the calculator.
   * 
   * @return the name, not null
   */
  String getName();

}
