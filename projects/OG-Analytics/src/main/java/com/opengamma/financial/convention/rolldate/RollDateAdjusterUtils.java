/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rolldate;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.ArgumentChecker;

/**
 * Utilities related to RollDateAjuster. In particular to compute the n-th date from a starting point.
 */
public class RollDateAdjusterUtils {

  /**
   * Compute the nth rolled date for a given adjuster.
   * @param startingDate The starting date for the roll. The date itself is included in the roll period, 
   * i.e. if the starting date is in the RollDateAdjuster, the first date is the starting date.
   * @param adjuster The roll date adjuster.
   * @param numberRoll The number of times the date should be rolled.
   * @return The adjusted date.
   */
  public static ZonedDateTime nthDate(final ZonedDateTime startingDate, final RollDateAdjuster adjuster, final int numberRoll) {
    ArgumentChecker.isTrue(numberRoll >= 1, "At least one roll period");
    ZonedDateTime nthDate = startingDate.with(adjuster);
    for (int loopNumber = 1; loopNumber < numberRoll; loopNumber++) {
      nthDate = nthDate.plusDays(1).with(adjuster);
    }
    return nthDate;
  }

}
