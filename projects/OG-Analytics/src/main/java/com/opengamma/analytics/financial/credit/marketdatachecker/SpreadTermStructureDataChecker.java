/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.marketdatachecker;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.util.ArgumentChecker;

/**
 * Class to check the efficacy of user input CDS spread data (e.g. that tenors of calibrating instruments are in ascending order)
 */
public class SpreadTermStructureDataChecker {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private final double _tolerance = 1e-15;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Check that no two tenors are identical
  // TODO : Don't need to check that val date is before any of the calibration tenors

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public void checkSpreadData(
      final ZonedDateTime valuationDate,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads) {

    // Check that the number of input tenors matches the number of input spreads
    ArgumentChecker.isTrue(marketTenors.length == marketSpreads.length, "Number of tenors and number of spreads should be equal");

    ArgumentChecker.isTrue(marketTenors[0].isAfter(valuationDate), "Calibration instrument of tenor {} is before the valuation date {}", marketTenors[0], valuationDate);
    ArgumentChecker.notNegative(marketSpreads[0], "Market spread at tenor " + marketTenors[0]);
    ArgumentChecker.notZero(marketSpreads[0], _tolerance, "Market spread at tenor " + marketTenors[0]);
    // Check the efficacy of the input market data
    for (int m = 1; m < marketTenors.length; m++) {
      ArgumentChecker.isTrue(marketTenors[m].isAfter(valuationDate), "Calibration instrument of tenor {} is before the valuation date {}", marketTenors[m], valuationDate);
      //      if (marketTenors.length > 1 && m > 0) {
      ArgumentChecker.isTrue(marketTenors[m].isAfter(marketTenors[m - 1]), "Tenors not in ascending order");
      //      }
      ArgumentChecker.notNegative(marketSpreads[m], "Market spread at tenor " + marketTenors[m]);
      ArgumentChecker.notZero(marketSpreads[m], _tolerance, "Market spread at tenor " + marketTenors[m]);
    }
  }
}
