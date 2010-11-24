/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.Validate;

/**
 * Base class providing a hash and equality test based on the class.
 */
/* package */abstract class StatelessDayCount implements DayCount {

  /**
   * Validates that the dates are non-null and ordered/equal.
   * @param d1  the first date, not null
   * @param d2  the second date, not null
   */
  protected void testDates(final ZonedDateTime d1, final ZonedDateTime d2) {
    Validate.notNull(d1);
    Validate.notNull(d2);
    Validate.isTrue(d2.isAfter(d1) || d2.equals(d1));
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
    Validate.isTrue((d2.isAfter(d1) || d2.equals(d1)) && (d2.isBefore(d3) || d2.equals(d3)));
  }

  //-------------------------------------------------------------------------
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

}
