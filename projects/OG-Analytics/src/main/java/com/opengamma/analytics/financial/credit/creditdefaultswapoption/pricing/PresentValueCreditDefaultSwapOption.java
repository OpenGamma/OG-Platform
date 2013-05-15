/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswapoption.pricing;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.calibration.CalibrateHazardRateTermStructureISDAMethod;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.pricing.vanilla.PresentValueCreditDefaultSwap;
import com.opengamma.analytics.financial.credit.creditdefaultswapoption.definition.CreditDefaultSwapOptionDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the valuation of CDS Swaptions
 */
public class PresentValueCreditDefaultSwapOption {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Lots of ongoing work to do in this class - Work In Progress

  // TODO : Need to check through this model in detail - specifically the risky dV01 and forward spread calculations
  // TODO : Need to add error checking for d1 and d2 calculations
  // TODO : Check that valuationDate is not inconsistent with other trade economics

  // TODO : Need to do the calibration of the hazard rates for the underlying reference entity
  // TODO : Need to take out the HazardRateCurve input from this ctor
  // TODO : Make sure have got the cashflow conventions (and also when to include the FEP calculation) correct

  // TODO : Have to sort out the hazard rate calibration objects (the type of the underlying CDS should just be CreditDefaultSwapDefinition)

  // TODO : Check that the underlying CDS calibration is performed correctly

  // NOTE : Have not included the PriceType field for the CDS - assume this is entered as part of the underlying CDS contract definition
  // NOTE : The test for a negative option strike is done in the CDS Swaption ctor
  // NOTE : The checks of the efficacy of the input spread data are done in the underlying CDS classes (so don't need to be done here)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  private static final String SPREAD_INTERPOLATOR = Interpolator1DFactory.LINEAR; //TODO "FlatInterpolator" does not exist - is linear correct, or should it be STEP?
  private static final String LEFT_EXTRAPOLATOR = Interpolator1DFactory.FLAT_EXTRAPOLATOR;
  private static final String RIGHT_EXTRAPOLATOR = Interpolator1DFactory.FLAT_EXTRAPOLATOR;

  private static final CombinedInterpolatorExtrapolator INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(SPREAD_INTERPOLATOR, LEFT_EXTRAPOLATOR, RIGHT_EXTRAPOLATOR);

  private static final GenerateCreditDefaultSwapPremiumLegSchedule PREMIUM_LEG_SCHEDULE_CALCULATOR = new GenerateCreditDefaultSwapPremiumLegSchedule();

  // Create a CDS PV calculator object (this is used in the calibration of the survival probabilities)
  private static final PresentValueCreditDefaultSwap creditDefaultSwap = new PresentValueCreditDefaultSwap();

  // Create an object for calibrating a SNCDS
  private static final CalibrateHazardRateTermStructureISDAMethod cdsCalibrator = new CalibrateHazardRateTermStructureISDAMethod();

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Public method for computing the PV of a CDS Swaption based on an input CDS Swaption contract

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
    ArgumentChecker.notNull(cdsSwaption, "CreditDefaultSwapOptionDefinition");
    ArgumentChecker.notNull(calibrationTenors, "Market tenors");
    ArgumentChecker.notNull(marketSpreads, "Market spreads");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve");
    //ArgumentChecker.notNull(hazardRateCurve, "HazardRateCurve"); Allow the hazard rate curve object to be null

    ArgumentChecker.notNegative(sigma, "Spread volatility");

    // ----------------------------------------------------------------------------------------------------------------------------------------

    final double[] times = PREMIUM_LEG_SCHEDULE_CALCULATOR.convertTenorsToDoubles(calibrationTenors, valuationDate, ACT_365);

    final DoublesCurve spreadCurve = InterpolatedDoublesCurve.fromSorted(times, marketSpreads, INTERPOLATOR);

    final NormalDistribution normal = new NormalDistribution(0.0, 1.0);

    double presentValue = 0.0;
    double frontendProtection = 0.0;

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Create a copy of the underlying CDS for the purposes of calibration of the hazard rate term structure
    final LegacyVanillaCreditDefaultSwapDefinition underlyingCalibrationCDS = (LegacyVanillaCreditDefaultSwapDefinition) cdsSwaption.getUnderlyingCDS();

