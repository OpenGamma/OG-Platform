/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import java.io.Serializable;

import javax.time.calendar.LocalDate;
import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Base class providing a hash and equality test based on the class.
 */
/* package */abstract class StatelessDayCount extends DayCount implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * Validates that the dates are non-null and ordered/equal.
   * @param d1  the first date, not null
   * @param d2  the second date, not null
   */
  protected void testDates(final LocalDate d1, final LocalDate d2) {
    Validate.notNull(d1);
    Validate.notNull(d2);
    if (!(d2.isAfter(d1) || d2.equals(d1))) {
      throw new OpenGammaRuntimeException("d2 must be on or after d1: have d1 = " + d1 + " and d2 = " + d2);
    }
  }

  /**
   * Validates that the dates are non-null and ordered/equal.
   * @param d1  the first date, not null
   * @param d2  the second date, not null
   */
  protected void testDates(final ZonedDateTime d1, final ZonedDateTime d2) {
    Validate.notNull(d1);
    Validate.notNull(d2);
    testDates(d1.toLocalDate(), d2.toLocalDate());
  }

  /**
   * Validates that the dates are non-null and ordered/equal.
   * @param d1  the first date, not null
   * @param d2  the second date, not null
   * @param d3  the third date, not null
   */
  protected void testDates(final LocalDate d1, final LocalDate d2, final LocalDate d3) {
    Validate.notNull(d1);
    Validate.notNull(d2);
    Validate.notNull(d3);
    Validate.isTrue((d2.isAfter(d1) || d2.equals(d1)) && (d2.isBefore(d3) || d2.equals(d3)),
        "must have d1 <= d2 <= d3, have d1 = " + d1 + ", d2 = " + d2 + ", d3 = " + d3);
  }

  /**
   * Validates that the dates are non-null and ordered/equal.
   * @param d1  the first date, not null
   * @param d2  the second date, not null
   * @param d3  the third date, not null
   */
  protected void testDates(final ZonedDateTime d1, final ZonedDateTime d2, final ZonedDateTime d3) {
    Validate.notNull(d1);
    Validate.notNull(d2);
    Validate.notNull(d3);
    testDates(d1.toLocalDate(), d2.toLocalDate(), d3.toLocalDate());
  }

  // -------------------------------------------------------------------------
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    return getClass().equals(obj.getClass());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public String toString() {
    return "DayCount [" + getConventionName() + "]";
  }
  
}
