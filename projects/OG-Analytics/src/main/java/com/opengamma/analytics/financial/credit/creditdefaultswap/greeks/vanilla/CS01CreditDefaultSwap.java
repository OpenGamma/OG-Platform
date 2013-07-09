/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.bumpers.CreditSpreadBumpers;
import com.opengamma.analytics.financial.credit.bumpers.SpreadBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.marketdatachecker.SpreadTermStructureDataChecker;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of CS01 for a vanilla Legacy CDS (parallel and bucketed bumps)
 */
public class CS01CreditDefaultSwap {

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Create an object to assist with the spread bumping
  private static final CreditSpreadBumpers spreadBumper = new CreditSpreadBumpers();

  // ------------------------------------------------------------------------------------------------------------------------------------------

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
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double spreadBump,
      final SpreadBumpType spreadBumpType,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "CreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");
    ArgumentChecker.notNull(priceType, "price type");

    ArgumentChecker.notNegative(spreadBump, "Spread bump");

    // Construct a market data checker object
    final SpreadTermStructureDataChecker checkMarketData = new SpreadTermStructureDataChecker();

    // Check the efficacy of the input market data
    checkMarketData.checkSpreadData(valuationDate, /*cds,*/marketTenors, marketSpreads);
 
    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector to hold the bumped market spreads
    final double[] bumpedMarketSpreads = spreadBumper.getBumpedCreditSpreads(marketSpreads, spreadBump, spreadBumpType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS PV calculator
    //final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();
    final PresentValueCreditDefaultSwap creditDefaultSwap = new PresentValueCreditDefaultSwap();

    // Calculate the unbumped CDS PV
    final double presentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

    // Calculate the bumped (up) CDS PV
    final double bumpedPresentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, bumpedMarketSpreads, yieldCurve, priceType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the parallel CS01
    final double parallelCS01 = (bumpedPresentValue - presentValue) / spreadBump;

    return parallelCS01;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Compute the beta adjusted parallel CS01 
  public double getBetaAdjustedCS01ParallelShiftCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double spreadBump,
      final double beta,
      final SpreadBumpType spreadBumpType,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check that beta is in the range [-100%, +100%] and not null (don't need to check the other inputs as these are checked in the parallel CS01 calc)
    ArgumentChecker.isInRangeInclusive(-1.0, 1.0, beta);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Compute the unadjusted parallel CS01
    final double parallelCS01 = getCS01ParallelShiftCreditDefaultSwap(valuationDate, cds, yieldCurve, marketTenors, marketSpreads, spreadBump, spreadBumpType, priceType);

    // beta adjust the parallel CS01
    final double betaAdjustedParallelCS01 = beta * parallelCS01;

    return betaAdjustedParallelCS01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the beta adjusted bucketed CS01
  public double[] getBetaAdjustedCS01BucketedCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double spreadBump,
      final double beta,
      final SpreadBumpType spreadBumpType,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check that beta is in the range [-100%, +100%] and not null (don't need to check the other inputs as these are checked in the parallel CS01 calc)
    ArgumentChecker.isInRangeInclusive(-1.0, 1.0, beta);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Compute the unadjusted bucketed CS01
    final double[] bucketedCS01 = getCS01BucketedCreditDefaultSwap(valuationDate, cds, yieldCurve, marketTenors, marketSpreads, spreadBump, spreadBumpType, priceType);

    final double[] betaAdjustedBucketedCS01 = new double[bucketedCS01.length];

    // beta adjust the bucketed CS01
    for (int m = 0; m < betaAdjustedBucketedCS01.length; m++) {
      betaAdjustedBucketedCS01[m] = beta * bucketedCS01[m];
    }

    return betaAdjustedBucketedCS01;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Compute the CS01 by bumping each point on the spread curve individually by spreadBump (bump is same for all tenors)
  public double[] getCS01BucketedCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
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
    checkMarketData.checkSpreadData(valuationDate, /*cds, */marketTenors, marketSpreads);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector of bucketed CS01 sensitivities (per tenor)
    final double[] bucketedCS01 = new double[marketSpreads.length];

    // Vector to hold the bumped market spreads
    //double[] bumpedMarketSpreads = new double[marketSpreads.length];

    final double[] unbumpedMarketSpreads = new double[marketSpreads.length];

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS PV calculator
    final PresentValueCreditDefaultSwap creditDefaultSwap = new PresentValueCreditDefaultSwap();

    // Calculate the unbumped CDS PV
    final double presentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Loop through and bump each of the spreads at each tenor
    for (int m = 0; m < marketSpreads.length; m++) {

      // Reset the unbumpedMarketSpreads vector to the original marketSpreads (shouldn't have to do this, but something funny happening if don't)
      for (int n = 0; n < marketTenors.length; n++) {
        unbumpedMarketSpreads[n] = marketSpreads[n];
      }

      // Calculate the bumped spreads vector
      final double[] bumpedMarketSpreads = spreadBumper.getBumpedCreditSpreads(unbumpedMarketSpreads, m, spreadBump, spreadBumpType);

      // Calculate the bumped CDS PV
      final double bumpedPresentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, bumpedMarketSpreads, yieldCurve, priceType);

      // Compute the CS01 for this tenor
      bucketedCS01[m] = (bumpedPresentValue - presentValue) / spreadBump;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return bucketedCS01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------
}
