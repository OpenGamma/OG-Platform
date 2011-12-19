/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.simpleinstruments.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.util.money.Currency;

/**
 * 
 */
public class SimpleFuture implements SimpleInstrument {
  private final double _expiry;
  private final double _settlement;
  private final double _referencePrice;
  private final double _unitAmount;
  private final Currency _currency;
  
  public SimpleFuture(final double expiry, final double settlement, final double referencePrice, final double unitAmount, final Currency currency) {
    Validate.notNull(currency, "currency");
    Validate.isTrue(expiry > 0, "time to expiry must be positive");
    Validate.isTrue(settlement > 0, "time to settlement must be positive");
    _expiry = expiry;
    _settlement = settlement;
    _referencePrice = referencePrice;
    _unitAmount = unitAmount;
    _currency = currency;
  }
  
  public double getExpiry() {
    return _expiry;
  }
  
  public double getSettlement() {
    return _settlement;
  }
  
  public double getReferencePrice() {
    return _referencePrice;
  }
  
  public double getUnitAmount() {
    return _unitAmount;
  }
  
  public Currency getCurrency() {
    return _currency;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _currency.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_expiry);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_referencePrice);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_settlement);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(_unitAmount);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    } 
    if (getClass() != obj.getClass()) {
      return false;
    }
    SimpleFuture other = (SimpleFuture) obj;
    if (Double.doubleToLongBits(_expiry) != Double.doubleToLongBits(other._expiry)) {
      return false;
    }
    if (Double.doubleToLongBits(_referencePrice) != Double.doubleToLongBits(other._referencePrice)) {
      return false;
    }
    if (Double.doubleToLongBits(_settlement) != Double.doubleToLongBits(other._settlement)) {
      return false;
    }
    if (Double.doubleToLongBits(_unitAmount) != Double.doubleToLongBits(other._unitAmount)) {
      return false;
    }
    if (!ObjectUtils.equals(_currency, other._currency)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(final SimpleInstrumentVisitor<S, T> visitor, final S data) {
    return visitor.visitSimpleFuture(this, data);
  }

  @Override
  public <S, T> T accept(SimpleInstrumentVisitor<S, T> visitor) {
    return visitor.visitSimpleFuture(this);
  }
  
  
}

