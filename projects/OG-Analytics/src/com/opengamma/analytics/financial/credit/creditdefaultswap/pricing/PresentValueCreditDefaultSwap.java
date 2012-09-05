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

/**
 *  Class containing methods for the valuation of a legacy vanilla CDS (and its constituent legs)
 */
public class PresentValueCreditDefaultSwap {

  // -------------------------------------------------------------------------------------------------

  // TODO : Lots of work to do in this file
  // TODO : Add corrections for seasoned trades (currently just valuing at t = 0)
  // TODO : Might need to dumb down the 'TimeCalculator' calcs (to not include leap year calcs - can we turn this off)
  // TODO : The 'TimeCalculator' is ACT/ACT where the denominator can be 365 or 366 - calcs fraction of year period falls in a leap year etc
  // TODO : Add a method to calc both the legs in one go (is this useful or not? Might be useful from a speed perspective - remember can have O(10^5) positions in a book)

  // -------------------------------------------------------------------------------------------------

  // Public method for computing the PV of a CDS based on an input CDS contract (work-in-progress) 
  public double getNewPresentValueCreditDefaultSwap(CreditDefaultSwapDefinition cds) {

    // -------------------------------------------------------------

    //double presentValue = 0.0;
    double presentValuePremiumLeg = 0.0;
    double presentValueAccruedPremium = 0.0;
    double presentValueContingentLeg = 0.0;

    // -------------------------------------------------------------

    // Get the relevant contract data needed to value the premium leg

    // Get the notional amount and par CDS spread (in bps, therefore divide by 10,000) to multiply the premium leg by
    double notional = cds.getNotional();
    double parSpread = cds.getParSpread() / 10000.0;

    //double curveRecoveryRate = cds.getCurveRecoveryRate();
    //double hazardRate = parSpread / (1 - curveRecoveryRate);

    // get the yield and survival curves
    YieldCurve yieldCurve = cds.getYieldCurve();
    SurvivalCurve survivalCurve = cds.getSurvivalCurve();

    double hazardRate = survivalCurve.getFlatHazardRate();

    // Do we need to calculate the accrued premium as well
    boolean includeAccruedPremium = cds.getIncludeAccruedPremium();

    // Extract the adjusted effective date from the computed cashflow schedule
    //ZonedDateTime adjustedEffectiveDate = cashflowSchedule[0][0];

    ZonedDateTime valuationDate = cds.getValuationDate();

    // -------------------------------------------------------------

    // Calculate the PV of the CDS (assumes we are buying protection i.e. paying the premium leg, receiving the contingent leg)
    double presentValue = -presentValuePremiumLeg + presentValueContingentLeg;

    // If we are selling protection, then reverse the direction of the premium and contingent leg cashflows
    if (cds.getBuySellProtection() == BuySellProtection.SELL) {
      presentValue = -1 * presentValue;
    }

    return presentValue;
  }

  // -------------------------------------------------------------------------------------------------

  // Public method for computing the PV of a CDS based on an input CDS contract
  public double getPresentValueCreditDefaultSwap(CreditDefaultSwapDefinition cds) {

    // Construct a cashflow schedule object
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Build the premium leg cashflow schedule from the contract specification
    ZonedDateTime[][] premiumLegSchedule = cashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);

    // Calculate the value of the premium leg (including accrued)
    double presentValuePremiumLeg = calculatePremiumLeg(cds, premiumLegSchedule);

    // Calculate the value of the contingent leg
    double presentValueContingentLeg = calculateContingentLeg(cds, premiumLegSchedule);

    // Calculate the PV of the CDS (assumes we are buying protection i.e. paying the premium leg, receiving the contingent leg)
    double presentValue = -presentValuePremiumLeg + presentValueContingentLeg;

    // If we are selling protection, then reverse the direction of the premium and contingent leg cashflows
    if (cds.getBuySellProtection() == BuySellProtection.SELL) {
      presentValue = -1 * presentValue;
    }

