/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditindexdefaultswap.definition;

/**
 * 
 */
public class CreditIndexDefaultSwapDefinition {
  
  private final int _numberofobligors;
  
  private final double _notional;
  
  public CreditIndexDefaultSwapDefinition(int numberofobligors, double notional) {
    
    _numberofobligors = numberofobligors;
    
    _notional = notional;
  }

//----------------------------------------------------------------------------------------------------------------------------------------
  
  int getNumberofobligors() {
    return _numberofobligors;
  }

  double getNotional() {
    return _notional;
  }
  
//----------------------------------------------------------------------------------------------------------------------------------------
  
}
