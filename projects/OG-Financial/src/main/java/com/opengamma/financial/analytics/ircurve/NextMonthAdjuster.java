/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;

/**
 * A {@code DateAdjuster} that moves the date to the next March/June/September/December.
 */
public class NextMonthAdjuster implements DateAdjuster {
  @Override
  public LocalDate adjustDate(LocalDate date) {
    LocalDate result = date;
    result = result.plusMonths(1);
    return result;
  }
}
