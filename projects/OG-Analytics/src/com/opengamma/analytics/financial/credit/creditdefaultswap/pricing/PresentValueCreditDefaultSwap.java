/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import com.opengamma.analytics.financial.credit.BuySellProtection;
import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;

/**
 *  Class containing methods for the valuation of a legacy vanilla CDS (and its constituent legs)
 */
public class PresentValueCreditDefaultSwap {

  // -------------------------------------------------------------------------------------------------
  
  // Method for computing the PV of a CDS  
  public double getPresentValueCreditDefaultSwap(CreditDefaultSwapDefinition cds, double [][]cashflowSchedule) {

    // -------------------------------------------------------------------------------------------------

    // Calculate the value of the premium leg
    double presentValuePremiumLeg = calculatePremiumLeg(cds, cashflowSchedule);

    // Calculate the value of the contingent leg
    double presentValueContingentLeg = calculateContingentLeg(cds);

    // Calculate the PV of the CDS (assumes we are buying protection i.e. paying the premium leg, receiving the contingent leg)
    double presentValue = -presentValuePremiumLeg + presentValueContingentLeg;

    // If we are selling protection, then reverse the direction of the premium and contingent leg cashflows
    if (cds.getBuySellProtection() == BuySellProtection.SELL) {
      presentValue = -1 * presentValue;
    }

    return presentValue;
  }

  //-------------------------------------------------------------------------------------------------

  // TODO : Seperate out the accrued premium calc out into another method (so users can see contribution of this directly)
  // TODO : Add a method to calc both the legs in one go (is this useful or not? Might be useful from a speed perspective - remember can have O(10^5) positions in a book)

  // We assume the schedule of coupon payment dates represented as doubles (suitably generated) has been computed externally and is passed in via cashflowSchedule
  // We assume the discount factors have been computed externally and are passed in with the CDS object
  // We assume the 'calibrated' survival probabilities have been computed externally and are passed in with the CDS object
  // Will replace these three dummy 'objects' with suitably computed objects in due course
  // For now, assuming we are on a cashflow date (for testing purposes) - will need to add the corrections for a seasoned trade

  // -------------------------------------------------------------------------------------------------

  // Method to calculate the value of the premium leg of a CDS 
  double calculatePremiumLeg(CreditDefaultSwapDefinition cds, double [][]cashflowSchedule) {

    double presentValuePremiumLeg = 0.0;
    double presentValueAccruedPremium = 0.0;

    // Determine how many premium cashflows there are in the original contract schedule (this includes time zero even though there is no cashfow on this date)
    int n = cashflowSchedule.length;

    // Get the notional amount to multiply the premium leg by
    double notional = cds.getNotional();

    // Get the CDS par spread (remember this is supplied in bps, therefore needs to be divided by 10,000)
    double parSpread = cds.getParSpread() / 10000.0;

    // get the yield curve
    YieldCurve yieldCurve = cds.getYieldCurve();

    // Get the survival curve
    YieldCurve survivalCurve = cds.getSurvivalCurve();

    // Do we need to calculate the accrued premium as well
    boolean includeAccruedPremium = cds.getIncludeAccruedPremium();

    // Loop through all the elements (times and dcf's) in the cashflow schedule (note limits of loop)
    for (int i = 1; i < n; i++) {

      double t = cashflowSchedule[i][0];
      double dcf = cashflowSchedule[i][1];

      double discountFactor = yieldCurve.getDiscountFactor(t);
      double survivalProbability = survivalCurve.getDiscountFactor(t);

      presentValuePremiumLeg += dcf * discountFactor * survivalProbability;

      // If required, calculate the accrued premium contribution to the overall premium leg
      if (includeAccruedPremium) {

        double tPrevious = cashflowSchedule[i - 1][0];
        double survivalProbabilityPrevious = survivalCurve.getDiscountFactor(tPrevious);

        presentValueAccruedPremium += 0.5 * dcf * discountFactor * (survivalProbabilityPrevious - survivalProbability);
      }
    }

    return parSpread * notional * (presentValuePremiumLeg + presentValueAccruedPremium);
  }

  // -------------------------------------------------------------------------------------------------

  // Method to calculate the accrued premium of a CDS premium leg (this method is just to allow a user to calculate the accrued on its own)
  double calculateAccruedPremium(CreditDefaultSwapDefinition cds) {

    // TODO : Add this code
    
    double presentValueAccruedPremium = 0.0;

    return presentValueAccruedPremium;
  }

  // -------------------------------------------------------------------------------------------------

  // Method to calculate the value of the contingent leg of a CDS
  double calculateContingentLeg(CreditDefaultSwapDefinition cds) {

    double presentValueContingentLeg = 0.0;

    // Get the notional amount to multiply the contingent leg by
    double notional = cds.getNotional();

    // get the yield curve
    YieldCurve yieldCurve = cds.getYieldCurve();

    // Get the survival curve
    YieldCurve survivalCurve = cds.getSurvivalCurve();

    // Hardcoded hack for now - will remove when implement the schedule generator
    int tVal = 0;
    int maturity = 5;

    //ZonedDateTime maturityDate = cds.getMaturityDate();
    //ZonedDateTime valuationDate = cds.getValuationDate();

    //int numDays = DateUtils.getDaysBetween(valuationDate, maturityDate);

    int numberOfIntegrationSteps = cds.getNumberOfIntegrationSteps();

    // Check this calculation more carefully - is the proper way to do it
    //int numberOfPartitions = (int) (numberOfIntegrationSteps * numDays / 365.25 + 0.5); 

    int numberOfPartitions = (int) (numberOfIntegrationSteps * (maturity - tVal) + 0.5);

    double epsilon = (double) (maturity - tVal) / (double) numberOfPartitions;

    double valuationRecoveryRate = cds.getValuationRecoveryRate();

    // Calculate the integral for the contingent leg
    for (int k = 1; k < numberOfPartitions; k++) {

      double t = k * epsilon;
      double tPrevious = (k - 1) * epsilon;

      double discountFactor = yieldCurve.getDiscountFactor(t);
      double survivalProbability = survivalCurve.getDiscountFactor(t);
      double survivalProbabilityPrevious = survivalCurve.getDiscountFactor(tPrevious);

      presentValueContingentLeg += discountFactor * (survivalProbabilityPrevious - survivalProbability);
    }

    return notional * (1 - valuationRecoveryRate) * presentValueContingentLeg;
  }

  // -------------------------------------------------------------------------------------------------
}
