/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import org.threeten.bp.temporal.TemporalAdjuster;

/**
 * Interface that adds information about the fraction of a year (e.g. 3 for quarterly)
 * for a {@link TemporalAdjuster}.
 */
public interface RollDateAdjuster extends TemporalAdjuster {

  /**
   * Returns the number of months by which to adjust - e.g. 3 for quarterly.
   * @return The number of months
   */
  long getMonthsToAdjust();
}
