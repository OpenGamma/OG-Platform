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
 * Class containing methods for the computation of Gamma for a CDS Swaption (parallel and bucketed bumps)
 */
public class GammaCreditDefaultSwapOption {

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Create an object to assist with the spread bumping
  private static final CreditSpreadBumpers spreadBumper = new CreditSpreadBumpers();

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to sort out where the underlying CDS priceType is set

  // ------------------------------------------------------------------------------------------------------------------------------------------

  // Compute the Gamma by a parallel bump of each point on the spread curve
  public double getGammaParallelShiftCreditDefaultSwapOption(
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

    // Calculate the bumped up market spreads
    final double[] bumpedUpMarketSpreads = spreadBumper.getBumpedCreditSpreads(marketSpreads, spreadBump, spreadBumpType);

    // Calculate the bumped down market spreads
    final double[] bumpedDownMarketSpreads = spreadBumper.getBumpedCreditSpreads(marketSpreads, -spreadBump, spreadBumpType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS Swaption PV calculator
    final PresentValueCreditDefaultSwapOption creditDefaultSwapOption = new PresentValueCreditDefaultSwapOption();

    // Calculate the unbumped CDS Swaption PV
    final double presentValue = creditDefaultSwapOption.getPresentValueCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, marketTenors, marketSpreads, yieldCurve, hazardRateCurve);

    // Calculate the bumped up CDS Swaption PV
    final double bumpedUpPresentValue = creditDefaultSwapOption.getPresentValueCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, marketTenors, bumpedUpMarketSpreads, yieldCurve,
        hazardRateCurve);

    // Calculate the bumped down CDS Swaption PV
    final double bumpedDownPresentValue = creditDefaultSwapOption.getPresentValueCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, marketTenors, bumpedDownMarketSpreads, yieldCurve,
        hazardRateCurve);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the parallel gamma using a simple finite-difference approximation
    final double parallelGamma = (bumpedUpPresentValue - 2 * presentValue + bumpedDownPresentValue) / (2 * spreadBump);

    return parallelGamma;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double getBetaAdjustedGammaParallelShiftCreditDefaultSwapOption(
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

    // Compute the unadjusted parallel gamma
    final double parallelGamma = getGammaParallelShiftCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, yieldCurve, hazardRateCurve, marketTenors, marketSpreads, spreadBump, spreadBumpType);

    // beta adjust the parallel gamma
    final double betaAdjustedParallelGamma = beta * parallelGamma;

    return betaAdjustedParallelGamma;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double[] getGammaBucketedCreditDefaultSwapOption(
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

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Construct a market data checker object
    final SpreadTermStructureDataChecker checkMarketData = new SpreadTermStructureDataChecker();

    // Check the efficacy of the input market data
    checkMarketData.checkSpreadData(valuationDate, marketTenors, marketSpreads);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector to hold the bucketed gamma sensitivities (by tenor)
    final double[] bucketedGamma = new double[marketSpreads.length];

    // Vectors to hold the bumped (up and down) market spreads
    final double[] bumpedUpMarketSpreads = new double[marketSpreads.length];
    final double[] bumpedDownMarketSpreads = new double[marketSpreads.length];

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS Swaption PV calculator
    final PresentValueCreditDefaultSwapOption creditDefaultSwapOption = new PresentValueCreditDefaultSwapOption();

    // Calculate the unbumped CDS Swaption PV
    final double presentValue = creditDefaultSwapOption.getPresentValueCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, marketTenors, marketSpreads, yieldCurve, hazardRateCurve);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Loop through each of the spreads at each tenor
    for (int m = 0; m < marketTenors.length; m++) {

      // Reset the bumpedMarketSpreads vector to the original marketSpreads
      for (int n = 0; n < marketTenors.length; n++) {
        bumpedUpMarketSpreads[n] = marketSpreads[n];
        bumpedDownMarketSpreads[n] = marketSpreads[n];
      }

      // Bump the spread at tenor m
      if (spreadBumpType == SpreadBumpType.ADDITIVE_BUCKETED || spreadBumpType == SpreadBumpType.ADDITIVE) {
        bumpedUpMarketSpreads[m] = marketSpreads[m] + spreadBump;
        bumpedDownMarketSpreads[m] = marketSpreads[m] - spreadBump;
      } else if (spreadBumpType == SpreadBumpType.MULTIPLICATIVE_BUCKETED || spreadBumpType == SpreadBumpType.MULTIPLICATIVE) {
        bumpedUpMarketSpreads[m] = marketSpreads[m] * (1 + spreadBump);
        bumpedDownMarketSpreads[m] = marketSpreads[m] * (1 - spreadBump);
      } else {
        throw new IllegalArgumentException("Cannot handle bumps of type " + spreadBumpType);
      }

      // Calculate the bumped up CDS Swaption PV
      final double bumpedUpPresentValue = creditDefaultSwapOption.getPresentValueCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, marketTenors, bumpedUpMarketSpreads, yieldCurve,
          hazardRateCurve);

      // Calculate the bumped down CDS Swaption PV
      final double bumpedDownPresentValue = creditDefaultSwapOption.getPresentValueCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, marketTenors, bumpedDownMarketSpreads, yieldCurve,
          hazardRateCurve);

      // Compute the bucketed gamma for this tenor
      bucketedGamma[m] = (bumpedUpPresentValue - 2 * presentValue + bumpedDownPresentValue) / (2 * spreadBump);
    }

    return bucketedGamma;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  public double[] getBetaAdjustedGammaBucketedCreditDefaultSwapOption(
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

    final double[] betaAdjustedBucketedGamma = new double[marketTenors.length];

    // Compute the unadjusted bucketed gamma
    final double[] bucketedGamma = getGammaBucketedCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, yieldCurve, hazardRateCurve, marketTenors, marketSpreads, spreadBump, spreadBumpType);

    // beta adjust the bucketed gamma
    for (int m = 0; m < betaAdjustedBucketedGamma.length; m++) {
      betaAdjustedBucketedGamma[m] = beta * bucketedGamma[m];
    }

    return betaAdjustedBucketedGamma;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
