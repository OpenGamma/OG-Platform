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
 * Class containing methods for the computation of CS01 for a vanilla Legacy CDS (parallel and bucketed bumps)
 */
public class CS01LegacyCreditDefaultSwap {

  // ------------------------------------------------------------------------------------------------------------------------------------------

  private final double _tolerance = 1e-15;

  private static final DayCount ACT365 = new ActualThreeSixtyFive();

  //-------------------------------------------------------------------------------------------------

  // TODO : Lots of ongoing work to do in this class - Work In Progress

  // TODO : Further checks on efficacy of input arguments
  // TODO : Need to get the times[] calculation correct
  // TODO : Need to consider more sophisticated sensitivity calculations e.g. algorithmic differentiation
  // TODO : Move the calculation of generic CDS risk sensitivities (CS01, Gamma, IR01, RecRate01, VoD and theta) into the vanilla definition

  // NOTE : We enforce spreadBump > 0, therefore if the marketSpreads > 0 (an exception is thrown if this is not the case) then bumpedMarketSpreads > 0 by construction

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the CS01 by a parallel bump of each point on the spread curve

  public double getCS01ParallelShiftCreditDefaultSwap(
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
    ArgumentChecker.notNull(priceType, "price type");

    ArgumentChecker.notNegative(spreadBump, "Spread bump");

    // Construct a market data checker object
    final SpreadTermStructureDataChecker checkMarketData = new SpreadTermStructureDataChecker();

    // Check the efficacy of the input market data
    checkMarketData.checkSpreadData(valuationDate, cds, marketTenors, marketSpreads);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector to hold the bumped market spreads
    final double[] bumpedMarketSpreads = new double[marketSpreads.length];

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the bumped spreads
    for (int m = 0; m < marketTenors.length; m++) {
      if (spreadBumpType == SpreadBumpType.ADDITIVE_PARALLEL) {
        bumpedMarketSpreads[m] = marketSpreads[m] + spreadBump;
      }

      if (spreadBumpType == SpreadBumpType.MULTIPLICATIVE_PARALLEL) {
        bumpedMarketSpreads[m] = marketSpreads[m] * (1 + spreadBump);
      }
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS PV calculator
    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Calculate the unbumped CDS PV
    final double presentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

    // Calculate the bumped (up) CDS PV
    final double bumpedPresentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, bumpedMarketSpreads, yieldCurve, priceType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the parallel CS01
    final double parallelCS01 = (bumpedPresentValue - presentValue) / spreadBump;

    return parallelCS01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the CS01 by bumping each point on the spread curve individually by spreadBump (bump is same for all tenors)

  public double[] getCS01BucketedCreditDefaultSwap(
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

    ArgumentChecker.notNegative(spreadBump, "Spread bump");

    // Construct a market data checker object
    final SpreadTermStructureDataChecker checkMarketData = new SpreadTermStructureDataChecker();

    // Check the efficacy of the input market data
    checkMarketData.checkSpreadData(valuationDate, cds, marketTenors, marketSpreads);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector of bucketed CS01 sensitivities (per tenor)
    final double[] bucketedCS01 = new double[marketSpreads.length];

    // Vector to hold the bumped market spreads
    final double[] bumpedMarketSpreads = new double[marketSpreads.length];

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS PV calculator
    final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();

    // Calculate the unbumped CDS PV
    final double presentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Loop through and bump each of the spreads at each tenor
    for (int m = 0; m < marketSpreads.length; m++) {

      // Reset the bumpedMarketSpreads vector to the original marketSpreads
      for (int n = 0; n < marketTenors.length; n++) {
        bumpedMarketSpreads[n] = marketSpreads[n];
      }

      // Bump the spread at tenor m
      if (spreadBumpType == SpreadBumpType.ADDITIVE_BUCKETED) {
        bumpedMarketSpreads[m] = marketSpreads[m] + spreadBump;
      }

      if (spreadBumpType == SpreadBumpType.MULTIPLICATIVE_BUCKETED) {
        bumpedMarketSpreads[m] = marketSpreads[m] * (1 + spreadBump);
      }

      // Calculate the bumped CDS PV
      final double bumpedPresentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, bumpedMarketSpreads, yieldCurve, priceType);

      bucketedCS01[m] = (bumpedPresentValue - presentValue) / spreadBump;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return bucketedCS01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------
}
