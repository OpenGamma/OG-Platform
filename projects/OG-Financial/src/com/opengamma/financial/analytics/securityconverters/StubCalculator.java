/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.securityconverters;

import javax.time.calendar.LocalDate;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class Stub {

  public enum StubType {
    NONE, SHORT_START, LONG_START, SHORT_END, LONG_END
  }

  public static StubType getStartStubType(final LocalDate[] payments, final int paymentsPerYear) {
    Validate.isTrue(12 % paymentsPerYear == 0);
    final int months = 12 / paymentsPerYear;
    final LocalDate first = payments[0];
    final LocalDate second = payments[1];
    final LocalDate date = first.plusMonths(months);
    if (date.equals(second)) {
      return StubType.NONE;
    }
    if (date.isAfter(second)) {
      return StubType.SHORT_START;
    }
    return StubType.LONG_START;
  }

  public static StubType getEndStubType(final LocalDate[] payments, final int paymentsPerYear) {
    Validate.isTrue(12 % paymentsPerYear == 0);
    final int months = 12 / paymentsPerYear;
    final int n = payments.length;
    final LocalDate first = payments[n - 2];
    final LocalDate second = payments[n - 1];
    final LocalDate date = first.plusMonths(months);
    if (date.equals(second)) {
      return StubType.NONE;
    }
    if (date.isAfter(second)) {
      return StubType.SHORT_END;
    }
    return StubType.LONG_END;
  }
}
