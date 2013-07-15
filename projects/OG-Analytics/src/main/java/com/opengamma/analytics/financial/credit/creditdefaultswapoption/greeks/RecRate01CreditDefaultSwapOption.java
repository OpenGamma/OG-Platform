/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswapoption.greeks;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.bumpers.RecoveryRateBumpType;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CreditDefaultSwapOptionDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.pricing.PresentValueCreditDefaultSwapOption;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the computation of recovery rate sensitivity for a CDS Swaption
 */
public class RecRate01CreditDefaultSwapOption {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Need to move the recovery rate bumper function into the recovery rate bumpers class

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Compute the RecoveryRate01

  public double getRecoveryRate01CreditDefaultSwapOption(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final double sigma,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final ZonedDateTime[] marketTenors,
      final double[] marketSpreads,
      final double recoveryRateBump,
      final RecoveryRateBumpType recoveryRateBumpType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cdsSwaption, "CreditDefaultSwapOptionDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    //ArgumentChecker.notNull(hazardRateCurve, "HazardRateCurve"); Allow the hazard rate curve object to be null
    ArgumentChecker.notNull(marketTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");

    ArgumentChecker.notNegative(sigma, "Spread volatility");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Compute the bumped recovery rate
    final double bumpedRecoveryRate = getBumpedRecoveryRate(cdsSwaption, recoveryRateBump, recoveryRateBumpType);

    // Create a copy of the input CDS Swaption where the underlying CDS has the bumped recovery rate
    final CreditDefaultSwapOptionDefinition bumpedCDSSwaption = cdsSwaption.withRecoveryRate(bumpedRecoveryRate);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a CDS Swaption PV calculator
    final PresentValueCreditDefaultSwapOption creditDefaultSwapOption = new PresentValueCreditDefaultSwapOption();

    // Calculate the unbumped CDS Swaption PV
    final double presentValue = creditDefaultSwapOption.getPresentValueCreditDefaultSwapOption(valuationDate, cdsSwaption, sigma, marketTenors, marketSpreads, yieldCurve, hazardRateCurve);

    // Calculate the bumped CDS Swaption PV
    final double bumpedPresentValue = creditDefaultSwapOption.getPresentValueCreditDefaultSwapOption(valuationDate, bumpedCDSSwaption, sigma, marketTenors, marketSpreads, yieldCurve, hazardRateCurve);

    // Calculate the RecoveryRate01
    final double recoveryRate01 = (bumpedPresentValue - presentValue);

    return recoveryRate01;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

  private double getBumpedRecoveryRate(final CreditDefaultSwapOptionDefinition cdsSwaption, final double recoveryRateBump, final RecoveryRateBumpType recoveryRateBumpType) {

    double bumpedRecoveryRate = 0.0;

    if (recoveryRateBumpType == RecoveryRateBumpType.ADDITIVE) {
      bumpedRecoveryRate = cdsSwaption.getUnderlyingCDS().getRecoveryRate() + recoveryRateBump;
    }

    if (recoveryRateBumpType == RecoveryRateBumpType.MULTIPLICATIVE) {
      bumpedRecoveryRate = cdsSwaption.getUnderlyingCDS().getRecoveryRate() * (1 + recoveryRateBump);
    }

    return bumpedRecoveryRate;
  }

  // ------------------------------------------------------------------------------------------------------------------------------------------

}
