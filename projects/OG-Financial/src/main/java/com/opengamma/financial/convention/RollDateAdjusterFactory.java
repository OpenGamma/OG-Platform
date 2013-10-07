/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.HashMap;
import java.util.Map;

import org.threeten.bp.DayOfWeek;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

/**
 * Factory containing instances of {@link TemporalAdjuster}
 */
public class TemporalAdjusterFactory {
  /** The name of the third Wednesday adjuster */
  private static final String THIRD_WEDNESDAY_STRING = "Third Wednesday";
  /** The name of the third Monday adjuster */
  private static final String THIRD_MONDAY_STRING = "Third Monday";
  /** Adjusts dates to the third Wednesday of a month */
  private static final TemporalAdjuster THIRD_WEDNESDAY_ADJUSTER = TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.WEDNESDAY);
  /** Adjusts dates to the third Monday of a month */
  private static final TemporalAdjuster THIRD_MONDAY_ADJUSTER = TemporalAdjusters.dayOfWeekInMonth(3, DayOfWeek.MONDAY);
  /** Map containing the instances */
  private static final Map<String, TemporalAdjuster> s_instances = new HashMap<>();

  static {
    s_instances.put(THIRD_WEDNESDAY_STRING, THIRD_WEDNESDAY_ADJUSTER);
    s_instances.put(THIRD_MONDAY_STRING, THIRD_MONDAY_ADJUSTER);
  }

  /**
   * Gets the named adjuster.
   * @param name The name
   * @return The adjuster
   * @throws IllegalArgumentException if the adjuster was not found in the map
   */
  public static TemporalAdjuster getAdjuster(final String name) {
    final TemporalAdjuster adjuster = s_instances.get(name);
    if (adjuster != null) {
      return adjuster;
    }
    throw new IllegalArgumentException("Could not get adjuster called " + name);
  }
}
