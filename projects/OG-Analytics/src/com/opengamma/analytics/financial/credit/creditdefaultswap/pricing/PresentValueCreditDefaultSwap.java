/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.FlatSurvivalCurve;
import com.opengamma.analytics.financial.credit.SurvivalCurve;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.util.ArgumentChecker;

/**
 *  Class containing methods for the valuation of a legacy vanilla CDS (and its constituent legs)
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
  // TODO : Add corrections for seasoned trades (currently just valuing at t = 0)
  // TODO : Add a method to calc both the legs in one method (useful for performance reasons)

  // -------------------------------------------------------------------------------------------------

  // Public method for computing the PV of a CDS based on an input CDS contract (with a flat survival curve - mostly for testing purposes)
  public double getPresentValueCreditDefaultSwap(CreditDefaultSwapDefinition cds, YieldCurve yieldCurve, FlatSurvivalCurve survivalCurve) {

    // Check input CDS, YieldCurve and SurvivalCurve objects are not null
    ArgumentChecker.notNull(cds, "CDS field");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve field");
    ArgumentChecker.notNull(survivalCurve, "SurvivalCurve field");

    // Get the CDS's contractual spread
    double parSpread = cds.getParSpread() / 10000.0;

    // Construct a cashflow schedule object
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Build the premium leg cashflow schedule from the contract specification
    ZonedDateTime[][] premiumLegSchedule = cashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);

    // Calculate the value of the premium leg (including accrued if required)
    double presentValuePremiumLeg = calculatePremiumLeg(cds, premiumLegSchedule, yieldCurve, survivalCurve);

    // Calculate the value of the contingent leg
    double presentValueContingentLeg = calculateContingentLeg(cds, premiumLegSchedule, yieldCurve, survivalCurve);

    // Calculate the PV of the CDS (assumes we are buying protection i.e. paying the premium leg, receiving the contingent leg)
    double presentValue = -parSpread * presentValuePremiumLeg + presentValueContingentLeg;

    // If we are selling protection, then reverse the direction of the premium and contingent leg cashflows
    if (cds.getBuySellProtection() == BuySellProtection.SELL) {
      presentValue = -1 * presentValue;
    }

    return presentValue;
  }

  // -------------------------------------------------------------------------------------------------

  // Public method to calculate the par spread of a CDS at contract inception (with a flat survival curve - mostly for testing purposes)
  public double getParSpreadCreditDefaultSwap(CreditDefaultSwapDefinition cds, YieldCurve yieldCurve, FlatSurvivalCurve survivalCurve) {

    // Check input CDS, YieldCurve and SurvivalCurve objects are not null
    ArgumentChecker.notNull(cds, "CDS field");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve field");
    ArgumentChecker.notNull(survivalCurve, "SurvivalCurve field");

    // Add checks for returning 0.0's etc

    // Construct a cashflow schedule object
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Build the premium leg cashflow schedule from the contract specification
    ZonedDateTime[][] premiumLegSchedule = cashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);

    // Calculate the value of the premium leg (including accrued if required)
    double presentValuePremiumLeg = calculatePremiumLeg(cds, premiumLegSchedule, yieldCurve, survivalCurve);

    // Calculate the value of the contingent leg
    double presentValueContingentLeg = calculateContingentLeg(cds, premiumLegSchedule, yieldCurve, survivalCurve);

    double parSpread = presentValueContingentLeg / presentValuePremiumLeg;

    return parSpread;
  }

  //-------------------------------------------------------------------------------------------------  

  // WIP

  // Public method to calculate the par spread of a CDS at contract inception
  public double getParSpreadCreditDefaultSwap(CreditDefaultSwapDefinition cds, YieldCurve yieldCurve, SurvivalCurve survivalCurve) {

    // Check input CDS, YieldCurve and SurvivalCurve objects are not null
    ArgumentChecker.notNull(cds, "CDS field");
    ArgumentChecker.notNull(yieldCurve, "YieldCurve field");
    ArgumentChecker.notNull(survivalCurve, "SurvivalCurve field");

    // Add checks for returning 0.0's etc

    // Construct a cashflow schedule object
    final GenerateCreditDefaultSwapPremiumLegSchedule cashflowSchedule = new GenerateCreditDefaultSwapPremiumLegSchedule();

    // Build the premium leg cashflow schedule from the contract specification
    ZonedDateTime[][] premiumLegSchedule = cashflowSchedule.constructCreditDefaultSwapPremiumLegSchedule(cds);

    // Calculate the value of the premium leg (including accrued if required)
    //double presentValuePremiumLeg = calculatePremiumLeg(cds, premiumLegSchedule, yieldCurve, survivalCurve);

    // Calculate the value of the contingent leg
    //double presentValueContingentLeg = calculateContingentLeg(cds, premiumLegSchedule, yieldCurve, survivalCurve);

    double parSpread = 0.0; //presentValueContingentLeg / presentValuePremiumLeg;

    return parSpread;
  }

  //-------------------------------------------------------------------------------------------------

  // Method (private) to calculate the value of the premium leg of a CDS (with a flat survival curve - mostly for testing purposes)
  private double calculatePremiumLeg(CreditDefaultSwapDefinition cds, ZonedDateTime[][] cashflowSchedule, YieldCurve yieldCurve, FlatSurvivalCurve survivalCurve) {

    // -------------------------------------------------------------

    int counter = 1;

    double presentValuePremiumLeg = 0.0;
    double presentValueAccruedPremium = 0.0;

    // -------------------------------------------------------------

    // Get the relevant contract data needed to value the premium leg

    // The date on which we want to calculate the MtM
    ZonedDateTime valuationDate = cds.getValuationDate();

    // Get the notional amount and par CDS spread (in bps, therefore divide by 10,000) to multiply the premium leg by
    double notional = cds.getNotional();

    // Get the (flat) hazard rate for this simple curve
    double hazardRate = survivalCurve.getFlatHazardRate();

    // Get the daycount convention
    DayCount dayCount = cds.getDayCountFractionConvention();

    // Do we need to calculate the accrued premium as well
    boolean includeAccruedPremium = cds.getIncludeAccruedPremium();

    // Extract the adjusted effective date from the computed cashflow schedule
    ZonedDateTime adjustedEffectiveDate = cashflowSchedule[0][0];

    // -------------------------------------------------------------

    // Determine where in the cashflow schedule the valuationDate is
    while (valuationDate.isAfter(cashflowSchedule[counter][0])) {
      counter++;
    }

    // -------------------------------------------------------------

    // Loop through all the elements in the cashflow schedule that are after the valuation date(note limits of loop)
    for (int i = counter; i < cashflowSchedule.length; i++) {

      // Compute the daycount fraction between this cashflow and the last
      double dcf = cds.getDayCountFractionConvention().getDayCountFraction(cashflowSchedule[i - 1][0], cashflowSchedule[i][0]);

      // Calculate the time between the valuation date (time at which survival probability is unity) and the current cashflow
      double t = TimeCalculator.getTimeBetween(valuationDate, cashflowSchedule[i][0], dayCount);

      // Get the discount factor and survival probability
      double discountFactor = yieldCurve.getDiscountFactor(t);
      double survivalProbability = survivalCurve.getSurvivalProbability(hazardRate, t);

      presentValuePremiumLeg += dcf * discountFactor * survivalProbability;

      // If required, calculate the accrued premium contribution to the overall premium leg
      if (includeAccruedPremium) {
        // TODO : Check the valuationDate carefully
        double tPrevious = TimeCalculator.getTimeBetween(valuationDate, cashflowSchedule[i - 1][0]);
        double survivalProbabilityPrevious = survivalCurve.getSurvivalProbability(hazardRate, tPrevious);

        presentValueAccruedPremium += 0.5 * dcf * discountFactor * (survivalProbabilityPrevious - survivalProbability);
      }
    }

    // -------------------------------------------------------------

    return notional * (presentValuePremiumLeg + presentValueAccruedPremium);

    // -------------------------------------------------------------
  }

  // -------------------------------------------------------------------------------------------------

  // Method (private) to calculate the accrued premium of a CDS premium leg (with a flat survival curve - mostly for testing purposes)
  private double calculateAccruedPremium(CreditDefaultSwapDefinition cds, YieldCurve yieldCurve, FlatSurvivalCurve survivalCurve) {

    double presentValueAccruedPremium = 0.0;

    return presentValueAccruedPremium;
  }

  // -------------------------------------------------------------------------------------------------

  // Method (private) to calculate the value of the contingent leg of a CDS (with a flat survival curve - mostly for testing purposes)
  private double calculateContingentLeg(CreditDefaultSwapDefinition cds, ZonedDateTime[][] cashflowSchedule, YieldCurve yieldCurve, FlatSurvivalCurve survivalCurve) {

    double presentValueContingentLeg = 0.0;

    // Get the daycount convention
    DayCount dayCount = cds.getDayCountFractionConvention();

    // Get the notional amount to multiply the contingent leg by
    double notional = cds.getNotional();

    // Get the recovery rate used for valuation purposes
    double valuationRecoveryRate = cds.getValuationRecoveryRate();

    // Get the (flat) hazard rate from the survival curve
    double hazardRate = survivalCurve.getFlatHazardRate();

    // Calculate the protection leg integral between the valuationDate (when protection begins) and adjustedMaturityDate (when protection ends)
    ZonedDateTime valuationDate = cds.getValuationDate();
    ZonedDateTime adjustedMaturityDate = cashflowSchedule[cashflowSchedule.length - 1][0];

    // Calculate the discretisation interval for the time axis
    double timeInterval = TimeCalculator.getTimeBetween(valuationDate, adjustedMaturityDate, dayCount);
    int numberOfPartitions = (int) (_numberOfIntegrationSteps * timeInterval + 0.5);
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
