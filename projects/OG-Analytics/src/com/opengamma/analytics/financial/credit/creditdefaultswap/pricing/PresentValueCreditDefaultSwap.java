/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.credit.hazardratemodel.HazardRateCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.util.ArgumentChecker;

/**
 *  Class containing methods for the valuation of a vanilla CDS
 */
public class PresentValueCreditDefaultSwap {

  // -------------------------------------------------------------------------------------------------

  private static final DayCount ACT_365 = DayCountFactory.INSTANCE.getDayCount("ACT/365");

  // Set the number of partitions to divide the timeline up into for the valuation of the contingent leg

  private static final int DEFAULT_N_POINTS = 30;
  private final int _numberOfIntegrationSteps;

  public PresentValueCreditDefaultSwap() {
    this(DEFAULT_N_POINTS);
  }

  public PresentValueCreditDefaultSwap(int numberOfIntegrationPoints) {
    _numberOfIntegrationSteps = numberOfIntegrationPoints;
  }

  // -------------------------------------------------------------------------------------------------

  // TODO : Lots of work to do in this file - Work In Progress

  // TODO : Add a method to calc both the legs in one method (useful for performance reasons e.g. not computing survival probabilities and discount factors twice)
  // TODO : Check the calculation of the accrued premium carefully
  // TODO : If valuationDate = adjustedMatDate - 1day have to be more careful in how the contingent leg integral is calculated
  // TODO : Fix the bug when val date is very close to mat date

  // -------------------------------------------------------------------------------------------------

  // Public method for computing the PV of a CDS based on an input CDS contract (with a hazard rate curve calibrated to market observed data)
  public double getPresentValueCreditDefaultSwap(CreditDefaultSwapDefinition cds, YieldCurve yieldCurve, HazardRateCurve hazardRateCurve) {

    // -------------------------------------------------------------

    // Check input CDS, YieldCurve and SurvivalCurve objects are not null
    ArgumentChecker.notNull(cds, "CreditDefaultSwapDefinition field");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve field");
    ArgumentChecker.notNull(hazardRateCurve, "HazardRateCurve field");

    // -------------------------------------------------------------

    double presentValueAccruedPremium = 0.0;

    // Calculate the value of the premium leg (including accrued if required)
    double presentValuePremiumLeg = calculatePremiumLeg(cds, yieldCurve, hazardRateCurve);

    // Calculate the value of the contingent leg
    double presentValueContingentLeg = calculateISDAContingentLeg(cds, yieldCurve, hazardRateCurve);

    if (cds.getIncludeAccruedPremium()) {
      presentValueAccruedPremium = calculateAccruedPremium(cds, yieldCurve, hazardRateCurve);
    }

    // Calculate the PV of the CDS (assumes we are buying protection i.e. paying the premium leg, receiving the contingent leg)
    double presentValue = -(cds.getPremiumLegCoupon() / 10000.0) * (presentValuePremiumLeg + presentValueAccruedPremium) + presentValueContingentLeg;

    // If we are selling protection, then reverse the direction of the premium and contingent leg cashflows
    if (cds.getBuySellProtection() == BuySellProtection.SELL) {
      presentValue = -1 * presentValue;
    }

    // -------------------------------------------------------------

    return presentValue;
  }

  //-------------------------------------------------------------------------------------------------  

