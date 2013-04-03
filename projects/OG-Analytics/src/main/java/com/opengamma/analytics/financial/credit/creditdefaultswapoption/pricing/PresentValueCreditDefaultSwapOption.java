/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswapoption.pricing;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CreditDefaultSwapOptionDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
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
  // TODO : Need to add error checking for d1 and d2 calculations
  // TODO : Check that valuationDate is not inconsistent with other trade economics

  // NOTE : Have not included the PriceType field for the CDS - assume this is entered as part of the underlying CDS contract definition
  // NOTE : The test for a negative option strike is done in the CDS swaption ctor

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  public static final String SPREAD_INTERPOLATOR = "FlatInterpolator";
  public static final String LEFT_EXTRAPOLATOR = "FlatExtrapolator";
  public static final String RIGHT_EXTRAPOLATOR = "FlatExtrapolator";

  private static final CombinedInterpolatorExtrapolator INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(SPREAD_INTERPOLATOR, LEFT_EXTRAPOLATOR, RIGHT_EXTRAPOLATOR);

  private static final GenerateCreditDefaultSwapPremiumLegSchedule premiumLegSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Public method for computing the PV of a CDS swaption based on an input CDS swaption contract (with a hazard rate curve calibrated to market observed data)

  public double getPresentValueCreditDefaultSwapOption(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final double sigma,
      final ZonedDateTime[] calibrationTenors,
      final double[] marketSpreads,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Check input objects are not null or invalid

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(cdsSwaption, "LegacyCreditDefaultSwapDefinition");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    ArgumentChecker.notNull(hazardRateCurve, "HazardRateCurve");

    ArgumentChecker.notNegative(sigma, "sigma");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    final double[] times = premiumLegSchedule.convertTenorsToDoubles(calibrationTenors, valuationDate, ACT_365);

    final DoublesCurve curve = InterpolatedDoublesCurve.fromSorted(times, marketSpreads, INTERPOLATOR);

    final NormalDistribution normal = new NormalDistribution(0.0, 1.0);

    double presentValue = 0.0;
    double frontendProtection = 0.0;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Get the underlying CDS in the swaption contract
    final CreditDefaultSwapDefinition underlyingCDS = cdsSwaption.getUnderlyingCDS();

    // Generate the cashflow schedule for the (forward) premium leg
    final ZonedDateTime[] underlyingCDSPremiumLegSchedule = premiumLegSchedule.constructCreditDefaultSwapPremiumLegSchedule(underlyingCDS);

    final ZonedDateTime optionExpiryDate = cdsSwaption.getOptionExerciseDate();
    final ZonedDateTime cdsMaturityDate = cdsSwaption.getUnderlyingCDS().getMaturityDate();

    final double optionStrike = cdsSwaption.getOptionStrike();

    // Calculate the remaining time to option expiry
    final double optionExpiryTime = TimeCalculator.getTimeBetween(valuationDate, cdsSwaption.getOptionExerciseDate());

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // If we are not exactly at the option expiry time ...

    if (Double.doubleToLongBits(optionExpiryTime) != 0) {

      // ... the option still has some value (and the calculation shouldn't fall over)

      // Calculate the forward risky dV01 as seen at the valuation date for the period [optionExpiryDate, cdsMaturityDate]
      final double riskydV01 = calculateForwardRiskydV01(valuationDate, optionExpiryDate, cdsMaturityDate, cdsSwaption, underlyingCDSPremiumLegSchedule, yieldCurve, hazardRateCurve);

      // Calculate the forward spread as seen at the valuation date for the period [optionExpiryDate, cdsMaturityDate]
      final double forwardSpread = calculateForwardSpread(
          valuationDate,
          optionExpiryDate,
          cdsMaturityDate,
          cdsSwaption,
          underlyingCDSPremiumLegSchedule,
          curve,
          /*calibrationTenors,
          marketSpreads,*/
          yieldCurve,
          hazardRateCurve);

      // ----------------------------------------------------------------------------------------------------------------------------------------

      final double d1 = (Math.log(forwardSpread / optionStrike) + 0.5 * sigma * sigma * optionExpiryTime) / (sigma * Math.sqrt(optionExpiryTime));
      final double d2 = (Math.log(forwardSpread / optionStrike) - 0.5 * sigma * sigma * optionExpiryTime) / (sigma * Math.sqrt(optionExpiryTime));

      // ----------------------------------------------------------------------------------------------------------------------------------------

      // Calculate the value of the CDS swaption

      if (cdsSwaption.isPayer()) {
        presentValue = riskydV01 * (forwardSpread * normal.getCDF(d1) - optionStrike * normal.getCDF(d2));
      } else {
        presentValue = riskydV01 * (optionStrike * normal.getCDF(-d2) - forwardSpread * normal.getCDF(-d1));
      }

      if (!cdsSwaption.isKnockOut()) {
        frontendProtection = calculateFrontendProtection(valuationDate, cdsSwaption, yieldCurve, hazardRateCurve);
      }
    }

    return cdsSwaption.getNotional() * (presentValue + frontendProtection);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private double calculateForwardRiskydV01(
      final ZonedDateTime valuationDate,
      final ZonedDateTime forwardStartDate,
      final ZonedDateTime forwardEndDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final ZonedDateTime[] premiumLegSchedule,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    double forwardRiskydV01 = 0.0;

    for (int i = 0; i < premiumLegSchedule.length; i++) {

    }

    return forwardRiskydV01;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private double calculateForwardSpread(
      final ZonedDateTime valuationDate,
      final ZonedDateTime forwardStartDate,
      final ZonedDateTime forwardEndDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final ZonedDateTime[] premiumLegSchedule,
      final DoublesCurve curve,
      /*final ZonedDateTime[] calibrationTenors,
      final double[] marketSpreads,*/
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    final double dV01ToForwardDate = calculateForwardRiskydV01(valuationDate, valuationDate, forwardStartDate, cdsSwaption, premiumLegSchedule, yieldCurve, hazardRateCurve);
    final double dV01ToMaturitydDate = calculateForwardRiskydV01(valuationDate, valuationDate, forwardEndDate, cdsSwaption, premiumLegSchedule, yieldCurve, hazardRateCurve);

    final double timeToForwardStartDate = TimeCalculator.getTimeBetween(valuationDate, forwardStartDate);
    final double timeToForwardEndDate = TimeCalculator.getTimeBetween(valuationDate, forwardEndDate);

    final double parSpreadToForwardDate = curve.getYValue(timeToForwardStartDate);
    final double parSpreadToMaturityDate = curve.getYValue(timeToForwardEndDate);

    final double forwardSpread = (parSpreadToMaturityDate * dV01ToMaturitydDate - parSpreadToForwardDate * dV01ToForwardDate) / (dV01ToMaturitydDate - dV01ToForwardDate);

    return forwardSpread;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private double calculateFrontendProtection(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final ISDADateCurve yieldCurve,
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
