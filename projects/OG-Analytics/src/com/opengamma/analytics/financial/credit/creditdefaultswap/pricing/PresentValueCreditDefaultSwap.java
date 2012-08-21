/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;

/**
 * 
 */
public class PresentValueCreditDefaultSwap {
  
  public double getPresentValueCreditDefaultSwap(CreditDefaultSwapDefinition cds) {

    // -------------------------------------------------------------------------------------------------
    
    int phi = 0;
    
    double presentValue = 0.0;
    
    double presentValuePremiumLeg = calculatePremiumLeg(cds);
    double presentValueContingentLeg = calculateContingentLeg(cds);
    double presentValueAccruedPremium = calculateAccruedPremium(cds);
    
    // Constructing the CDS checks if the Buy/Sell flag is not equal to 'Buy' or 'Sell', so don't have to check it here
    if(cds.getBuySellProtection() == "Buy")
      phi = 1;
    else
      phi = -1;
    
    // TODO : Make sure make it is clear what the buy/sell protection cashflow convention is
    presentValue = -phi*(presentValuePremiumLeg + presentValueAccruedPremium) + presentValueContingentLeg;
    
    return presentValue;
  }

  //-------------------------------------------------------------------------------------------------
  
  // Method to calculate the value of the fee leg
  double calculatePremiumLeg(CreditDefaultSwapDefinition cds) {

    double presentValuePremiumLeg = 0.0;

    return presentValuePremiumLeg;
  }
  
  // -------------------------------------------------------------------------------------------------
  
  // Method to calculate the value of the contingent leg
  double calculateContingentLeg(CreditDefaultSwapDefinition cds) {
  
    double presentValueContingentLeg = 0.0;
    
    return presentValueContingentLeg;
  }
  
  //-------------------------------------------------------------------------------------------------
  
  // Method to calculate the value of the accrued premium (part of the premium leg cashflow)
  double calculateAccruedPremium(CreditDefaultSwapDefinition cds) {
    
    double presentValueAccruedPremium = 0.0;
    
    return presentValueAccruedPremium;
  }
  
  //-------------------------------------------------------------------------------------------------
}
