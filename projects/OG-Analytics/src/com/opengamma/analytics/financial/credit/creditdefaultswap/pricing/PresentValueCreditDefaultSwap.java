/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;

/**
 * 
 */
public class PresentValueCreditDefaultSwap {

  public double getPresentValueCreditDefaultSwap(CreditDefaultSwapDefinition cds, double [][]cashflowSchedule) {

    // -------------------------------------------------------------------------------------------------

    int phi = 0;

    double presentValue = 0.0;
    double presentValuePremiumLeg = 0.0;
    double presentValueContingentLeg = 0.0;

    // Calculate the value of the premium leg
    presentValuePremiumLeg = calculatePremiumLeg(cds, cashflowSchedule);

    // Calculate the value of the contingent leg
    presentValueContingentLeg = calculateContingentLeg(cds, cashflowSchedule);

    // Constructing the CDS checks if the Buy/Sell flag is not equal to 'Buy' or 'Sell', so don't have to check it here
    if (cds.getBuySellProtection().equalsIgnoreCase("Buy")) {
      phi = 1;
    } else {
      phi = -1;
    }

    // TODO : Make sure it is clear what the buy/sell protection cashflow convention is i.e. value of phi
    presentValue = -phi * presentValuePremiumLeg + presentValueContingentLeg;

    return presentValue;
  }

  //-------------------------------------------------------------------------------------------------

  // TODO : Seperate out the accrued premium calc out into another method (so users can see contribution of this directly)

  // Method to calculate the value of the fee leg

  // We assume the schedule of coupon payment dates represented as doubles (suitably generated) has been computed externally and is passed in via cashflowSchedule
  // We assume the discount factors have been computed externally and passed in with the CDS object
  // We assume the calibrated survival probabilities have been computed externally and are passed in with the CDS object

  // Will replace these dummy 'objects' with suitably computed objects in due course

  // For now, assuming we are on a cashflow date (for testing purposes) - will need to add the correction for a seasoned trade

  double calculatePremiumLeg(CreditDefaultSwapDefinition cds, double [][]cashflowSchedule) {

    double presentValuePremiumLeg = 0.0;
    double presentValueAccruedPremium = 0.0;

    // Determine how many premium cashflows there are in the original contract schedule (this includes time zero even though there is no cashfow on this date
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

  // Method to calculate the value of the contingent leg

  double calculateContingentLeg(CreditDefaultSwapDefinition cds, double [][]cashflowSchedule) {

    double presentValueContingentLeg = 0.0;

    //ZonedDateTime maturityDate = cds.getMaturityDate();
    //ZonedDateTime valuationDate = cds.getValuationDate();

    // Get the notional amount to multiply the premium leg by
    double notional = cds.getNotional();

    // get the yield curve
    YieldCurve yieldCurve = cds.getYieldCurve();

    // Get the survival curve
    YieldCurve survivalCurve = cds.getSurvivalCurve();

    // Hardcoded hack for now - will remove when work out how to use ZoneddateTime
    int tVal = 0;
    int maturity = 5;

    int numberOfIntegrationSteps = cds.getNumberOfIntegrationSteps();

    int numberOfPartitions = (int) (numberOfIntegrationSteps * (maturity - tVal) + 0.5);

    double epsilon = (double) (maturity - tVal) / (double) numberOfPartitions;

    double valuationRecoveryRate = cds.getValuationRecoveryRate();

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

  //-------------------------------------------------------------------------------------------------
}
