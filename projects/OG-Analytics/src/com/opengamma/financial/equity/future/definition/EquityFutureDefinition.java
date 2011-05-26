/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future.definition;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.equity.future.derivative.EquityFuture;
import com.opengamma.util.time.TimeCalculator;

/**
 * 
 */
public class EquityFutureDefinition {
  
  private final ZonedDateTime _fixingDate;
  private final ZonedDateTime _deliveryDate;
  private final double _strikePrice;
  private final double _pointValue; // !!! May want this to be {currency,double}
  private final int _numContracts;
  private final String _assetName; // !!! This is meant to completely identify the underlying. May need to include more fields.
 
  /**
   * Basic setup for an Equity Future. TODO resolve conventions; complete param set 
   * @param fixingDate The date-time (in years as a double)  at which the reference rate is fixed and the future is cash settled  
   * @param deliveryDate The date on which exchange is made, whether physical asset or cash equivalent 
   * @param strikePrice The reference price at which the future will be settled 
   * @param pointValue The currency value that the price of one contract will move by when the asset's price moves by one point
   * @param numContracts Whole number of contracts that are owned, whether held long (positive) or sold short (negative)
   * @param assetName Name of the underlying asset
   */
  
  public EquityFutureDefinition(
    final ZonedDateTime fixingDate,
    final ZonedDateTime deliveryDate,
    final double strikePrice,
    final double pointValue,
    final int numContracts,
    final String assetName) {
    // TODO checkInputs
    _fixingDate = fixingDate;
    _deliveryDate = deliveryDate;
    _strikePrice = strikePrice;
    _pointValue = pointValue;
    _numContracts = numContracts;
    _assetName = assetName;
  }
   
    
  public EquityFuture toDerivative(ZonedDateTime date) {
    
    double timeToFixing = TimeCalculator.getTimeBetween(date, _fixingDate);
    double timeToDelivery = TimeCalculator.getTimeBetween(date, _deliveryDate);

    EquityFuture newDeriv = new EquityFuture(timeToFixing, timeToDelivery, _strikePrice, _pointValue, _numContracts, _assetName);
    return newDeriv;
  }
}
