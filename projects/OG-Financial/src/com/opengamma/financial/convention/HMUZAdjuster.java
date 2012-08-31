/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.EnumSet;
import java.util.Set;

import javax.time.calendar.DateAdjuster;
import javax.time.calendar.LocalDate;
import javax.time.calendar.MonthOfYear;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public final class HMUZAdjuster implements DateAdjuster {
  private static final Set<MonthOfYear> FUTURE_EXPIRY_MONTHS = EnumSet.of(MonthOfYear.MARCH, MonthOfYear.JUNE, MonthOfYear.SEPTEMBER, MonthOfYear.DECEMBER);
  private static final HMUZAdjuster INSTANCE = new HMUZAdjuster();

  public static HMUZAdjuster getInstance() {
    return INSTANCE;
  }

  private HMUZAdjuster() {
  }

  @Override
  public LocalDate adjustDate(final LocalDate date) {
    ArgumentChecker.notNull(date, "date");
    LocalDate result = LocalDate.of(date);
    while (!FUTURE_EXPIRY_MONTHS.contains(result.getMonthOfYear())) {
      result = result.plusMonths(1);
    }
    return result;
  }

}
