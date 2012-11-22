/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.SpreadBumpType;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.LegacyCreditDefaultSwapDefinition;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of Gamma for a vanilla Legacy CDS
 */
public class GammaLegacyCreditDefaultSwap {

  private final double _tolerance = 1e-15;

  private static final DayCount ACT365 = new ActualThreeSixtyFive();

  //-------------------------------------------------------------------------------------------------

  // TODO : Lots of ongoing work to do in this class - Work In Progress

  // TODO : Further checks on efficacy of input arguments e.g. tenors in increasing order

  // -------------------------------------------------------------------------------------------------

  public double getGammaParallelShiftCreditDefaultSwap(
      LegacyCreditDefaultSwapDefinition cds,
      ISDACurve yieldCurve,
      ZonedDateTime[] marketTenors,
      double[] marketSpreads,
      double spreadBump,
      SpreadBumpType spreadBumpType) {

    // -------------------------------------------------------------

    // Check input CDS, YieldCurve and SurvivalCurve objects are not null

    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");

    // Check that the number of input tenors matches the number of input spreads
    ArgumentChecker.isTrue(marketTenors.length == marketSpreads.length, "Number of tenors and number of spreads should be equal");

    // Check the efficacy of the input market data
    for (int m = 0; m < marketTenors.length; m++) {

      ArgumentChecker.isTrue(marketTenors[m].isAfter(cds.getValuationDate()), "Calibration instrument of tenor {} is before the valuation date {}", marketTenors[m], cds.getValuationDate());

      if (marketTenors.length > 1 && m > 0) {
        ArgumentChecker.isTrue(marketTenors[m].isAfter(marketTenors[m - 1]), "Tenors not in ascending order");
      }

      ArgumentChecker.notNegative(marketSpreads[m], "Market spread at tenor " + marketTenors[m]);
      ArgumentChecker.notZero(marketSpreads[m], _tolerance, "Market spread at tenor " + marketTenors[m]);
    }

    // -------------------------------------------------------------

    double parallelGamma = 0.0;

    return parallelGamma;
  }

}
