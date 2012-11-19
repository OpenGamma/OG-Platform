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
import com.opengamma.analytics.financial.credit.marketdatachecker.SpreadTermStructureDataChecker;
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

  // -------------------------------------------------------------------------------------------------

  // Compute the Gamma by a parallel bump of each point on the spread curve 

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

    // Construct a market data checker object
    SpreadTermStructureDataChecker checkMarketData = new SpreadTermStructureDataChecker();

    // Check the efficacy of the input market data
    checkMarketData.checkSpreadData(cds, marketTenors, marketSpreads);

    // -------------------------------------------------------------

    double[] bumpedUpMarketSpreads = new double[marketSpreads.length];
    double[] bumpedDownMarketSpreads = new double[marketSpreads.length];

    // Calculate the bumped spreads
    for (int m = 0; m < marketTenors.length; m++) {
      if (spreadBumpType == SpreadBumpType.ADDITIVE_PARALLEL) {
        bumpedUpMarketSpreads[m] = marketSpreads[m] + spreadBump;
        bumpedDownMarketSpreads[m] = marketSpreads[m] - spreadBump;
      }

      if (spreadBumpType == SpreadBumpType.MULTIPLICATIVE_PARALLEL) {
        bumpedUpMarketSpreads[m] = marketSpreads[m] * (1 + spreadBump);
        bumpedDownMarketSpreads[m] = marketSpreads[m] * (1 - spreadBump);
      }
    }

    // -------------------------------------------------------------

    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Calculate the unbumped CDS PV
    double presentValue = creditDefaultSwap.calibrateAndGetPresentValue(cds, marketTenors, marketSpreads, yieldCurve);

    // Calculate the bumped up CDS PV
    double bumpedUpPresentValue = creditDefaultSwap.calibrateAndGetPresentValue(cds, marketTenors, bumpedUpMarketSpreads, yieldCurve);

    // Calculate the bumped down CDS PV
    double bumpedDownPresentValue = creditDefaultSwap.calibrateAndGetPresentValue(cds, marketTenors, bumpedDownMarketSpreads, yieldCurve);

    // -------------------------------------------------------------

    // Calculate the parallel gamma using a simple finite-difference approximation
    double parallelGamma = (bumpedUpPresentValue - 2 * presentValue + bumpedDownPresentValue) / (2 * spreadBump);

    return parallelGamma;
  }

  // -------------------------------------------------------------------------------------------------

  // Compute the Gamma by bumping each point on the spread curve individually by spreadBump (bump is same for all tenors) 

  public double[] getGammaBucketedCreditDefaultSwap(
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

    // Construct a market data checker object
    SpreadTermStructureDataChecker checkMarketData = new SpreadTermStructureDataChecker();

    // Check the efficacy of the input market data
    checkMarketData.checkSpreadData(cds, marketTenors, marketSpreads);

    // -------------------------------------------------------------

    double[] bucketedGamma = new double[marketSpreads.length];

    double[] bumpedUpMarketSpreads = new double[marketSpreads.length];
    double[] bumpedDownMarketSpreads = new double[marketSpreads.length];

    // -------------------------------------------------------------

    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Calculate the unbumped CDS PV
    double presentValue = creditDefaultSwap.calibrateAndGetPresentValue(cds, marketTenors, marketSpreads, yieldCurve);

    // -------------------------------------------------------------

    // Loop through each of the spreads at each tenor
    for (int m = 0; m < marketTenors.length; m++) {

      // Reset the bumpedMarketSpreads vector to the original marketSpreads
      for (int n = 0; n < marketTenors.length; n++) {
        bumpedUpMarketSpreads[n] = marketSpreads[n];
        bumpedDownMarketSpreads[n] = marketSpreads[n];
      }

      // Bump the spread at tenor m
      if (spreadBumpType == SpreadBumpType.ADDITIVE_BUCKETED) {
        bumpedUpMarketSpreads[m] = marketSpreads[m] + spreadBump;
        bumpedDownMarketSpreads[m] = marketSpreads[m] - spreadBump;
      }

      if (spreadBumpType == SpreadBumpType.MULTIPLICATIVE_BUCKETED) {
        bumpedUpMarketSpreads[m] = marketSpreads[m] * (1 + spreadBump);
        bumpedDownMarketSpreads[m] = marketSpreads[m] * (1 - spreadBump);
      }

      // Calculate the bumped up CDS PV
      double bumpedUpPresentValue = creditDefaultSwap.calibrateAndGetPresentValue(cds, marketTenors, bumpedUpMarketSpreads, yieldCurve);

      // Calculate the bumped down CDS PV
      double bumpedDownPresentValue = creditDefaultSwap.calibrateAndGetPresentValue(cds, marketTenors, bumpedDownMarketSpreads, yieldCurve);

      // Compute the gamma
      bucketedGamma[m] = (bumpedUpPresentValue - 2 * presentValue + bumpedDownPresentValue) / (2 * spreadBump);
    }

    return bucketedGamma;
  }

  // -------------------------------------------------------------------------------------------------
}
