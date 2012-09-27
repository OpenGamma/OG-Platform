/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.SurvivalCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 *  Class containing methods for the valuation of a vanilla CDS
 */
public class PresentValueCreditDefaultSwap {

  // -------------------------------------------------------------------------------------------------

  // Set the number of partitions to divide the timeline up into for the valuation of the contingent leg

  private static final int DEFAULT_N_POINTS = 100;
  private final int _numberOfIntegrationSteps;

  public PresentValueCreditDefaultSwap() {
    this(DEFAULT_N_POINTS);
  }

  public PresentValueCreditDefaultSwap(int numberOfIntegrationPoints) {
    _numberOfIntegrationSteps = numberOfIntegrationPoints;
  }

  // -------------------------------------------------------------------------------------------------

  // TODO : Lots of work to do in this file - Work In Progress
  // TODO : Revisit exactly when a coupon payment falls off the schedule - is it cashflow date - 1d (ssems to be this in the ISDA model)
  // TODO : Add a method to calc both the legs in one method (useful for performance reasons e.g. not computing survival probabilities and discount factors twice)
  // TODO : Check the calculation of the accrued premium carefully
  // TODO : If valuationDate = adjustedMatDate - 1day have to be careful in how the contingent leg integral is calculated
  // TODO : Check the time decay of the premium leg - are the coupons dropping off on the right day

  // -------------------------------------------------------------------------------------------------

  // Public method for computing the PV of a CDS based on an input CDS contract (with a survival curve calibrated to market observed data)
  public double getPresentValueCreditDefaultSwap(CreditDefaultSwapDefinition cds, YieldCurve yieldCurve, SurvivalCurve survivalCurve) {

    // -------------------------------------------------------------

    // Check input CDS, YieldCurve and SurvivalCurve objects are not null
    ArgumentChecker.notNull(cds, "CreditDefaultSwapDefinition field");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve field");
    ArgumentChecker.notNull(survivalCurve, "SurvivalCurve field");

    // -------------------------------------------------------------

    // Calculate the value of the premium leg (including accrued if required)
    double presentValuePremiumLeg = calculatePremiumLeg(cds, yieldCurve, survivalCurve);

    // Calculate the value of the contingent leg
    double presentValueContingentLeg = calculateContingentLeg(cds, yieldCurve, survivalCurve);

    // Calculate the PV of the CDS (assumes we are buying protection i.e. paying the premium leg, receiving the contingent leg)
    double presentValue = -(cds.getPremiumLegCoupon() / 10000.0) * presentValuePremiumLeg + presentValueContingentLeg;

    // If we are selling protection, then reverse the direction of the premium and contingent leg cashflows
    if (cds.getBuySellProtection() == BuySellProtection.SELL) {
      presentValue = -1 * presentValue;
    }

    // -------------------------------------------------------------

    return presentValue;
  }

  //-------------------------------------------------------------------------------------------------  

  // Public method to calculate the par spread of a CDS at contract inception (with a survival curve calibrated to market observed data)
  public double getParSpreadCreditDefaultSwap(CreditDefaultSwapDefinition cds, YieldCurve yieldCurve, SurvivalCurve survivalCurve) {

    // -------------------------------------------------------------

    // Check input CDS, YieldCurve and SurvivalCurve objects are not null
    ArgumentChecker.notNull(cds, "CDS field");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve field");
    ArgumentChecker.notNull(survivalCurve, "SurvivalCurve field");

    // -------------------------------------------------------------

    double parSpread = 0.0;

    // -------------------------------------------------------------

    // Construct a cashflow schedule object
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Check if the valuationDate equals the adjusted effective date
    ArgumentChecker.isTrue(cds.getValuationDate().equals(cashflowSchedule.getAdjustedEffectiveDate(cds)), "Valuation Date should equal the adjusted effective date when computing par spreads");

    // -------------------------------------------------------------

    // Calculate the value of the premium leg (including accrued if required)
    double presentValuePremiumLeg = calculatePremiumLeg(cds, yieldCurve, survivalCurve);

    // Calculate the value of the contingent leg
    double presentValueContingentLeg = calculateContingentLeg(cds, yieldCurve, survivalCurve);

    // -------------------------------------------------------------

    // Calculate the par spread (NOTE : Returned value is in bps)
    if (Double.doubleToLongBits(presentValuePremiumLeg) == 0.0) {
      throw new IllegalStateException("Warning : The premium leg has a PV of zero - par spread cannot be computed");
    } else {
      parSpread = 10000.0 * presentValueContingentLeg / presentValuePremiumLeg;
    }

    // -------------------------------------------------------------

    return parSpread;
  }

