/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.creditindexdefaultswap.definition;

import javax.time.calendar.ZonedDateTime;

/**
 * 
 */
public class CreditIndexDefaultSwapDefinition {
  
  ZonedDateTime _startDate;
  ZonedDateTime _effectiveDate;
  ZonedDateTime _maturityDate;
  ZonedDateTime _valuationDate;
  
  private final int _numberOfObligors;
  
  private final double _notional;
  
  private final double _spread;

  
  /*
  public CreditIndexDefaultSwapDefinition(ZonedDateTime startDate, 
      ZonedDateTime effectiveDate, 
      ZonedDateTime maturityDate, 
      ZonedDateTime valuationDate, 
      int numberOfObligors, 
      double notional, 
      double spread)
  */
  
  
  public CreditIndexDefaultSwapDefinition(double notional, double spread, int numberOfObligors) {
    
    //_startDate = startDate;
    //_effectiveDate = effectiveDate;
    //_maturityDate = maturityDate;
    //_valuationDate = valuationDate;
    
    _numberOfObligors = numberOfObligors;
    
    _notional = notional;
    
    _spread = spread;
  }

//----------------------------------------------------------------------------------------------------------------------------------------

  ZonedDateTime getStartDate() {
    return _startDate;
  }

  void setStartDate(ZonedDateTime startDate) {
    _startDate = startDate;
  }

  ZonedDateTime getEffectiveDate() {
    return _effectiveDate;
  }

  void setEffectiveDate(ZonedDateTime effectiveDate) {
    _effectiveDate = effectiveDate;
  }

  ZonedDateTime getMaturityDate() {
    return _maturityDate;
  }

  void setMaturityDate(ZonedDateTime maturityDate) {
    _maturityDate = maturityDate;
  }

  ZonedDateTime getValuationDate() {
    return _valuationDate;
  }
  
  int getNumberOfObligors() {
    return _numberOfObligors;
  }

  void setValuationDate(ZonedDateTime valuationDate) {
    _valuationDate = valuationDate;
  }

  double getNotional() {
    return _notional;
  }

  double getSpread() {
    return _spread;
  }
  
  
  
//----------------------------------------------------------------------------------------------------------------------------------------
  
}
