/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.indexcreditdefaultswap.pricing;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.legacy.LegacyVanillaCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.vanilla.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratecurve.HazardRateCurve;
import com.opengamma.analytics.financial.credit.indexcreditdefaultswap.definition.IndexCreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.isdayieldcurve.ISDADateCurve;
import com.opengamma.analytics.financial.credit.schedulegeneration.GenerateCreditDefaultSwapPremiumLegSchedule;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.ArgumentChecker;

/**
 * Class containing methods for the valuation of an index CDS
 */
public class PresentValueIndexCreditDefaultSwap {

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // TODO : Work - in - Progress

  // TODO : Make sure the Buy/sell convention is coded correctly
  // TODO : Need to make sure sort out the LegacyVanillaCreditDefaultSwapDefinition etc issues with the object hierarchy
  // TODO : Add a method to value the index swap using SNCDS analytics
  // TODO : Replace the intrinsic calculations with those based on the ISDA SNCDS model for the individual constituents

  // NOTE : We pass in market data as vectors of objects. That is, we pass in a vector of yieldCurves
  // NOTE : (hence in principle each Obligor in the UnderlyingPool can have their cashflows discounted
  // NOTE : by a different discount curve) and we pass in a vector of calibrated hazard rate term structures
  // NOTE : (one for each Obligor in the UnderlyingPool)

  // ----------------------------------------------------------------------------------------------------------------------------------------

  private static final GenerateCreditDefaultSwapPremiumLegSchedule PREMIUM_LEG_SCHEDULE_CALCULATOR = new GenerateCreditDefaultSwapPremiumLegSchedule();

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the present value of an Index CDS position treating the index as a SNCDS

  public double getPresentValueIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final CreditDefaultSwapDefinition cds,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    double presentValue = 0.0;

    return presentValue;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the present value of an Index CDS position based on the values of its intrinsic constituents