  // Public method to calculate the par spread of a CDS at contract inception (with a hazard rate curve calibrated to market observed data)
  public double getParSpreadCreditDefaultSwap(CreditDefaultSwapDefinition cds, YieldCurve yieldCurve, HazardRateCurve hazardRateCurve) {

    // -------------------------------------------------------------

    // Check input CDS, YieldCurve and SurvivalCurve objects are not null
    ArgumentChecker.notNull(cds, "CDS field");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve field");
    ArgumentChecker.notNull(hazardRateCurve, "HazardRateCurve field");

    // -------------------------------------------------------------

    double parSpread = 0.0;

    // -------------------------------------------------------------

    // Construct a cashflow schedule object
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Check if the valuationDate equals the adjusted effective date (have to do this after the schedule is constructed)
    ArgumentChecker.isTrue(cds.getValuationDate().equals(cashflowSchedule.getAdjustedEffectiveDate(cds)), "Valuation Date should equal the adjusted effective date when computing par spreads");

    // -------------------------------------------------------------

    // Calculate the value of the premium leg (including accrued if required)
    double presentValuePremiumLeg = calculatePremiumLeg(cds, yieldCurve, hazardRateCurve);

    // Calculate the value of the contingent leg
    double presentValueContingentLeg = calculateContingentLeg(cds, yieldCurve, hazardRateCurve);

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

  // Method to calculate the value of the premium leg of a CDS (with a hazard rate curve calibrated to market observed data)
  private double calculatePremiumLeg(CreditDefaultSwapDefinition cds, YieldCurve yieldCurve, HazardRateCurve hazardRateCurve) {

    // -------------------------------------------------------------

    // Local variable definitions

    int counter = 1;

    double presentValuePremiumLeg = 0.0;

    double offset = 0.0;

    if (cds.getProtectionStart()) {
      offset = cds.getProtectionOffset();
    }

    // -------------------------------------------------------------

    // Construct a cashflow schedule object
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Build the premium leg cashflow schedule from the contract specification
    ZonedDateTime[] premiumLegSchedule = cashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);

    /*
    double startTime = TimeCalculator.getTimeBetween(cds.getValuationDate(), cds.getStartDate(), ACT_365);
    double maturity = TimeCalculator.getTimeBetween(cds.getValuationDate(), cds.getMaturityDate(), ACT_365);
    double offsetMaturityTime = maturity + offset;
    boolean includeSchedule = true;
    double[] timeNodes = cashflowSchedule.constructISDACompliantCashflowSchedule(cds, yieldCurve, hazardRateCurve, startTime, offsetMaturityTime, includeSchedule);
     */

    // -------------------------------------------------------------

    // Get the relevant contract data needed to value the premium leg

    // Get the date on which we want to calculate the MtM
    ZonedDateTime valuationDate = cds.getValuationDate();

    // Get the (adjusted) maturity date of the trade
    ZonedDateTime adjustedMaturityDate = cashflowSchedule.getAdjustedMaturityDate(cds);

    // -------------------------------------------------------------

    // If the valuationDate is after the adjusted maturity date then throw an exception (differs from check in ctor because of the adjusted maturity date)
    ArgumentChecker.isTrue(!valuationDate.isAfter(adjustedMaturityDate), "Valuation date {} must be on or before the adjusted maturity date {}", valuationDate, adjustedMaturityDate);

    // If the valuation date is exactly the adjusted maturity date then simply return zero
    if (valuationDate.equals(adjustedMaturityDate)) {
      return 0.0;
    }
    // -------------------------------------------------------------

    // Determine where in the cashflow schedule the valuationDate is
    while (!valuationDate.isBefore(premiumLegSchedule[counter].minusDays(1))) {
      counter++;
    }

    // -------------------------------------------------------------

    // Calculate the value of the remaining premium and accrual payments (due after valuationDate) 
    for (int i = counter; i < premiumLegSchedule.length; i++) {

      // Get the beginning and end dates of the current coupon
      ZonedDateTime accrualStart = premiumLegSchedule[i - 1];
      ZonedDateTime accrualEnd = premiumLegSchedule[i];

      // Calculate the time between the valuation date (time at which survival probability is unity) and the current cashflow
      double t = TimeCalculator.getTimeBetween(valuationDate, accrualEnd, ACT_365);

      // If protection starts at the beginning of the period ...
      if (cds.getProtectionStart()) {

        // ... Roll all but the last date back by 1/365 of a year 
        if (i < premiumLegSchedule.length - 1) {
          t -= cds.getProtectionOffset();
        }

        // This is a bit of a hack - need a more elegant way of dealing with the timing nuances
        if (i == 1) {
          accrualStart = accrualStart.minusDays(1);
        }

        // ... Roll the final maturity date forward by one day
        if (i == premiumLegSchedule.length - 1) {
          accrualEnd = accrualEnd.plusDays(1);
        }
      }

      // Compute the daycount fraction for the current accrual period
      double dcf = cds.getDayCountFractionConvention().getDayCountFraction(accrualStart, accrualEnd);

      // Get the discount factor and survival probability at this time (need to fix the t in the disc factor)
      double discountFactor = yieldCurve.getDiscountFactor(t);
      double survivalProbability = hazardRateCurve.getSurvivalProbability(t);

      presentValuePremiumLeg += dcf * discountFactor * survivalProbability;
    }

    // -------------------------------------------------------------

    return cds.getNotional() * presentValuePremiumLeg;

    // -------------------------------------------------------------
  }

  // -------------------------------------------------------------------------------------------------

