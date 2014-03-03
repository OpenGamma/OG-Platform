/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rolldate;

import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;
import org.threeten.bp.temporal.TemporalAdjusters;

import com.opengamma.util.ArgumentChecker;

/**
 *  End of month roll date adjuster
 */
public final class EndOfMonthRollDateAdjuster implements RollDateAdjuster {

  /** Adjusts a date to the last day of a month */
  private static final TemporalAdjuster LAST_DAY_OF_THE_MONTH = TemporalAdjusters.lastDayOfMonth();
  /** The single instance */
  private static final EndOfMonthRollDateAdjuster INSTANCE = new EndOfMonthRollDateAdjuster();

  /**
   * Returns the single instance of this adjuster.
   * @return The adjuster
   */
  public static RollDateAdjuster getAdjuster() {
    return INSTANCE;
  }
  
  /**
   * Private constructor
   */
  private EndOfMonthRollDateAdjuster() {}
  
  @Override
  public Temporal adjustInto(Temporal temporal) {
    ArgumentChecker.notNull(temporal, "temporal");
    return temporal.with(LAST_DAY_OF_THE_MONTH);
  }
  
  @Override
  public long getMonthsToAdjust() {
    return 0;
  }

  @Override
  public String getName() {
    return RollDateAdjusterFactory.END_OF_MONTH_ROLL_STRING;
  }
}
