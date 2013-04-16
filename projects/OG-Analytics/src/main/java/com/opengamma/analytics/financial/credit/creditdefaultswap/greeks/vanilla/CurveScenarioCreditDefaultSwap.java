/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.greeks.vanilla;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.bumpers.SpreadBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.marketdatachecker.SpreadTermStructureDataChecker;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to enable user to specify a non-homogeneous set of bumps to the credit spread term structure
 * This allows the user to specify a curve scenario (e.g. curve flips from upward to downward sloping)
 * and to then compute the impact on the PV
 */
public class CurveScenarioCreditDefaultSwap {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The user can bump the market spreads at each tenor by a tenor specific amount (additive and parallel bumps)
  public double getCurveScenarioCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ISDADateCurve/*ISDACurve*/yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double[] spreadBumps,
      final SpreadBumpType spreadBumpType,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input CDS, YieldCurve and SurvivalCurve objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBumps, "Spread bumps");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");
    ArgumentChecker.notNull(priceType, "Price type");

    // Construct a market data checker object
    final SpreadTermStructureDataChecker checkMarketData = new SpreadTermStructureDataChecker();

    // Check the efficacy of the input market data
    checkMarketData.checkSpreadData(valuationDate, /*cds, */marketTenors, marketSpreads);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector to hold the bumped market spreads
    final double[] bumpedMarketSpreads = new double[marketSpreads.length];

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS PV calculator
    //final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();
    final PresentValueCreditDefaultSwap creditDefaultSwap = new PresentValueCreditDefaultSwap();

    // Calculate the unbumped CDS PV
    final double presentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Loop through and bump each of the spreads at each tenor
    for (int m = 0; m < marketSpreads.length; m++) {

      // Bump the spread at tenor m
      if (spreadBumpType == SpreadBumpType.ADDITIVE) {
        bumpedMarketSpreads[m] = marketSpreads[m] + spreadBumps[m];
      }

      if (spreadBumpType == SpreadBumpType.MULTIPLICATIVE) {
        bumpedMarketSpreads[m] = marketSpreads[m] * (1 + spreadBumps[m]);
      }

      ArgumentChecker.notNegative(bumpedMarketSpreads[m], "Bumped market spread");
    }

    // Calculate the bumped CDS PV
    final double bumpedPresentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, bumpedMarketSpreads, yieldCurve, priceType);

    double curveScenarioPresentValue = (bumpedPresentValue - presentValue);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return curveScenarioPresentValue;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // The user can bump the market spreads at each tenor by a specified amount (additive and parallel bumps)
  public double getCurveScenarioCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ISDADateCurve/*ISDACurve*/yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double spreadBump,
      final SpreadBumpType spreadBumpType,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input CDS, YieldCurve and SurvivalCurve objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cds, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(spreadBump, "Spread bump");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");
    ArgumentChecker.notNull(priceType, "Price type");

    // Construct a market data checker object
    final SpreadTermStructureDataChecker checkMarketData = new SpreadTermStructureDataChecker();

    // Check the efficacy of the input market data
    checkMarketData.checkSpreadData(valuationDate, /*cds, */marketTenors, marketSpreads);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Vector to hold the bumped market spreads
    final double[] bumpedMarketSpreads = new double[marketSpreads.length];

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS PV calculator
    //final PresentValueLegacyCreditDefaultSwap creditDefaultSwap = new PresentValueLegacyCreditDefaultSwap();
    final PresentValueCreditDefaultSwap creditDefaultSwap = new PresentValueCreditDefaultSwap();

    // Calculate the unbumped CDS PV
    final double presentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, marketSpreads, yieldCurve, priceType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Loop through and bump each of the spreads at each tenor
    for (int m = 0; m < marketSpreads.length; m++) {

      // Bump the spread at tenor m
      if (spreadBumpType == SpreadBumpType.ADDITIVE) {
        bumpedMarketSpreads[m] = marketSpreads[m] + spreadBump;
      }

      if (spreadBumpType == SpreadBumpType.MULTIPLICATIVE) {
        bumpedMarketSpreads[m] = marketSpreads[m] * (1 + spreadBump);
      }

      ArgumentChecker.notNegative(bumpedMarketSpreads[m], "Bumped market spread");
    }

    // Calculate the bumped CDS PV
    final double bumpedPresentValue = creditDefaultSwap.calibrateAndGetPresentValue(valuationDate, cds, marketTenors, bumpedMarketSpreads, yieldCurve, priceType);

    double curveScenarioPresentValue = (bumpedPresentValue - presentValue);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return curveScenarioPresentValue;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
