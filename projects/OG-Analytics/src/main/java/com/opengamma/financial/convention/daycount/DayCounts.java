/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.daycount;

/**
 * Standard implementations of {@code DayCount}.
 * <p>
 * These implementations are derived from {@link DayCountFactory}
 * thus the implementations can be overridden using a properties file.
 */
public final class DayCounts {

  /**
   * The 'Actual/360' day count.
   */
  public static final DayCount ACT_360 = DayCountFactory.of("Actual/360");
  /**
   * The 'Actual/365' day count.
   */
  public static final DayCount ACT_365 = DayCountFactory.of("Actual/365");
  /**
   * The 'Actual/365L' day count.
   */
  public static final DayCount ACT_365L = DayCountFactory.of("Actual/365L");
  /**
   * The 'Actual/Actual AFB' day count.
   */
  public static final DayCount ACT_ACT_AFB = DayCountFactory.of("Actual/Actual AFB");
  /**
   * The 'Actual/Actual ICMA' day count.
   */
  public static final DayCount ACT_ACT_ICMA = DayCountFactory.of("Actual/Actual ICMA");
  /**
   * The 'Actual/Actual ICMA Normal' day count.
   */
  public static final DayCount ACT_ACT_ICMA_NORMAL = DayCountFactory.of("Actual/Actual ICMA Normal");
  /**
   * The 'Actual/Actual ISDA' day count.
   */
  public static final DayCount ACT_ACT_ISDA = DayCountFactory.of("Actual/Actual ISDA");
  /**
   * The 'Actual/Actual ISDA' day count.
   */
  public static final DayCount ACT_36525 = DayCountFactory.of("Actual/365.25");
  /**
   * The '30/360 ISDA' day count.
   */
  public static final DayCount THIRTY_360_ISDA = DayCountFactory.of("30/360 ISDA");
  /**
   * The '30U/360' day count.
   */
  public static final DayCount THIRTY_U_360 = DayCountFactory.of("30U/360");
  /**
   * The '30E/360' day count.
   */
  public static final DayCount THIRTY_E_360 = DayCountFactory.of("30E/360");
  /**
   * The '30E/360' day count.
   */
  public static final DayCount THIRTY_E_360_ISDA = DayCountFactory.of("30E/360 ISDA");
  /**
   * The '28/360' day count.
   */
  public static final DayCount TWENTY_EIGHT_360 = DayCountFactory.of("28/360");
  /**
   * The 'Business/252' day count.
   */
  public static final DayCount BUSINESS_252 = DayCountFactory.of("Business/252");
  /**
   * The 30/360 day count.
   */
  public static final DayCount THIRTY_360 = DayCountFactory.of("30/360");
  /**
   * The 1/1 day count.
   */
  public static final DayCount ONE_ONE = DayCountFactory.of("1/1");

  /**
   * Restricted constructor.
   */
  private DayCounts() {
  }

}
