/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.marketdatachecker;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to check the efficacy of user input CDS spread data (e.g. that tenors are in ascending order)
 */
public class SpreadTermStructureDataChecker {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private final double _tolerance = 1e-15;

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Check that no two tenors are identical

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public void checkSpreadData(final ZonedDateTime valuationDate, final LegacyCreditDefaultSwapDefinition cds, final ZonedDateTime[] marketTenors, final double[] marketSpreads) {

    // Check that the number of input tenors matches the number of input spreads
    ArgumentChecker.isTrue(marketTenors.length == marketSpreads.length, "Number of tenors and number of spreads should be equal");

    // Check the efficacy of the input market data
    for (int m = 0; m < marketTenors.length; m++) {

      ArgumentChecker.isTrue(marketTenors[m].isAfter(valuationDate), "Calibration instrument of tenor {} is before the valuation date {}", marketTenors[m], valuationDate);

      if (marketTenors.length > 1 && m > 0) {
        ArgumentChecker.isTrue(marketTenors[m].isAfter(marketTenors[m - 1]), "Tenors not in ascending order");
      }

      ArgumentChecker.notNegative(marketSpreads[m], "Market spread at tenor " + marketTenors[m]);
      ArgumentChecker.notZero(marketSpreads[m], _tolerance, "Market spread at tenor " + marketTenors[m]);
    }

  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