    // Build a hazard rate curve object based on the input market data
    //final HazardRateCurve calibratedHazardRateCurve = creditDefaultSwap.calibrateHazardRateCurve(valuationDate, underlyingCalibrationCDS, calibrationTenors, marketSpreads, yieldCurve);
    final HazardRateCurve calibratedHazardRateCurve = cdsCalibrator.isdaCalibrateHazardRateCurve(valuationDate, underlyingCalibrationCDS, calibrationTenors, marketSpreads, yieldCurve);

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Get the underlying CDS in the swaption contract (don't really need to do this if we fix the LegacyVanilla issue above)
    final CreditDefaultSwapDefinition underlyingCDS = cdsSwaption.getUnderlyingCDS();

    CreditDefaultSwapDefinition shortCDS = cdsSwaption.getUnderlyingCDS();

    shortCDS = shortCDS.withStartDate(cdsSwaption.getStartDate());
    shortCDS = shortCDS.withEffectiveDate(cdsSwaption.getStartDate().plusDays(1));
    shortCDS = shortCDS.withMaturityDate(cdsSwaption.getOptionExerciseDate());

    // Generate the cashflow schedule for the (forward) premium leg
    final ZonedDateTime[] underlyingCDSPremiumLegSchedule = PREMIUM_LEG_SCHEDULE_CALCULATOR.constructCreditDefaultSwapPremiumLegSchedule(underlyingCDS);

    final ZonedDateTime[] shortCDSPremiumLegSchedule = PREMIUM_LEG_SCHEDULE_CALCULATOR.constructCreditDefaultSwapPremiumLegSchedule(shortCDS);

    // Get the option expiry date
    final ZonedDateTime optionExpiryDate = cdsSwaption.getOptionExerciseDate();

    // Get the maturity date of the underlying CDS
    final ZonedDateTime cdsMaturityDate = cdsSwaption.getUnderlyingCDS().getMaturityDate();

    // Get the option strike
    final double optionStrike = cdsSwaption.getOptionStrike() / 10000.0;

    // Calculate the remaining time to option expiry (as a double)
    final double optionExpiryTime = TimeCalculator.getTimeBetween(valuationDate, cdsSwaption.getOptionExerciseDate());

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // If we are not exactly at the option expiry time ...

    if (Double.doubleToLongBits(optionExpiryTime) != 0) {

      // ... the option still has some value (and the calculation shouldn't fall over)

      // Calculate the forward risky dV01 as seen at the valuation date for the period [optionExpiryDate, cdsMaturityDate]
      final double riskydV01 = calculateForwardRiskydV01(
          valuationDate,
          optionExpiryDate,
          cdsMaturityDate,
          cdsSwaption,
          underlyingCDSPremiumLegSchedule,
          yieldCurve,
          calibratedHazardRateCurve/*hazardRateCurve*/);

      // Calculate the forward spread as seen at the valuation date for the period [optionExpiryDate, cdsMaturityDate]
      final double forwardSpread = calculateForwardSpread(
          valuationDate,
          optionExpiryDate,
          cdsMaturityDate,
          cdsSwaption,
          underlyingCDSPremiumLegSchedule,
          shortCDSPremiumLegSchedule,
          spreadCurve,
          yieldCurve,
          calibratedHazardRateCurve/*hazardRateCurve*/) / 10000.0;

      // ----------------------------------------------------------------------------------------------------------------------------------------

      // Calculate the value of the CDS swaption

      final double d1 = (Math.log(forwardSpread / optionStrike) + 0.5 * sigma * sigma * optionExpiryTime) / (sigma * Math.sqrt(optionExpiryTime));
      final double d2 = (Math.log(forwardSpread / optionStrike) - 0.5 * sigma * sigma * optionExpiryTime) / (sigma * Math.sqrt(optionExpiryTime));

      if (cdsSwaption.isPayer()) {
        presentValue = riskydV01 * (forwardSpread * normal.getCDF(d1) - optionStrike * normal.getCDF(d2));
      } else {
        presentValue = riskydV01 * (optionStrike * normal.getCDF(-d2) - forwardSpread * normal.getCDF(-d1));
      }

      // Do we need to add the front end protection
      if (!cdsSwaption.isKnockOut()) {
        frontendProtection = calculateFrontendProtection(valuationDate, cdsSwaption, yieldCurve, calibratedHazardRateCurve/*hazardRateCurve*/);
      }
    }

    return cdsSwaption.getNotional() * (presentValue + frontendProtection);
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the value of the risky premium leg cashflows for the underlying CDS ( which occur after the option expiry date)

  private double calculateForwardRiskydV01(
      final ZonedDateTime valuationDate,
      final ZonedDateTime forwardStartDate,
      final ZonedDateTime forwardEndDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final ZonedDateTime[] premiumLegSchedule,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    double forwardRiskydV01 = 0.0;

    for (int i = 1; i < premiumLegSchedule.length; i++) {

      final double timeToPreviousCashflow = TimeCalculator.getTimeBetween(valuationDate, premiumLegSchedule[i - 1]);
      final double timeToCurrentCashflow = TimeCalculator.getTimeBetween(valuationDate, premiumLegSchedule[i]);
      final double dcf = TimeCalculator.getTimeBetween(premiumLegSchedule[i - 1], premiumLegSchedule[i]);

      final double discountFactor = yieldCurve.getDiscountFactor(timeToCurrentCashflow);

      final double survivalProbabilityToPreviousCashflow = hazardRateCurve.getSurvivalProbability(timeToPreviousCashflow);
      final double survivalProbabilityToCurrentCashflow = hazardRateCurve.getSurvivalProbability(timeToCurrentCashflow);

      forwardRiskydV01 += (dcf * discountFactor * (survivalProbabilityToPreviousCashflow + survivalProbabilityToCurrentCashflow));
    }

    return 0.5 * forwardRiskydV01;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private double calculateForwardSpread(
      final ZonedDateTime valuationDate,
      final ZonedDateTime forwardStartDate,
      final ZonedDateTime forwardEndDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final ZonedDateTime[] premiumLegSchedule,
      final ZonedDateTime[] shortCDSPremiumLegSchedule,
      final DoublesCurve spreadCurve,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    final double dV01ToForwardDate = calculateForwardRiskydV01(valuationDate, valuationDate, forwardStartDate, cdsSwaption, shortCDSPremiumLegSchedule, yieldCurve, hazardRateCurve);
    final double dV01ToMaturitydDate = calculateForwardRiskydV01(valuationDate, valuationDate, forwardEndDate, cdsSwaption, premiumLegSchedule, yieldCurve, hazardRateCurve);

    final double timeToForwardStartDate = TimeCalculator.getTimeBetween(valuationDate, forwardStartDate);
    final double timeToForwardEndDate = TimeCalculator.getTimeBetween(valuationDate, forwardEndDate);

    final double parSpreadToForwardDate = spreadCurve.getYValue(timeToForwardStartDate);
    final double parSpreadToMaturityDate = spreadCurve.getYValue(timeToForwardEndDate);

    final double forwardSpread = (parSpreadToMaturityDate * dV01ToMaturitydDate - parSpreadToForwardDate * dV01ToForwardDate) / (dV01ToMaturitydDate - dV01ToForwardDate);

    return forwardSpread;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private double calculateFrontendProtection(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapOptionDefinition cdsSwaption,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    // Calculate the remaining time between valuationDate to option expiry time
    final double remainingOptionTime = TimeCalculator.getTimeBetween(valuationDate, cdsSwaption.getOptionExerciseDate());

    // Get the discount factor between [valuationDate, optionExpiry]
    final double discountFactor = yieldCurve.getDiscountFactor(remainingOptionTime);

    // Get the survival probability between [valuationDate, optionExpiry]
    final double survivalProbability = hazardRateCurve.getSurvivalProbability(remainingOptionTime);

    // Get the recovery rate of the reference entity in the underlying CDS
    final double recoveryRate = cdsSwaption.getUnderlyingCDS().getRecoveryRate();

    // Compute the value of the front-end protection (note that we don't multiply by the CDS Swaption notional here)
    final double frontendProtection = (1 - recoveryRate) * discountFactor * (1 - survivalProbability);

    return frontendProtection;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