  // -------------------------------------------------------------------------------------------------

  // Method (private) to calculate the value of the premium leg of a CDS (with a survival curve calibrated to market observed data)
  private double calculatePremiumLeg(CreditDefaultSwapDefinition cds, /*ZonedDateTime[][] cashflowSchedule, */YieldCurve yieldCurve, SurvivalCurve survivalCurve) {

    // -------------------------------------------------------------

    // Local variable definitions

    int counter = 1;

    double t = 0.0;
    double tStar = 0.0;

    double dcf = 0.0;
    double dcfStar = 0.0;

    double discountFactor = 0.0;
    double survivalProbability = 0.0;

    double deltaCoupon = 0.0;
    double deltaAccrual = 0.0;
    double presentValuePremiumLeg = 0.0;
    double presentValueAccruedPremium = 0.0;

    // -------------------------------------------------------------

    // Construct a cashflow schedule object
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Build the premium leg cashflow schedule from the contract specification
    ZonedDateTime[][] premiumLegSchedule = cashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);

    // -------------------------------------------------------------

    // Get the relevant contract data needed to value the premium leg

    // Get the date on which we want to calculate the MtM
    ZonedDateTime valuationDate = cds.getValuationDate();

    // Get the (adjusted) maturity date of the trade
    ZonedDateTime adjustedMaturityDate = cashflowSchedule.getAdjustedMaturityDate(cds);

    // Get the daycount convention
    DayCount dayCount = cds.getDayCountFractionConvention();

    // Do we need to calculate the accrued premium as well
    boolean includeAccruedPremium = cds.getIncludeAccruedPremium();

    // -------------------------------------------------------------

    // If the valuationDate is after the adjusted maturity date then throw an exception
    ArgumentChecker.isTrue(!valuationDate.isAfter(adjustedMaturityDate), "Valuation date {} must be on or before the adjusted maturity date {}", valuationDate, adjustedMaturityDate);

    // -------------------------------------------------------------

    // Determine where in the cashflow schedule the valuationDate is (if valuationDate = a coupon date - 1d, this coupon is not included in the remaining coupons)
    while (valuationDate.isAfter(premiumLegSchedule[counter][0])) {
      counter++;
    }

    // -------------------------------------------------------------

    // Calculate the value of the remaining premium and accrual payments (due after valuationDate) 
    for (int i = counter; i < premiumLegSchedule.length; i++) {

      // Compute the daycount fraction between cashflow i and i - 1 
      dcf = cds.getDayCountFractionConvention().getDayCountFraction(premiumLegSchedule[i - 1][0], premiumLegSchedule[i][0]);

      // Calculate the time between the valuation date (time at which survival probability is unity) and the current cashflow
      t = TimeCalculator.getTimeBetween(valuationDate, premiumLegSchedule[i][0], dayCount);

      // Get the discount factor and survival probability at this time
      discountFactor = yieldCurve.getDiscountFactor(t);
      survivalProbability = survivalCurve.getSurvivalProbability(t);

      presentValuePremiumLeg += dcf * discountFactor * survivalProbability;

      // If required, calculate the accrued premium contribution to the overall premium leg
      if (includeAccruedPremium) {
        double tPrevious = TimeCalculator.getTimeBetween(valuationDate, premiumLegSchedule[i - 1][0]);
        double survivalProbabilityPrevious = survivalCurve.getSurvivalProbability(tPrevious);

        presentValueAccruedPremium += 0.5 * dcf * discountFactor * (survivalProbabilityPrevious - survivalProbability);
      }
    }

