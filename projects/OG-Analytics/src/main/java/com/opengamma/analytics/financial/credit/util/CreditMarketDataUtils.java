/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.util;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.ArgumentChecker;

/**
 * Class to check the efficacy of user input CDS spread data (e.g. that tenors of calibrating instruments are in ascending order)
 *@deprecated this will be deleted 
 */
@Deprecated
public class CreditMarketDataUtils {
  private static final double TOLERANCE = 1e-15;

  public static void checkSpreadData(final ZonedDateTime valuationDate, final ZonedDateTime[] marketDates, final double[] marketSpreads) {
    ArgumentChecker.notNull(valuationDate, "valuation date");
    ArgumentChecker.notNull(marketDates, "market dates");
    ArgumentChecker.notNull(marketSpreads, "market spreads");
    ArgumentChecker.isTrue(marketDates.length == marketSpreads.length, "Number of dates {} and spreads {} should be equal", marketDates.length, marketSpreads.length);
    ArgumentChecker.isTrue(marketDates[0].isAfter(valuationDate), "Calibration instrument of tenor {} is before the valuation date {}", marketDates[0], valuationDate);
    ArgumentChecker.notNegativeOrZero(marketSpreads[0], TOLERANCE, "Market spread ({}) for date {}", marketSpreads[0], marketDates[0]);
    if (marketDates.length > 1) {
      for (int m = 1; m < marketDates.length; m++) {
        ArgumentChecker.isTrue(marketDates[m].isAfter(marketDates[m - 1]), "Dates not in ascending order");
        ArgumentChecker.notNegativeOrZero(marketSpreads[m], TOLERANCE, "Market spread ({}) for date {}", marketSpreads[m], marketDates[m]);
      }
    }
  }
}
