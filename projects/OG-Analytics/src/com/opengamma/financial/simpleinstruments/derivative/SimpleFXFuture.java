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
public class SimpleFXFuture implements SimpleInstrument {
  private final double _expiry;
  private final double _settlement;
  private final double _referencePrice;
  private final double _unitAmount;
  private final Currency _payCurrency;
  private final Currency _receiveCurrency;
  
  public SimpleFXFuture(final double expiry, final double settlement, final double referencePrice, final double unitAmount, final Currency payCurrency, final Currency receiveCurrency) {
    Validate.notNull(payCurrency, "currency");
    Validate.isTrue(expiry > 0, "time to expiry must be positive");
    Validate.isTrue(settlement > 0, "time to settlement must be positive");
    _expiry = expiry;
    _settlement = settlement;
    _referencePrice = referencePrice;
    _unitAmount = unitAmount;
    _payCurrency = payCurrency;
    _receiveCurrency = receiveCurrency;
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
  
  public Currency getPayCurrency() {
    return _payCurrency;
  }
  
  public Currency getReceiveCurrency() {
    return _receiveCurrency;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _payCurrency.hashCode();
    result = prime * result + _receiveCurrency.hashCode();
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
    SimpleFXFuture other = (SimpleFXFuture) obj;
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
    if (!ObjectUtils.equals(_payCurrency, other._payCurrency)) {
      return false;
    }
    if (!ObjectUtils.equals(_receiveCurrency, other._receiveCurrency)) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(final SimpleInstrumentVisitor<S, T> visitor, final S data) {
    return visitor.visitSimpleFXFuture(this, data);
  }

  @Override
  public <S, T> T accept(final SimpleInstrumentVisitor<S, T> visitor) {
    return visitor.visitSimpleFXFuture(this);
  }
  
}

