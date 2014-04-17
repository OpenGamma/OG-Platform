/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rolldate;

import org.threeten.bp.temporal.Temporal;
import org.threeten.bp.temporal.TemporalAdjuster;

/**
 *  A general implementation of RollDateAdjuster
 */
public class GeneralRollDateAdjuster implements RollDateAdjuster {

  private final int _numMonthsToAdjust;
  private final TemporalAdjuster _adjuster;

  GeneralRollDateAdjuster(final int monthsToAdjust, final TemporalAdjuster adjuster) {
    _numMonthsToAdjust = monthsToAdjust;
    _adjuster = adjuster;
  }

  @Override
  public long getMonthsToAdjust() {
    return _numMonthsToAdjust;
  }

  @Override
  public Temporal adjustInto(final Temporal temporal) {
    return temporal.with(_adjuster);
  }

  @Override
  public String getName() {
    return "General";
  }
}
