/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswapoption.pricing;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.PriceType;
import com.opengamma.analytics.financial.credit.cds.ISDACurve;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CDSOptionKnockoutType;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CDSOptionType;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CreditDefaultSwapOptionDefinition;
import com.opengamma.analytics.financial.credit.hazardratemodel.HazardRateCurve;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the valuation of CDS swaptions
 */
public class PresentValueCreditDefaultSwapOption {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Lots of ongoing work to do in this class - Work In Progress

  // TODO : Need to sort out the calculation of the risky dV01
  // TODO : Need to sort out the calculation of the forward starting spread
  // TODO : Need to check through this model in detail
  // TODO : Sort out if we need the 'PriceType' field
  // TODO : Need to add error checking for d1 and d2 calculations

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Public method for computing the PV of a CDS swaption based on an input CDS swaption contract (with a hazard rate curve calibrated to market observed data)

  public double getPresentValueCreditDefaultSwapOption(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final double sigma,
      final ISDACurve yieldCurve,
      final HazardRateCurve hazardRateCurve,
      final PriceType priceType) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null or invalid

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cdsSwaption, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(hazardRateCurve, "HazardRateCurve");
    ArgumentChecker.notNull(priceType, "price type");

    ArgumentChecker.notNegative(sigma, "sigma");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    NormalDistribution normal = new NormalDistribution(0.0, 1.0);

    double presentValue = 0.0;
    double frontendProtection = 0.0;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    double optionStrike = cdsSwaption.getOptionStrike();

    // Calculate the option expiry time
    final double optionExpiryTime = TimeCalculator.getTimeBetween(valuationDate, cdsSwaption.getOptionExerciseDate());

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the risky dV01 
    final double dV01 = calculateRiskydV01(valuationDate, cdsSwaption, yieldCurve, hazardRateCurve);

    // Calculate the forward spread
    final double forwardSpread = calculateForwardSpread(valuationDate, cdsSwaption, yieldCurve, hazardRateCurve);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    final double d1 = (Math.log(forwardSpread / optionStrike) + 0.5 * sigma * sigma * optionExpiryTime) / (sigma * Math.sqrt(optionExpiryTime));
    final double d2 = (Math.log(forwardSpread / optionStrike) - 0.5 * sigma * sigma * optionExpiryTime) / (sigma * Math.sqrt(optionExpiryTime));

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the value of the CDS swaption

    if (cdsSwaption.getOptionType() == CDSOptionType.PAYER) {
      presentValue = dV01 * (forwardSpread * normal.getCDF(d1) - optionStrike * normal.getCDF(d2));
    }

    if (cdsSwaption.getOptionType() == CDSOptionType.RECEIVER) {
      presentValue = dV01 * (optionStrike * normal.getCDF(-d2) - forwardSpread * normal.getCDF(-d1));
    }

    if (cdsSwaption.getOptionKnockoutType() == CDSOptionKnockoutType.NONKNOCKOUT) {
      frontendProtection = calculateFrontendProtection(valuationDate, cdsSwaption, yieldCurve, hazardRateCurve);
    }

    return cdsSwaption.getNotional() * (presentValue + frontendProtection);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private double calculateRiskydV01(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final ISDACurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    double riskydV01 = 0.0;

    return riskydV01;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private double calculateForwardSpread(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final ISDACurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    double forwardSpread = 0.0;

    return forwardSpread;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private double calculateFrontendProtection(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final ISDACurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    // Calculate the option expiry time
    final double optionExpiryTime = TimeCalculator.getTimeBetween(valuationDate, cdsSwaption.getOptionExerciseDate());

    // Get the discount factor between [valuationDate, optionExpiry]
    final double discountFactor = yieldCurve.getDiscountFactor(optionExpiryTime);

    // Get the survival probability between [valuationDate, optionExpiry]
    final double survivalProbability = hazardRateCurve.getSurvivalProbability(optionExpiryTime);

    // Get the recovery rate of the reference entity in the underlying CDS
    final double recoveryRate = cdsSwaption.getUnderlyingCDS().getRecoveryRate();

    // Compute the value of the front-end protection
    final double frontendProtection = cdsSwaption.getNotional() * (1 - recoveryRate) * discountFactor * (1 - survivalProbability);

    return frontendProtection;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
