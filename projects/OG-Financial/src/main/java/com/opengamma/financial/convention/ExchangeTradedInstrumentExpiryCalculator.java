/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.id.ExternalScheme;

/**
 * Calculates the expiry date of the nth option for an option type and an exchange. For example, IMM future options use the HMUZ schedule and expire two
 * business days before the third Wednesday of the month, so on 1/1/2012, the expiry date of the third option is the 17th of September.
 */
public interface ExchangeTradedInstrumentExpiryCalculator {

  /**
   * The scheme for the external ids of expiry calculators.
   */
  ExternalScheme SCHEME = ExternalScheme.of("EXPIRY_CONVENTION");

  /**
   * Gets the expiry date of a future.
   * @param n The number of the future
   * @param today The date from which to calculate the expiry
   * @param holidayCalendar The holiday calendar for this future
   * @return The expiry date of the future
   */
  LocalDate getExpiryDate(int n, LocalDate today, Calendar holidayCalendar);

  /**
   * Gets the expiry month of a future.
   * @param n The number of the future
   * @param today The date from which to get the expiry month
   * @return The expiry month of the future
   */
  LocalDate getExpiryMonth(int n, LocalDate today);

  /**
   * Gets the name of the calculator.
   * @return The name
   */
  String getName();
}
