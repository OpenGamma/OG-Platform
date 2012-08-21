/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditdefaultswap.pricing;

import com.opengamma.analytics.financial.credit.creditdefaultswap.definition.CreditDefaultSwapDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;

/**
 * 
 */
public class PresentValueCreditDefaultSwap {
  
  public double getPresentValueCreditDefaultSwap(CreditDefaultSwapDefinition cds) {

    // -------------------------------------------------------------------------------------------------

    int phi = 0;

    double presentValue = 0.0;
    double presentValuePremiumLeg = 0.0;
    double presentValueContingentLeg = 0.0;
    double presentValueAccruedPremium = 0.0;


    // Calculate the value of the premium leg
    presentValuePremiumLeg = calculatePremiumLeg(cds);
    
    // Calculate the value of the contingent leg
    presentValueContingentLeg = calculateContingentLeg(cds);
    
    // Calculate the accrued premium if this is required
    if (cds.getIncludeAccruedPremium()) {
      presentValueAccruedPremium = calculateAccruedPremium(cds);
    }                    // Sort out checkstyle
    else {
      presentValueAccruedPremium = 0.0;
    }

    // Constructing the CDS checks if the Buy/Sell flag is not equal to 'Buy' or 'Sell', so don't have to check it here
    if (cds.getBuySellProtection().equalsIgnoreCase("Buy")) {
      phi = 1;
    }                   // Sort out checkstyle
    else {
      phi = -1;
    }

    // TODO : Make sure make it is clear what the buy/sell protection cashflow convention is
    presentValue = -phi * (presentValuePremiumLeg + presentValueAccruedPremium) + presentValueContingentLeg;

    return presentValue;
  }

  //-------------------------------------------------------------------------------------------------
  
  // Method to calculate the value of the fee leg
  double calculatePremiumLeg(CreditDefaultSwapDefinition cds) {

    double presentValuePremiumLeg = 0.0;
    
    // Hack together a hard-coded premium leg, just to get the ball rolling
    
    double s = 60.0/10000.0;
    double N = 1;
    
    double T = 5.0;

    double Z = 0.0;
    double S = 0.0;
    
    double r = 0.0;
    double h = 0.01;
    
    double dcf = 0.25;
    
    int n = 20;
   
    for(int i = 1; i < n; i++)
    {
      double t = i/4;
      
      presentValuePremiumLeg += dcf*Math.exp(-r*t)*(Math.exp(-h*(t - 0.25)) - Math.exp(-h*t));
    }
    
    return s*N*presentValuePremiumLeg;
  }
  
  // -------------------------------------------------------------------------------------------------
  
  // Method to calculate the value of the contingent leg
  double calculateContingentLeg(CreditDefaultSwapDefinition cds) {
  
    double presentValueContingentLeg = 0.0;
    
    double delta = 0.4;
    
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
