/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswapoption.greeks;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.bumpers.SpreadVolatilityBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CreditDefaultSwapOptionDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.pricing.PresentValueCreditDefaultSwapOption;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of spread volatility sensitivity for a CDS Swaption
 */
public class VegaCreditDefaultSwapOption {

  // Compute the RecoveryRate01

  public double getVegaCreditDefaultSwapOption(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final double sigma,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double spreadVolatilityBump,
      final SpreadVolatilityBumpType spreadVolatilityBumpType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cdsSwaption, "CreditDefaultSwapOptionDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    //ArgumentChecker.notNull(hazardRateCurve, "HazardRateCurve"); Allow the hazard rate curve object to be null
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");

    ArgumentChecker.notNegative(sigma, "Spread volatility");
    ArgumentChecker.notNull(spreadVolatilityBumpType, "Spread volatility bump type");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Compute the bumped recovery rate
    final double bumpedSigma = getBumpedSpreadVolatility(sigma, spreadVolatilityBump, spreadVolatilityBumpType);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS Swaption PV calculator
    final PresentValueCreditDefaultSwapOption creditDefaultSwapOption = new PresentValueCreditDefaultSwapOption();

    // Calculate the unbumped CDS Swaption PV
    final double presentValue = creditDefaultSwapOption.getPresentValueCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, marketTenors, marketSpreads, yieldCurve, hazardRateCurve);

    // Calculate the bumped CDS Swaption PV
    final double bumpedPresentValue = creditDefaultSwapOption.getPresentValueCreditDefaultSwapOption(valuationDate, cdsSwaption, bumpedSigma, marketTenors, marketSpreads, yieldCurve, hazardRateCurve);

    // Calculate the RecoveryRate01
    final double vega01 = (bumpedPresentValue - presentValue);

    return vega01;

    // ----------------------------------------------------------------------------------------------------------------------------------------
  }

  private double getBumpedSpreadVolatility(final double spreadVolatility, final double spreadVolatilityBump, final SpreadVolatilityBumpType spreadVolatilityBumpType) {

    double bumpedSpreadVolatility = 0.0;

    if (spreadVolatilityBumpType == SpreadVolatilityBumpType.ADDITIVE) {
      bumpedSpreadVolatility = spreadVolatility + spreadVolatilityBump;
    }

    if (spreadVolatilityBumpType == SpreadVolatilityBumpType.MULTIPLICATIVE) {
      bumpedSpreadVolatility = spreadVolatility * (1 + spreadVolatilityBump);
    }

    return bumpedSpreadVolatility;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
