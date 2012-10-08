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

  // TODO : Add a method to calc both the legs in one method (useful for performance reasons e.g. not computing survival probabilities and discount factors twice)
  // TODO : Check the calculation of the accrued premium carefully
  // TODO : If valuationDate = adjustedMatDate - 1day have to be more careful in how the contingent leg integral is calculated
  // TODO : Replace the contingent leg calculation with a faster approximation
  // TODO : Fix the bug when val date is very close to mat date
  // TODO : Fix the bug in the contingent leg calc (the difference from the ISDA calc is in the survival probabilities)
  // TODO : Revisit calculation of where in the sequence of cashflows valuationDate is (check that it is correct)

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
    double presentValue = -(cds.getPremiumLegCoupon() / 10000.0) * presentValuePremiumLeg; // + presentValueContingentLeg;

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

    // Check if the valuationDate equals the adjusted effective date (have to do this after the schedule is constructed)
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
  private double calculatePremiumLeg(CreditDefaultSwapDefinition cds, YieldCurve yieldCurve, SurvivalCurve survivalCurve) {

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

    // If the valuationDate is after the adjusted maturity date then throw an exception (differs from check in ctor because of the adjusted maturity date)
    ArgumentChecker.isTrue(!valuationDate.isAfter(adjustedMaturityDate), "Valuation date {} must be on or before the adjusted maturity date {}", valuationDate, adjustedMaturityDate);

    // If the valuation date is exactly the adjusted maturity date then simply return zero
    if (valuationDate.equals(adjustedMaturityDate)) {
      return 0.0;
    }
    // -------------------------------------------------------------

    // Determine where in the cashflow schedule the valuationDate is
    while (!valuationDate.isBefore(premiumLegSchedule[counter][0].minusDays(1))) {
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
        double tPrevious = TimeCalculator.getTimeBetween(valuationDate, premiumLegSchedule[i - 1][0], dayCount);
        double survivalProbabilityPrevious = survivalCurve.getSurvivalProbability(tPrevious);

        presentValueAccruedPremium += 0.5 * dcf * discountFactor * (survivalProbabilityPrevious - survivalProbability);
      }
    }

    // -------------------------------------------------------------

    // Check these calcs carefully - not right yet

    /*
    // Calculate the additional accrued premiums when we are not standing on a coupon payment date
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
     */

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
    double protectionPeriod = TimeCalculator.getTimeBetween(valuationDate, adjustedMaturityDate, cds.getDayCountFractionConvention());

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

      double survivalProbability = survivalCurve.getSurvivalProbability(t);
      double survivalProbabilityPrevious = survivalCurve.getSurvivalProbability(tPrevious);

      presentValueContingentLeg += discountFactor * (survivalProbabilityPrevious - survivalProbability);
    }

    // -------------------------------------------------------------

    return cds.getNotional() * (1 - cds.getRecoveryRate()) * presentValueContingentLeg;
  }

  // -------------------------------------------------------------------------------------------------
}
