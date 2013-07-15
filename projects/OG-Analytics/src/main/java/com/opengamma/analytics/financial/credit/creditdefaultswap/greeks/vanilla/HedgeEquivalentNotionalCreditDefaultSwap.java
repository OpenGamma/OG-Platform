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
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class HedgeEquivalentNotionalCreditDefaultSwap {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Add the analytics for these calculations

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Compute the hedge equivalent notional for the tenor specified in hedgeEquivalentNotionalTenor

  public double getHedgeEquivalentNotionalCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final ZonedDateTime hedgeEquivalentNotionalTenor,
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
    ArgumentChecker.notNull(hedgeEquivalentNotionalTenor, "Hedge equivalent notional tenor");
    ArgumentChecker.notNull(spreadBumpType, "Spread bump type");
    ArgumentChecker.notNull(priceType, "price type");

    ArgumentChecker.notNegative(spreadBump, "Spread bump");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    double hedgeEquivalentNotional = 0.0;

    return hedgeEquivalentNotional;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Compute the hedge equivalent notionals for the tenors specified in marketTenors 

  public double[] getBucketedHedgeEquivalentNotionalCreditDefaultSwap(
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

    // ----------------------------------------------------------------------------------------------------------------------------------------

    double[] bucketedHedgeEquivalentNotional = new double[marketSpreads.length];

    for (int i = 0; i < bucketedHedgeEquivalentNotional.length; i++) {
      bucketedHedgeEquivalentNotional[i] = 0.0;
    }

    return bucketedHedgeEquivalentNotional;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
