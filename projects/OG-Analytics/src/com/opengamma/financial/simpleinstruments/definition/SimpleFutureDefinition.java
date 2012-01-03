/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.simpleinstruments.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.simpleinstruments.derivative.SimpleFuture;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.TimeCalculator;

/**
 * 
 */
public class SimpleFutureDefinition implements SimpleInstrumentDefinition<SimpleFuture> {
  private final ZonedDateTime _expiryDate;
  private final ZonedDateTime _settlementDate;
  private final double _referencePrice;
  private final Currency _currency;
  private final double _unitAmount;
  
  public SimpleFutureDefinition(final ZonedDateTime expiryDate, final ZonedDateTime settlementDate, final double referencePrice, final Currency currency, final double unitAmount) {
    Validate.notNull(expiryDate, "expiry date");
    Validate.notNull(settlementDate, "settlement date");
    Validate.notNull(currency, "currency");
    _expiryDate = expiryDate;
    _settlementDate = settlementDate;
    _referencePrice = referencePrice;
    _currency = currency;
    _unitAmount = unitAmount;
  }
  
  public ZonedDateTime getExpiry() {
    return _expiryDate;
  }
  
  public ZonedDateTime getSettlementDate() {
    return _settlementDate;
  }
  
  public double getReferencePrice() {
    return _referencePrice;
  }
  
  public Currency getCurrency() {
    return _currency;
  }
  
  public double getUnitAmount() {
    return _unitAmount;
  }
  
  @Override
  public SimpleFuture toDerivative(final ZonedDateTime date) {
    Validate.notNull(date, "date");
    Validate.isTrue(date.isBefore(_expiryDate));
    double timeToFixing = TimeCalculator.getTimeBetween(date, _expiryDate);
    double timeToDelivery = TimeCalculator.getTimeBetween(date, _settlementDate);
    return new SimpleFuture(timeToFixing, timeToDelivery, _referencePrice, _unitAmount, _currency);
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    result = prime * result + _expiryDate.hashCode();
    result = prime * result + _settlementDate.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_referencePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_unitAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final SimpleFutureDefinition other = (SimpleFutureDefinition) obj;
    if (Double.doubleToLongBits(_referencePrice) != Double.doubleToLongBits(other._referencePrice)) {
      return false;
    }
    if (!ObjectUtils.equals(_expiryDate, other._expiryDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_settlementDate, other._settlementDate)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }   
    if (Double.doubleToLongBits(_unitAmount) != Double.doubleToLongBits(other._unitAmount)) {
      return false;
    }
    return true;
  }
}
