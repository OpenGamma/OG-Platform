/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import org.threeten.bp.ZonedDateTime;

/**
 * Utilities for working with tenors.
 * <p>
 * This is a thread-safe static utility class.
 * @deprecated The methods in this class are either deprecated or not used.
 */
@Deprecated
public class TenorUtils {

  /**
   * Restricted constructor.
   */
  protected TenorUtils() {
    super();
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the number of days in the tenor.
   * This method assumes 24-hour days.
   * Minutes are ignored.
   * @param tenor  the tenor, not null
   * @return the number of days
   */
  public static double getDaysInTenor(final Tenor tenor) {
    return tenor.getPeriod().getDays();
  }

  /**
   * Subtracts a tenor from a date-time.
   * @param dateTime  the date-time to adjust, not null
   * @param tenor  the tenor, not null
   * @return the adjusted date-time
   * @deprecated This method name is not clear about whether the tenor is added to or subtracted from the date.
   */
  @Deprecated
  public static ZonedDateTime getDateWithTenorOffset(final ZonedDateTime dateTime, final Tenor tenor) {
    return dateTime.minus(tenor.getPeriod());
  }

  /**
   * Gets the fraction representing the the number of days in the first tenor
   * divided by the number of days in the second tenor.
   * @param first  the first tenor, not null
   * @param second  the second tenor, not null
   * @return the number of the second tenor in the first
   * @deprecated The method name does not make it clear that this applies only to tenors with a number
   * of days in them and would give 0 for first = P2Y, second = P1Y and is not a general-purpose
   * method.
   */
  @Deprecated
  public static double getTenorsInTenor(final Tenor first, final Tenor second) {
    return TenorUtils.getDaysInTenor(first) / TenorUtils.getDaysInTenor(second);
  }

}
