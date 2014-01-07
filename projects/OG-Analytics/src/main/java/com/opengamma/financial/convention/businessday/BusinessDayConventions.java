/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

/**
 * Standard implementations of {@code BusinessDayConvention}.
 * <p>
 * These implementations are derived from {@link BusinessDayConventionFactory}
 * thus the implementations can be overridden using a properties file.
 */
public final class BusinessDayConventions {

  /**
   * The modified following business day convention.
   * <p>
   * This chooses the next working day following a non-working day, unless that date is in a different month.
   * In which case the date is adjusted to be the preceding business day.
   */
  public static final BusinessDayConvention MODIFIED_FOLLOWING = BusinessDayConventionFactory.of("Modified Following");
  /**
   * The modified preceding business day convention.
   * <p>
   * This chooses the previous working day before a non-working day, unless than date is in a different month. 
   * In that case, the date is adjusted to be the following business day. 
   */
  public static final BusinessDayConvention MODIFIED_PRECEDING = BusinessDayConventionFactory.of("Modified Preceding");
  /**
   * The following business day convention.
   * <p>
   * This chooses the next working day following a non-working day.
   */
  public static final BusinessDayConvention FOLLOWING = BusinessDayConventionFactory.of("Following");
  /**
   * The preceding business day convention.
   * <p>
   * This chooses the latest working day preceding a non-working day.
   */
  public static final BusinessDayConvention PRECEDING = BusinessDayConventionFactory.of("Preceding");
  /**
   * The no adjustment business day convention.
   * <p>
   * This implementation always returns the input date, performing no adjustments.
   */
  public static final BusinessDayConvention NONE = BusinessDayConventionFactory.of("None");

  /**
   * Restricted constructor.
   */
  private BusinessDayConventions() {
  }

}
