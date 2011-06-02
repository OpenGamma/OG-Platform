/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.equity.future.definition;

import com.opengamma.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.util.money.Currency;

import javax.time.calendar.ZonedDateTime;

/**
 * Each time a view is recalculated, the security definition 
 * creates an analytic derivative for the current time. 
 */
public class EquityIndexDividendFutureDefinition extends EquityFutureDefinition {

  /**
   * @param expiryDate
   * @param settlementDate
   * @param strikePrice
   * @param currency
   * @param unitValue
   */
  public EquityIndexDividendFutureDefinition(ZonedDateTime expiryDate, ZonedDateTime settlementDate, double strikePrice, Currency currency, double unitValue) {
    super(expiryDate, settlementDate, strikePrice, currency, unitValue);
  }

  @Override
  public EquityIndexDividendFuture toDerivative(ZonedDateTime date) {
    return (EquityIndexDividendFuture) super.toDerivative(date);

  }

}
