/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future.definition;

import com.opengamma.financial.equity.future.derivative.EquityFuture;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.TimeCalculator;

import javax.time.calendar.ZonedDateTime;

/**
 * 
 */
public class EquityFutureDefinition {

  private final ZonedDateTime _expiryDate;
  private final ZonedDateTime _settlementDate;
  private final double _strikePrice;
  private final Currency _currency;
  private final double _unitAmount;

  /**
   * Basic setup for an Equity Future. TODO resolve conventions; complete param set 
   * @param expiryDate The date-time (in years as a double)  at which the reference rate is fixed and the future is cash settled  
   * @param settlementDate The date on which exchange is made, whether physical asset or cash equivalent 
   * @param strikePrice The reference price at which the future will be settled 
   * @param currency The reporting currency of the future
   * @param unitValue The currency value that the price of one contract will move by when the asset's price moves by one point
   */
  public EquityFutureDefinition(
      final ZonedDateTime expiryDate,
      final ZonedDateTime settlementDate,
      final double strikePrice,
      final Currency currency,
      final double unitValue) {

    _expiryDate = expiryDate;
    _settlementDate = settlementDate;
    _strikePrice = strikePrice;
    _currency = currency;
    _unitAmount = unitValue;
  }

  public EquityFuture toDerivative(ZonedDateTime date) {

    double timeToFixing = TimeCalculator.getTimeBetween(date, _expiryDate);
    double timeToDelivery = TimeCalculator.getTimeBetween(date, _settlementDate);

    EquityFuture newDeriv = new EquityFuture(timeToFixing, timeToDelivery, _strikePrice, _currency, _unitAmount);
    return newDeriv;
  }
}