  public double getIntrinsicPresentValueIndexCreditDefaultSwap(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final double[] breakevenSpreads,
      final ISDADateCurve[] yieldCurves,
      final HazardRateCurve[] hazardRateCurves) {

    // ----------------------------------------------------------------------------------------------------------------------------------------

    ArgumentChecker.notNull(valuationDate, "Valuation date");
    ArgumentChecker.notNull(indexCDS, "Index CDS");
    ArgumentChecker.notNull(yieldCurves, "Yield Curves");
    ArgumentChecker.notNull(hazardRateCurves, "Hazard Rate Curves");

    for (int i = 0; i < indexCDS.getUnderlyingPool().getNumberOfObligors(); i++) {
      ArgumentChecker.notNegative(breakevenSpreads[i], "breakeven spread");
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    // Calculate the value of the premium leg (including accrued if required)
    double presentValueIndexPremiumLeg = (indexCDS.getIndexCoupon() / 10000.0) * calculateIndexPremiumLeg(valuationDate, indexCDS, yieldCurves, hazardRateCurves);

    // Calculate the value of the contingent leg
    double presentValueIndexContingentLeg = calculateIndexContingentLeg(valuationDate, indexCDS, breakevenSpreads, yieldCurves, hazardRateCurves);

    // Calculate the PV of the CDS (assumes we are buying protection i.e. paying the premium leg, receiving the contingent leg)
    double presentValue = -presentValueIndexPremiumLeg + presentValueIndexContingentLeg;

    // If we are selling protection, then reverse the direction of the premium and contingent leg cashflows
    if (indexCDS.getBuySellProtection() == BuySellProtection.SELL) {
      presentValue = -1 * presentValue;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------

    return presentValue;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the present value of an Index CDS premium leg
  private double calculateIndexPremiumLeg(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final ISDADateCurve[] yieldCurves,
      final HazardRateCurve[] hazardRateCurves) {

    double[] breakevenSpreads = new double[indexCDS.getUnderlyingPool().getNumberOfObligors()];

    // Build a vector of dummy breakeven spreads
    for (int i = 0; i < breakevenSpreads.length; i++) {
      breakevenSpreads[i] = 1.0;
    }

    // Calculate the intrinsic value of the premium leg
    final double presentValueIndexPremiumLeg = calculateIndexRiskydV01(valuationDate, indexCDS, breakevenSpreads, yieldCurves, hazardRateCurves);

    return indexCDS.getNotional() * presentValueIndexPremiumLeg;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the present value of an Index CDS contingent leg
  private double calculateIndexContingentLeg(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final double[] breakevenSpreads,
      final ISDADateCurve[] yieldCurves,
      final HazardRateCurve[] hazardRateCurves) {

    // Calculate the intrinsic value of the premium leg
    final double presentValueIndexContingentLeg = calculateIndexRiskydV01(valuationDate, indexCDS, breakevenSpreads, yieldCurves, hazardRateCurves);

    return indexCDS.getNotional() * presentValueIndexContingentLeg;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the risky dV01 for all the index constituents 

  private double calculateIndexRiskydV01(
      final ZonedDateTime valuationDate,
      final IndexCreditDefaultSwapDefinition indexCDS,
      final double[] breakevenSpreads,
      final ISDADateCurve[] yieldCurves,
      final HazardRateCurve[] hazardRateCurves) {

    double indexRiskydV01 = 0.0;

    final int numberofObligors = indexCDS.getUnderlyingPool().getNumberOfObligors();

    for (int i = 0; i < numberofObligors; i++) {

      // Extract out the CDS associated with obligor i
      LegacyVanillaCreditDefaultSwapDefinition cds = (LegacyVanillaCreditDefaultSwapDefinition) indexCDS.getUnderlyingCDS()[i];

      // Calculate the risky dV01 for obligor i
      final double riskydV01 = calculateRiskydV01(valuationDate, cds, breakevenSpreads[i], yieldCurves[i], hazardRateCurves[i]);

      // Add this to the running total
      indexRiskydV01 += riskydV01;
    }

    return indexRiskydV01 / numberofObligors;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------

  // Calculate the risky dV01 for an individual obligor

  private double calculateRiskydV01(
      final ZonedDateTime valuationDate,
      final LegacyVanillaCreditDefaultSwapDefinition cds,
      final double breakevenSpread,
      final ISDADateCurve yieldCurve,
      final HazardRateCurve hazardRateCurve) {

    double riskydV01 = 0.0;

    // Construct the schedule of payments on the premium leg
    final ZonedDateTime[] premiumLegSchedule = PREMIUM_LEG_SCHEDULE_CALCULATOR.constructCreditDefaultSwapPremiumLegSchedule(cds);

    // Loop  over each payment date in the schedule
    for (int i = 1; i < premiumLegSchedule.length; i++) {

      // Does this payment occur in the future ...
      if (premiumLegSchedule[i].isAfter(valuationDate)) {

        // ... yes, add it to the risky discounted cashflows

        final double timeToPreviousCashflow = TimeCalculator.getTimeBetween(valuationDate, premiumLegSchedule[i - 1]);
        final double timeToCurrentCashflow = TimeCalculator.getTimeBetween(valuationDate, premiumLegSchedule[i]);
        final double dcf = TimeCalculator.getTimeBetween(premiumLegSchedule[i - 1], premiumLegSchedule[i]);

        final double discountFactor = yieldCurve.getDiscountFactor(timeToCurrentCashflow);

        final double survivalProbabilityToPreviousCashflow = hazardRateCurve.getSurvivalProbability(timeToPreviousCashflow);
        final double survivalProbabilityToCurrentCashflow = hazardRateCurve.getSurvivalProbability(timeToCurrentCashflow);

        riskydV01 += (dcf * discountFactor * (survivalProbabilityToPreviousCashflow + survivalProbabilityToCurrentCashflow));
      }
    }

    return 0.5 * (breakevenSpread / 10000.0) * riskydV01;
  }

  // ----------------------------------------------------------------------------------------------------------------------------------------
}