    // -------------------------------------------------------------

    // Check these calcs carefully - not right yet

    if (includeAccruedPremium) {

      // Calculate the time of the last premium payment prior to valuationDate (check this)
      tStar = TimeCalculator.getTimeBetween(valuationDate, premiumLegSchedule[counter][0]);

      dcf = cds.getDayCountFractionConvention().getDayCountFraction(premiumLegSchedule[counter - 1][0], valuationDate);
      dcfStar = cds.getDayCountFractionConvention().getDayCountFraction(premiumLegSchedule[counter - 1][0], premiumLegSchedule[counter][0]);

      // Get the discount factor and survival probability
      discountFactor = yieldCurve.getDiscountFactor(tStar);
      survivalProbability = survivalCurve.getSurvivalProbability(tStar);

      deltaCoupon = dcfStar * discountFactor * survivalProbability;
      deltaAccrual = dcf * discountFactor * (1 - survivalProbability);

    }

    // -------------------------------------------------------------

    return cds.getNotional() * (presentValuePremiumLeg + presentValueAccruedPremium + deltaCoupon + deltaAccrual);

    // -------------------------------------------------------------
  }

  // -------------------------------------------------------------------------------------------------

  // Method (private) to calculate the value of the contingent leg of a CDS (with a survival curve calibrated to market observed data)
  private double calculateContingentLeg(CreditDefaultSwapDefinition cds, YieldCurve yieldCurve, SurvivalCurve survivalCurve) {

    // -------------------------------------------------------------

    // Construct a schedule generation object (to access the adjusted maturity date method)
    GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Calculate the protection leg integral between the valuationDate (when protection begins) and adjustedMaturityDate (when protection ends)
    ZonedDateTime valuationDate = cds.getValuationDate();
    ZonedDateTime adjustedMaturityDate = cashflowSchedule.getAdjustedMaturityDate(cds);

    // -------------------------------------------------------------

    // If the valuationDate is after the adjusted maturity date then throw an exception
    ArgumentChecker.isTrue(!valuationDate.isAfter(adjustedMaturityDate), "Valuation date {} must be on or before the adjusted maturity date {}", valuationDate, adjustedMaturityDate);

    // -------------------------------------------------------------

    double presentValueContingentLeg = 0.0;

    // -------------------------------------------------------------

    // Calculate the discretisation interval for the time axis
    double protectionPeriod = TimeCalculator.getTimeBetween(valuationDate, adjustedMaturityDate, cds.getDayCountFractionConvention());
    int numberOfPartitions = (int) (_numberOfIntegrationSteps * protectionPeriod + 0.5);
    double epsilon = protectionPeriod / numberOfPartitions;

    // -------------------------------------------------------------

    // Calculate the integral for the contingent leg
    for (int k = 1; k <= numberOfPartitions; k++) {

      double t = k * epsilon;
      double tPrevious = (k - 1) * epsilon;

      double discountFactor = yieldCurve.getDiscountFactor(t);
      double survivalProbability = survivalCurve.getSurvivalProbability(t);
      double survivalProbabilityPrevious = survivalCurve.getSurvivalProbability(tPrevious);

      presentValueContingentLeg += discountFactor * (survivalProbabilityPrevious - survivalProbability);
    }

    // -------------------------------------------------------------

    return cds.getNotional() * (1 - cds.getValuationRecoveryRate()) * presentValueContingentLeg;
  }

  // -------------------------------------------------------------------------------------------------
}