  // Method to calculate the value of the accrued premium leg of a CDS (with a hazard rate curve calibrated to market observed data) - WIP
  private double calculateAccruedPremium(CreditDefaultSwapDefinition cds, YieldCurve yieldCurve, HazardRateCurve hazardRateCurve) {

    double presentValueAccruedPremium = 0.0;

    double offset = 0.0;

    if (cds.getProtectionStart()) {
      offset = cds.getProtectionOffset();
    }

    double startTime = TimeCalculator.getTimeBetween(cds.getValuationDate(), cds.getStartDate(), ACT_365);

    double maturity = TimeCalculator.getTimeBetween(cds.getValuationDate(), cds.getMaturityDate(), ACT_365);

    double offsetMaturityTime = maturity + offset;

    boolean includeSchedule = true;

    // Construct a cashflow schedule object
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Build the accrual leg cashflow schedule from the contract specification
    double[] timeNodes = cashflowSchedule.constructISDACompliantCashflowSchedule(cds, yieldCurve, hazardRateCurve, startTime, offsetMaturityTime, includeSchedule);

    int startIndex, endIndex = 0;

    ZonedDateTime[] premiumLegSchedule = cashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);

    for (int i = 0; i < premiumLegSchedule.length; i++) {

      ZonedDateTime accrualStart = premiumLegSchedule[i - 1];
      ZonedDateTime accrualEnd = premiumLegSchedule[i];

      double dcf = cds.getDayCountFractionConvention().getDayCountFraction(accrualStart, accrualEnd);

      double amount = dcf;

      // Calculate the time between the valuation date (time at which survival probability is unity) and the current cashflow
      double t = TimeCalculator.getTimeBetween(cds.getValuationDate(), accrualEnd, ACT_365);

      // If protection starts at the beginning of the period ...
      if (cds.getProtectionStart()) {

        // ... Roll all but the last date back by 1/365 of a year 
        if (i < premiumLegSchedule.length - 1) {
          t -= cds.getProtectionOffset();
        }

        // ... Roll the final maturity date forward by one day
        if (i == premiumLegSchedule.length - 1) {
          accrualEnd = accrualEnd.plusDays(1);
        }
      }

      startIndex = endIndex;
      while (timeNodes[endIndex] < t) {
        ++endIndex;
      }

      startTime = timeNodes[startIndex];
      final double endTime = timeNodes[endIndex];

      final double stepInTime = TimeCalculator.getTimeBetween(cds.getStartDate(), cds.getEffectiveDate(), ACT_365);

      final double offsetStepinTime = stepInTime - offset;

      final double subStartTime = stepInTime > startTime ? stepInTime : startTime;
      final double accrualRate = amount / (endTime - startTime);

      double t0 = subStartTime - startTime + 0.5 / 365.0; //HALF_DAY_ACT_365F;
      double survival0 = hazardRateCurve.getSurvivalProbability(subStartTime);
      double discount0 = 1.0;

      double t1;
      double survival1;

      double value = 0.0;

      for (int j = startIndex + 1; j <= endIndex; ++j) {

        if (timeNodes[j] <= stepInTime) {
          continue;
        }

        t1 = timeNodes[j] - startTime + 0.5 / 365.0; //HALF_DAY_ACT_365F;
        double dt = t1 - t0;

        survival1 = hazardRateCurve.getSurvivalProbability(timeNodes[j]);
        double discount1 = 1.0; //discountFactors[i];

        double lambda = Math.log(survival0 / survival1) / dt;
        double fwdRate = Math.log(discount0 / discount1) / dt;

        double lambdaFwdRate = lambda + fwdRate + 1.0e-50;

        double valueForTimeStep = lambda * accrualRate * survival0 * discount0
            * (((t0 + 1.0 / lambdaFwdRate) / lambdaFwdRate) - ((t1 + 1.0 / lambdaFwdRate) / lambdaFwdRate) * survival1 / survival0 * discount1 / discount0);

        value += valueForTimeStep;

        t0 = t1;
        survival0 = survival1;
        discount0 = discount1;
      }

      presentValueAccruedPremium += value;

    }

