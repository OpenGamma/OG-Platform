/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rolldate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.convert.FromString;

import com.opengamma.financial.convention.NamedInstanceFactory;

/**
 * Factory containing instances of {@link RollDateAdjuster}
 */
public final class RollDateAdjusterFactory implements NamedInstanceFactory<RollDateAdjuster> {
  /**
   * Singleton instance
   */
  public static final RollDateAdjusterFactory INSTANCE = new RollDateAdjusterFactory();
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
  private static final List<RollDateAdjuster> s_adjusters = new ArrayList<>();

  private RollDateAdjusterFactory() {
  }
  
  static {
    store(QUARTERLY_IMM_ROLL_STRING, QUARTERLY_IMM_ROLL_ADJUSTER);
    store(MONTHLY_IMM_ROLL_STRING, MONTHLY_IMM_ROLL_ADJUSTER);
  }
  
  private static void store(String name, RollDateAdjuster adjuster) {
    s_instances.put(name, adjuster);
    s_adjusters.add(adjuster);
  }

  /**
   * Gets the named adjuster.
   * @param name The name
   * @return The adjuster
   * @throws IllegalArgumentException if the adjuster was not found in the map
   * @deprecated use of()
   */
  @Deprecated
  public static RollDateAdjuster getAdjuster(final String name) {
    final RollDateAdjuster adjuster = s_instances.get(name);
    if (adjuster != null) {
      return adjuster;
    }
    throw new IllegalArgumentException("Could not get adjuster called " + name);
  }
  
  /**
   * Gets the named adjuster.
   * @param name The name
   * @return The adjuster
   * @throws IllegalArgumentException if the adjuster was not found in the map
   */
  @FromString
  public static RollDateAdjuster of(final String name) {
    final RollDateAdjuster adjuster = s_instances.get(name);
    if (adjuster != null) {
      return adjuster;
    }
    throw new IllegalArgumentException("Could not get adjuster called " + name);
  }

  @Override
  public List<RollDateAdjuster> values() {
    return Collections.unmodifiableList(s_adjusters);
  }
}
