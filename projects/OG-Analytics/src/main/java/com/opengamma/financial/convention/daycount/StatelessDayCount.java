/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import java.io.Serializable;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * Base class providing a hash and equality test based on the class.
 */
/* package */abstract class StatelessDayCount extends AbstractDayCount implements Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * ArgumentCheckers that the dates are non-null and ordered/equal.
   * @param d1  the first date, not null
   * @param d2  the second date, not null
   */
  protected void testDates(final LocalDate d1, final LocalDate d2) {
    ArgumentChecker.notNull(d1, "first date");
    ArgumentChecker.notNull(d2, "second date");
    if (!(d2.isAfter(d1) || d2.equals(d1))) {
      throw new OpenGammaRuntimeException("d2 must be on or after d1: have d1 = " + d1 + " and d2 = " + d2);
    }
  }

  /**
   * ArgumentCheckers that the dates are non-null and ordered/equal.
   * @param d1  the first date, not null
   * @param d2  the second date, not null
   */
  protected void testDates(final ZonedDateTime d1, final ZonedDateTime d2) {
    ArgumentChecker.notNull(d1, "first date");
    ArgumentChecker.notNull(d2, "second date");
    testDates(d1.toLocalDate(), d2.toLocalDate());
  }

  /**
   * ArgumentCheckers that the dates are non-null and ordered/equal.
   * @param d1  the first date, not null
   * @param d2  the second date, not null
   * @param d3  the third date, not null
   */
  protected void testDates(final LocalDate d1, final LocalDate d2, final LocalDate d3) {
    ArgumentChecker.notNull(d1, "first date");
    ArgumentChecker.notNull(d2, "second date");
    ArgumentChecker.notNull(d3, "third date");
    ArgumentChecker.isTrue((d2.isAfter(d1) || d2.equals(d1)) && (d2.isBefore(d3) || d2.equals(d3)),
        "must have d1 <= d2 <= d3, have d1 = " + d1 + ", d2 = " + d2 + ", d3 = " + d3);
  }

  /**
   * ArgumentCheckers that the dates are non-null and ordered/equal.
   * @param d1  the first date, not null
   * @param d2  the second date, not null
   * @param d3  the third date, not null
   */
  protected void testDates(final ZonedDateTime d1, final ZonedDateTime d2, final ZonedDateTime d3) {
    ArgumentChecker.notNull(d1, "first date");
    ArgumentChecker.notNull(d2, "second date");
    ArgumentChecker.notNull(d3, "third date");
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
    return "DayCount [" + getName() + "]";
  }

}
