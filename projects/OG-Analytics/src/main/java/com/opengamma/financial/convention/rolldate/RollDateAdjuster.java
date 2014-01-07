/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rolldate;

import org.joda.convert.FromStringFactory;
import org.joda.convert.ToString;
import org.threeten.bp.temporal.TemporalAdjuster;

import com.opengamma.financial.convention.NamedInstance;

/**
 * Interface that adds information about the fraction of a year (e.g. 3 for quarterly)
 * for a {@link TemporalAdjuster}.
 */
@FromStringFactory(factory = RollDateAdjusterFactory.class)
public interface RollDateAdjuster extends TemporalAdjuster, NamedInstance {

  /**
   * Returns the number of months by which to adjust - e.g. 3 for quarterly.
   * 
   * @return The number of months
   */
  long getMonthsToAdjust();

  /**
   * Returns the name of the adjuster convention.
   * 
   * @return the name, not null
   */
  @ToString
  String getName();

}