    return presentValueAccruedPremium;
  }

  // -------------------------------------------------------------------------------------------------

  // Method to calculate the contingent leg in the same way as the ISDA model
  private double calculateISDAContingentLeg(CreditDefaultSwapDefinition cds, YieldCurve yieldCurve, HazardRateCurve hazardRateCurve) {

    double presentValueContingentLeg = 0.0;

    double offset = 0.0;

    if (cds.getProtectionStart()) {
      offset = cds.getProtectionOffset();
    }

    // Effective date (assuming T + 1 effective date)
    double stepInTime = TimeCalculator.getTimeBetween(cds.getValuationDate(), cds.getValuationDate().plusDays(1), ACT_365);

    // Maturity of CDS
    double maturity = TimeCalculator.getTimeBetween(cds.getValuationDate(), cds.getMaturityDate(), ACT_365);

    // Beginning of CDS contract wrt valuation date
    double startTime = TimeCalculator.getTimeBetween(cds.getValuationDate(), cds.getStartDate(), ACT_365);

    final double offsetPricingTime = -offset;
    final double offsetStepinTime = stepInTime - offset;
    final double protectionStartTime = Math.max(Math.max(startTime, offsetStepinTime), offsetPricingTime);

    GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Calculate the time nodes in the range [protectionStartTime, maturity] on which to evaluate the contingent leg integral
    double[] timeNodes = cashflowSchedule.constructISDACompliantCashflowSchedule(cds, yieldCurve, hazardRateCurve, protectionStartTime, maturity, false);

    double survivalProbability = hazardRateCurve.getSurvivalProbability(timeNodes[0]);
    double discountFactor = yieldCurve.getDiscountFactor(timeNodes[0]); //timePoints[0] > PRICING_TIME ? discountFactors[0] : 1.0;

    for (int i = 1; i < timeNodes.length; ++i) {

      double deltat = timeNodes[i] - timeNodes[i - 1];

      double survivalProbabilityPrevious = survivalProbability;
      double discountFactorPrevious = discountFactor;

      survivalProbability = hazardRateCurve.getSurvivalProbability(timeNodes[i]);
      discountFactor = yieldCurve.getDiscountFactor(timeNodes[i]); //discountFactors[i];

      double lambda = Math.log(survivalProbabilityPrevious / survivalProbability) / deltat;
      double fwdRate = Math.log(discountFactorPrevious / discountFactor) / deltat;

      presentValueContingentLeg += (lambda / (lambda + fwdRate)) * (1.0 - Math.exp(-(lambda + fwdRate) * deltat)) * survivalProbabilityPrevious * discountFactorPrevious;
    }

    return cds.getNotional() * (1 - cds.getRecoveryRate()) * presentValueContingentLeg;
  }

  // -------------------------------------------------------------------------------------------------

  // Method to calculate the value of the contingent leg of a CDS (with a hazard rate curve calibrated to market observed data)
  private double calculateContingentLeg(CreditDefaultSwapDefinition cds, YieldCurve yieldCurve, HazardRateCurve hazardRateCurve) {

    // -------------------------------------------------------------

    // Construct a schedule generation object (to access the adjusted maturity date method)
    GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Get the date when protection begins
    ZonedDateTime valuationDate = cds.getValuationDate();

    // Get the date when protection ends
    ZonedDateTime adjustedMaturityDate = cashflowSchedule.getAdjustedMaturityDate(cds);

    // -------------------------------------------------------------

    // If the valuationDate is after the adjusted maturity date then throw an exception (differs from check in ctor because of the adjusted maturity date)
    ArgumentChecker.isTrue(!valuationDate.isAfter(adjustedMaturityDate), "Valuation date {} must be on or before the adjusted maturity date {}", valuationDate, adjustedMaturityDate);

    // If the valuation date is exactly the adjusted maturity date then simply return zero
    if (valuationDate.equals(adjustedMaturityDate)) {
      return 0.0;
    }

    // -------------------------------------------------------------

    double presentValueContingentLeg = 0.0;

    // -------------------------------------------------------------

    // Calculate the partition of the time axis for the calculation of the integral in the contingent leg

    // The period of time for which protection is provided
    double protectionPeriod = TimeCalculator.getTimeBetween(valuationDate, adjustedMaturityDate.plusDays(1), /*cds.getDayCountFractionConvention()*/ACT_365);

    // Given the protection period, how many partitions should it be divided into
    int numberOfPartitions = (int) (_numberOfIntegrationSteps * protectionPeriod + 0.5);

    // The size of the time increments in the calculation of the integral
    double epsilon = protectionPeriod / numberOfPartitions;

    // -------------------------------------------------------------

    // Calculate the integral for the contingent leg (note the limits of the loop)
    for (int k = 1; k <= numberOfPartitions; k++) {

      double t = k * epsilon;
      double tPrevious = (k - 1) * epsilon;

      double discountFactor = yieldCurve.getDiscountFactor(t);

      double survivalProbability = hazardRateCurve.getSurvivalProbability(t);
      double survivalProbabilityPrevious = hazardRateCurve.getSurvivalProbability(tPrevious);

      presentValueContingentLeg += discountFactor * (survivalProbabilityPrevious - survivalProbability);
    }

    // -------------------------------------------------------------

    return cds.getNotional() * (1.0 - cds.getRecoveryRate()) * presentValueContingentLeg;
  }

  // -------------------------------------------------------------------------------------------------
}
