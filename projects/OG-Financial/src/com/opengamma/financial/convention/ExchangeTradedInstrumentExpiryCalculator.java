/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import javax.time.calendar.LocalDate;

import com.opengamma.financial.convention.calendar.Calendar;

/**
 * Calculates the expiry date of the nth option for an option type and an exchange. For example, IMM future options use the HMUZ schedule and expire two
 * business days before the third Wednesday of the month, so on 1/1/2012, the expiry date of the third option is the 17th of September.
 */
public interface ExchangeTradedInstrumentExpiryCalculator {

  LocalDate getExpiryDate(int n, LocalDate today, Calendar holidayCalendar);

  LocalDate getExpiryMonth(int n, LocalDate today);

  String getName();
}
