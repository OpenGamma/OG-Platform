/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;

/**
 * 
 */
public class PresentValueCreditDefaultSwap {
  
  public double getPresentValueCreditDefaultSwap(CreditDefaultSwapDefinition cds, double cashflowSchedule[][], double irCurve[][], double survCurve[][]) {

    // -------------------------------------------------------------------------------------------------

    int phi = 0;

    double presentValue = 0.0;
    double presentValuePremiumLeg = 0.0;
    double presentValueContingentLeg = 0.0;
    double presentValueAccruedPremium = 0.0;

    // Calculate the value of the premium leg
    presentValuePremiumLeg = calculatePremiumLeg(cds, cashflowSchedule, irCurve, survCurve);
    
    // Calculate the value of the contingent leg
    presentValueContingentLeg = calculateContingentLeg(cds, cashflowSchedule, irCurve, survCurve);
    
    // Calculate the accrued premium if this is specified in the contract
    if (cds.getIncludeAccruedPremium()) {
      presentValueAccruedPremium = calculateAccruedPremium(cds, cashflowSchedule, irCurve, survCurve);
    } else {
      presentValueAccruedPremium = 0.0;
    }

    // Constructing the CDS checks if the Buy/Sell flag is not equal to 'Buy' or 'Sell', so don't have to check it here
    if (cds.getBuySellProtection().equalsIgnoreCase("Buy")) {
      phi = 1;
    } else {
      phi = -1;
    }

    // TODO : Make sure it is clear what the buy/sell protection cashflow convention is i.e. value of phi
    presentValue = -phi * (presentValuePremiumLeg + presentValueAccruedPremium) + presentValueContingentLeg;

    return presentValue;
  }

  //-------------------------------------------------------------------------------------------------
  
  // Method to calculate the value of the fee leg
  
  // We assume the schedule of coupon payment dates (suitably generated) has been computed externally and is passed in via cashflowSchedule
  // We assume the discount factors have been computed externally and passed in via irCurve
  // We assume the calibrated survival probabilities have been computed externally and are passed in via survCurve
  
  // Will replace these dummy 'objects' with suitably computed objects in due course
  
  double calculatePremiumLeg(CreditDefaultSwapDefinition cds, double cashflowSchedule[][], double irCurve[][], double survCurve[][]) {

    // Determine how many cashflows there are (this includes time zero even though there is no cashfow on this date
    int n = cashflowSchedule.length;
    
    double presentValuePremiumLeg = 0.0;
    
    // Get the notional amount to multiply for premium leg by
    double notional = cds.getNotional();
    
    // Get the CDS par spread (remember this is supplied in bps, therefore needs to be divided by 10,000)
    double parSpread = cds.getParSpread() / 10000.0;
 
    for (int i = 1; i < n; i++) {
      double dcf = cashflowSchedule[i][1];
      double discountFactor = irCurve[i][1];
      double survivalProbability = survCurve[i][1];
      
      presentValuePremiumLeg += dcf * discountFactor * survivalProbability;
    }

    return parSpread * notional * presentValuePremiumLeg;
  }
  
  // -------------------------------------------------------------------------------------------------
  
  // Method to calculate the value of the contingent leg
  double calculateContingentLeg(CreditDefaultSwapDefinition cds, double cashflowSchedule[][], double irCurve[][], double survCurve[][]) {
  
    double presentValueContingentLeg = 0.0;
    
    ZonedDateTime maturityDate = cds.getMaturityDate();
    ZonedDateTime valuationDate = cds.getValuationDate();
    
    // Hardcoded hack for now - will remove when work out how to use ZoneddateTime
    int t = 0;
    int T =5;
    
    //int K = numberOfIntegrationPoints * (T - t) + 0.5;
    
    int numberOfIntegrationSteps = cds.getNumberOfIntegrationSteps();
    
    double valuationRecoveryRate = cds.getValuationRecoveryRate();
    
    System.out.println("delta = " + valuationRecoveryRate);
    
    return presentValueContingentLeg;
  }
  
  //-------------------------------------------------------------------------------------------------
  
  // Method to calculate the value of the accrued premium (part of the premium leg cashflow)
  double calculateAccruedPremium(CreditDefaultSwapDefinition cds, double cashflowSchedule[][], double irCurve[][], double survCurve[][]) {
    
    double presentValueAccruedPremium = 0.0;
    
    return presentValueAccruedPremium;
  }
  
  //-------------------------------------------------------------------------------------------------
}
