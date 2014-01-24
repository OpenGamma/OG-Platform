/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rolldate;

import org.joda.convert.FromString;

import com.opengamma.financial.convention.AbstractNamedInstanceFactory;

/**
 * Factory containing instances of {@link RollDateAdjuster}
 */
public final class RollDateAdjusterFactory
    extends AbstractNamedInstanceFactory<RollDateAdjuster> {
  /**
   * Singleton instance
   */
  public static final RollDateAdjusterFactory INSTANCE = new RollDateAdjusterFactory();
  /** The name of the next quarterly IMM roll date adjuster */
  public static final String QUARTERLY_IMM_ROLL_STRING = "Quarterly IMM Roll";
  /** The name of the next monthly IMM roll date adjuster */
  public static final String MONTHLY_IMM_ROLL_STRING = "Monthly IMM Roll";
  /** The name of the end of month roll date adjuster */
  public static final String END_OF_MONTH_ROLL_STRING = "End Of Month Roll";

  //-------------------------------------------------------------------------
  /**
   * Finds an adjuster by name, ignoring case.
   * 
   * @param name  the name of the instance to find, not null
   * @return the adjuster, not null
   * @throws IllegalArgumentException if the name is not found
   */
  @FromString
  public static RollDateAdjuster of(final String name) {
    return INSTANCE.instance(name);
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor, hard coding the conventions.
   */
  private RollDateAdjusterFactory() {
    super(RollDateAdjuster.class);
    addInstance(MonthlyIMMRollDateAdjuster.getAdjuster());
    addInstance(QuarterlyIMMRollDateAdjuster.getAdjuster());
    addInstance(EndOfMonthRollDateAdjuster.getAdjuster());
  }

  /**
   * Gets the named adjuster.
   * 
   * @param name  the name, not null
   * @return the adjuster, not null
   * @throws IllegalArgumentException if the adjuster was not found in the map
   * @deprecated Use {@link #of(String)} or {@link #instance(String)}.
   */
  @Deprecated
  public static RollDateAdjuster getAdjuster(String name) {
    return INSTANCE.instance(name);
  }

}
