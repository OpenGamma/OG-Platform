/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswapoption.greeks;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.bumpers.CreditSpreadBumpers;
import com.opengamma.analytics.financial.credit.bumpers.SpreadBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CreditDefaultSwapOptionDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.pricing.PresentValueCreditDefaultSwapOption;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.marketdatachecker.SpreadTermStructureDataChecker;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of CS01 for a CDS Swaption (parallel and bucketed bumps)
 */
public class CS01CreditDefaultSwapOption {

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Create an object to assist with the spread bumping
  private static final CreditSpreadBumpers spreadBumper = new CreditSpreadBumpers();

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to sort out where the underlying CDS priceType is set

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the CS01 by a parallel bump of each point on the spread curve
  public double getCS01ParallelShiftCreditDefaultSwapOption(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final double sigma,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double spreadBump,
      final SpreadBumpType spreadBumpType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cdsSwaption, "CreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");

    ArgumentChecker.notNegative(sigma, "Spread volatility");
    ArgumentChecker.notNegative(spreadBump, "Spread bump");

    // Construct a market data checker object
    final SpreadTermStructureDataChecker checkMarketData = new SpreadTermStructureDataChecker();

    // Check the efficacy of the input market data
    checkMarketData.checkSpreadData(valuationDate, marketTenors, marketSpreads);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector to hold the bumped market spreads
    final double[] bumpedMarketSpreads = spreadBumper.getBumpedCreditSpreads(marketSpreads, spreadBump, spreadBumpType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS Swaption PV calculator
    final PresentValueCreditDefaultSwapOption creditDefaultSwapOption = new PresentValueCreditDefaultSwapOption();

    // Calculate the unbumped CDS Swaption PV
    final double presentValue = creditDefaultSwapOption.getPresentValueCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, marketTenors, marketSpreads, yieldCurve, hazardRateCurve);

    // Calculate the bumped CDS Swaption PV
    final double bumpedPresentValue = creditDefaultSwapOption.getPresentValueCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, marketTenors, bumpedMarketSpreads, yieldCurve, hazardRateCurve);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the parallel CS01
    final double parallelCS01 = (bumpedPresentValue - presentValue) / spreadBump;

    return parallelCS01;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double getBetaAdjustedCS01ParallelShiftCreditDefaultSwapOption(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final double sigma,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double spreadBump,
      final double beta,
      final SpreadBumpType spreadBumpType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check that beta is in the range [-100%, +100%] and not null (don't need to check the other inputs as these are checked in the parallel CS01 calc)
    ArgumentChecker.isInRangeInclusive(-1.0, 1.0, beta);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Compute the unadjusted parallel CS01
    final double parallelCS01 = getCS01ParallelShiftCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, yieldCurve, hazardRateCurve, marketTenors, marketSpreads, spreadBump, spreadBumpType);

    // beta adjust the parallel CS01
    final double betaAdjustedParallelCS01 = beta * parallelCS01;

    return betaAdjustedParallelCS01;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Compute the CS01 by a parallel bump of each point on the spread curve
  public double[] getCS01BucketedCreditDefaultSwapOption(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final double sigma,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double spreadBump,
      final SpreadBumpType spreadBumpType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cdsSwaption, "CreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    //ArgumentChecker.notNull(hazardRateCurve, "HazardRateCurve"); Allow the hazard rate curve object to be null
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");

    ArgumentChecker.notNegative(sigma, "Spread volatility");
    ArgumentChecker.notNegative(spreadBump, "Spread bump");

    // Construct a market data checker object
    final SpreadTermStructureDataChecker checkMarketData = new SpreadTermStructureDataChecker();

    // Check the efficacy of the input market data
    checkMarketData.checkSpreadData(valuationDate, marketTenors, marketSpreads);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector of bucketed CS01 sensitivities (per tenor)
    final double[] bucketedCS01 = new double[marketSpreads.length];

    // Vector to hold the unbumped market spreads
    final double[] unbumpedMarketSpreads = new double[marketSpreads.length];

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS Swaption PV calculator
    final PresentValueCreditDefaultSwapOption creditDefaultSwapOption = new PresentValueCreditDefaultSwapOption();

    // Calculate the unbumped CDS Swaption PV
    final double presentValue = creditDefaultSwapOption.getPresentValueCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, marketTenors, marketSpreads, yieldCurve, hazardRateCurve);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Loop through and bump each of the spreads at each tenor
    for (int m = 0; m < marketSpreads.length; m++) {

      // Reset the unbumpedMarketSpreads vector to the original marketSpreads (shouldn't have to do this, but something funny happening if don't)
      for (int n = 0; n < marketTenors.length; n++) {
        unbumpedMarketSpreads[n] = marketSpreads[n];
      }

      // Calculate the bumped spreads vector (bumping only the spread at tenor m)
      final double[] bumpedMarketSpreads = spreadBumper.getBumpedCreditSpreads(unbumpedMarketSpreads, m, spreadBump, spreadBumpType);

      // Calculate the bumped CDS PV
      final double bumpedPresentValue = creditDefaultSwapOption.getPresentValueCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, marketTenors, bumpedMarketSpreads, yieldCurve,
          hazardRateCurve);

      // Compute the CS01 for this tenor
      bucketedCS01[m] = (bumpedPresentValue - presentValue) / spreadBump;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return bucketedCS01;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Compute the CS01 by a parallel bump of each point on the spread curve
  public double[] getBetaAdjustedCS01BucketedCreditDefaultSwapOption(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final double sigma,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double spreadBump,
      final double beta,
      final SpreadBumpType spreadBumpType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check that beta is in the range [-100%, +100%] and not null (don't need to check the other inputs as these are checked in the bucketed CS01 calc)
    ArgumentChecker.isInRangeInclusive(-1.0, 1.0, beta);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Compute the unadjusted bucketed CS01
    final double[] bucketedCS01 = getCS01BucketedCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, yieldCurve, hazardRateCurve, marketTenors, marketSpreads, spreadBump, spreadBumpType);

    final double[] betaAdjustedBucketedCS01 = new double[bucketedCS01.length];

    // beta adjust the bucketed CS01
    for (int m = 0; m < betaAdjustedBucketedCS01.length; m++) {
      betaAdjustedBucketedCS01[m] = beta * bucketedCS01[m];
    }

    return betaAdjustedBucketedCS01;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