    return presentValue;
  }

  // -------------------------------------------------------------------------------------------------

  // Public method to calculate the par spread of a CDS at contract inception
  public double getParSpreadCreditDefaultSwap(CreditDefaultSwapDefinition cds, ZonedDateTime[] cashflowSchedule) {

    double parSpread = 0.0;

    return parSpread;
  }

  //-------------------------------------------------------------------------------------------------

  // Method to calculate the value of the premium leg of a CDS 
  double calculatePremiumLeg(CreditDefaultSwapDefinition cds, ZonedDateTime[][] cashflowSchedule) {

    // -------------------------------------------------------------

    double presentValuePremiumLeg = 0.0;
    double presentValueAccruedPremium = 0.0;

    // -------------------------------------------------------------

    // Get the relevant contract data needed to value the premium leg

    // Get the notional amount and par CDS spread (in bps, therefore divide by 10,000) to multiply the premium leg by
    double notional = cds.getNotional();
    double parSpread = cds.getParSpread() / 10000.0;

    // get the yield and survival curves
    YieldCurve yieldCurve = cds.getYieldCurve();
    SurvivalCurve survivalCurve = cds.getSurvivalCurve();

    // Compute the (flat) hazard rate for this simple curve
    double hazardRate = survivalCurve.getFlatHazardRate();

    // Do we need to calculate the accrued premium as well
    boolean includeAccruedPremium = cds.getIncludeAccruedPremium();

    // Extract the adjusted effective date from the computed cashflow schedule
    ZonedDateTime adjustedEffectiveDate = cashflowSchedule[0][0];

    // -------------------------------------------------------------

    // Loop through all the elements in the cashflow schedule (note limits of loop)
    for (int i = 1; i < cashflowSchedule.length; i++) {

      // Compute the daycount fraction between this cashflow and the last
      double dcf = cds.getDayCountFractionConvention().getDayCountFraction(cashflowSchedule[i - 1][0], cashflowSchedule[i][0]);

      // Calculate the time between the adjusted effective date (time at which surv prob is unity) and the current cashflow
      double t = TimeCalculator.getTimeBetween(adjustedEffectiveDate, cashflowSchedule[i][0]);

      // Get the discount factor and survival probability
      double discountFactor = yieldCurve.getDiscountFactor(t);
      double survivalProbability = survivalCurve.getSurvivalProbability(hazardRate, t);

      presentValuePremiumLeg += dcf * discountFactor * survivalProbability;

      // If required, calculate the accrued premium contribution to the overall premium leg
      if (includeAccruedPremium) {
        double tPrevious = TimeCalculator.getTimeBetween(cds.getEffectiveDate(), cashflowSchedule[i - 1][0]);
        double survivalProbabilityPrevious = survivalCurve.getSurvivalProbability(hazardRate, tPrevious);

        presentValueAccruedPremium += 0.5 * dcf * discountFactor * (survivalProbabilityPrevious - survivalProbability);
      }
    }

    // -------------------------------------------------------------

    return parSpread * notional * (presentValuePremiumLeg + presentValueAccruedPremium);

    // -------------------------------------------------------------
  }

  // -------------------------------------------------------------------------------------------------

  // Method to calculate the accrued premium of a CDS premium leg (this method is just to allow a user to calculate the accrued on its own)
  double calculateAccruedPremium(CreditDefaultSwapDefinition cds) {

    double presentValueAccruedPremium = 0.0;

    return presentValueAccruedPremium;
  }

  // -------------------------------------------------------------------------------------------------

  // Method to calculate the value of the contingent leg of a CDS
  double calculateContingentLeg(CreditDefaultSwapDefinition cds, ZonedDateTime[][] cashflowSchedule) {

    double presentValueContingentLeg = 0.0;

    // Get the notional amount to multiply the contingent leg by
    double notional = cds.getNotional();
    double parSpread = cds.getParSpread() / 10000.0;

    double valuationRecoveryRate = cds.getValuationRecoveryRate();

    // get the yield and survival curves
    YieldCurve yieldCurve = cds.getYieldCurve();
    SurvivalCurve survivalCurve = cds.getSurvivalCurve();

    // Get the (flat) hazard rate
    double hazardRate = survivalCurve.getFlatHazardRate();

    int numberOfIntegrationSteps = cds.getNumberOfIntegrationSteps();

    // Calculate the protection leg integral between the adjustedEffectiveDate and maturityDate
    ZonedDateTime adjustedEffectiveDate = cashflowSchedule[0][0];
    ZonedDateTime immAdjustedMaturityDate = cashflowSchedule[cashflowSchedule.length - 1][0];

    // Calculate the discretisation of the time axis
    double timeInterval = TimeCalculator.getTimeBetween(adjustedEffectiveDate, immAdjustedMaturityDate);
    int numberOfPartitions = (int) (numberOfIntegrationSteps * timeInterval + 0.5);
    double epsilon = timeInterval / numberOfPartitions;

    // Calculate the integral for the contingent leg
    for (int k = 1; k <= numberOfPartitions; k++) {
      double t = k * epsilon;
      double tPrevious = (k - 1) * epsilon;

      double discountFactor = yieldCurve.getDiscountFactor(t);
      double survivalProbability = survivalCurve.getSurvivalProbability(hazardRate, t);
      double survivalProbabilityPrevious = survivalCurve.getSurvivalProbability(hazardRate, tPrevious);

      presentValueContingentLeg += discountFactor * (survivalProbabilityPrevious - survivalProbability);
    }

    return notional * (1 - valuationRecoveryRate) * presentValueContingentLeg;
  }

  // -------------------------------------------------------------------------------------------------
}
