/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.legacy;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.SpreadBumpType;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.marketdatachecker.SpreadTermStructureDataChecker;
import com.opengamma.financial.convention.daycount.ActualThreeSixtyFive;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of Gamma for a vanilla Legacy CDS (parallel and bucketed bumps)
 */
public class GammaLegacyCreditDefaultSwap {

  private final double _tolerance = 1e-15;

  private static final DayCount ACT365 = new ActualThreeSixtyFive();

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Lots of ongoing work to do in this class - Work In Progress

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Compute the Gamma by a parallel bump of each point on the spread curve

  public double getGammaParallelShiftCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyCreditDefaultSwapDefinition cds,
      final ISDACurve yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double spreadBump,
      final SpreadBumpType spreadBumpType,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");
    ArgumentChecker.notNull(priceType, "Price type");

    // Construct a market data checker object
    final SpreadTermStructureDataChecker checkMarketData = new SpreadTermStructureDataChecker();

    // Check the efficacy of the input market data
    checkMarketData.checkSpreadData(valuationDate, cds, marketTenors, marketSpreads);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vectors to hold the bumped (up and down) market spreads
    final double[] bumpedUpMarketSpreads = new double[marketSpreads.length];
    final double[] bumpedDownMarketSpreads = new double[marketSpreads.length];

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

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS PV calculator
    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Calculate the unbumped CDS PV
    final double presentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

    // Calculate the bumped up CDS PV
    final double bumpedUpPresentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, bumpedUpMarketSpreads, yieldCurve, priceType);

    // Calculate the bumped down CDS PV
    final double bumpedDownPresentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, bumpedDownMarketSpreads, yieldCurve, priceType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the parallel gamma using a simple finite-difference approximation
    final double parallelGamma = (bumpedUpPresentValue - 2 * presentValue + bumpedDownPresentValue) / (2 * spreadBump);

    return parallelGamma;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Compute the Gamma by bumping each point on the spread curve individually by spreadBump (bump is same for all tenors)

  public double[] getGammaBucketedCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyCreditDefaultSwapDefinition cds,
      final ISDACurve yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double spreadBump,
      final SpreadBumpType spreadBumpType,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");
    ArgumentChecker.notNull(priceType, "Price type");

    // Construct a market data checker object
    final SpreadTermStructureDataChecker checkMarketData = new SpreadTermStructureDataChecker();

    // Check the efficacy of the input market data
    checkMarketData.checkSpreadData(valuationDate, cds, marketTenors, marketSpreads);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector to hold the bucketed gamma sensitivities (by tenor)
    final double[] bucketedGamma = new double[marketSpreads.length];

    // Vectors to hold the bumped (up and down) market spreads
    final double[] bumpedUpMarketSpreads = new double[marketSpreads.length];
    final double[] bumpedDownMarketSpreads = new double[marketSpreads.length];

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS calculator object
    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Calculate the unbumped CDS PV
    final double presentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

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
      final double bumpedUpPresentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, bumpedUpMarketSpreads, yieldCurve, priceType);

      // Calculate the bumped down CDS PV
      final double bumpedDownPresentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, bumpedDownMarketSpreads, yieldCurve, priceType);

      // Compute the bucketed gamma for this tenor
      bucketedGamma[m] = (bumpedUpPresentValue - 2 * presentValue + bumpedDownPresentValue) / (2 * spreadBump);
    }

    return bucketedGamma;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
