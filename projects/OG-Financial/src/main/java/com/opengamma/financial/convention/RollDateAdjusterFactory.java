/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory containing instances of {@link RollDateAdjuster}
 */
public class RollDateAdjusterFactory {
  /** The name of the next quarterly IMM roll date adjuster */
  public static final String QUARTERLY_IMM_ROLL_STRING = "Quarterly IMM Roll";
  /** The name of the next monthly IMM roll date adjuster */
  public static final String MONTHLY_IMM_ROLL_STRING = "Monthly IMM Roll";
  /** Adjusts dates to the next quarterly IMM roll date */
  private static final RollDateAdjuster QUARTERLY_IMM_ROLL_ADJUSTER = QuarterlyIMMRollDateAdjuster.getAdjuster();
  /** Adjusts dates to the next monthly IMM roll date */
  private static final RollDateAdjuster MONTHLY_IMM_ROLL_ADJUSTER = MonthlyIMMRollDateAdjuster.getAdjuster();
  /** Map containing the instances */
  private static final Map<String, RollDateAdjuster> s_instances = new HashMap<>();

  static {
    s_instances.put(QUARTERLY_IMM_ROLL_STRING, QUARTERLY_IMM_ROLL_ADJUSTER);
    s_instances.put(MONTHLY_IMM_ROLL_STRING, MONTHLY_IMM_ROLL_ADJUSTER);
  }

  /**
   * Gets the named adjuster.
   * @param name The name
   * @return The adjuster
   * @throws IllegalArgumentException if the adjuster was not found in the map
   */
  public static RollDateAdjuster getAdjuster(final String name) {
    final RollDateAdjuster adjuster = s_instances.get(name);
    if (adjuster != null) {
      return adjuster;
    }
    throw new IllegalArgumentException("Could not get adjuster called " + name);
  }
}
