/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.integration.cashflow;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.core.position.Position;
import com.opengamma.util.money.CurrencyAmount;

/**
 * 
 */
public class PositionPayment {

  private final Position _position;
  private final PaymentType _paymentType;
  private final PaymentDirection _direction;
  private final String _index;
  private final CurrencyAmount _amount;
  
  public PositionPayment(Position position, PaymentType paymentType, PaymentDirection direction, String index, CurrencyAmount amount) {
    _position = position;
    _paymentType = paymentType;
    _direction = direction;
    _index = index;
    _amount = amount;
  }

  public Position getPosition() {
    return _position;
  }

  public PaymentType getPaymentType() {
    return _paymentType;
  }
  
  public PaymentDirection getDirection() {
    return _direction;
  }
  
  public String getIndex() {
    return _index;
  }

  public CurrencyAmount getAmount() {
    return _amount;
  }

  //-------------------------------------------------------------------------
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_amount == null) ? 0 : _amount.hashCode());
    result = prime * result + ((_index == null) ? 0 : _index.hashCode());
    result = prime * result + ((_paymentType == null) ? 0 : _paymentType.hashCode());
    result = prime * result + ((_direction == null) ? 0 : _direction.hashCode());
    result = prime * result + ((_position == null) ? 0 : _position.hashCode());
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
    if (!(obj instanceof PositionPayment)) {
      return false;
    }
    PositionPayment other = (PositionPayment) obj;
    return ObjectUtils.equals(_amount, other._amount)
        && ObjectUtils.equals(_index, other._index)
        && ObjectUtils.equals(_paymentType, other._paymentType)
        && ObjectUtils.equals(_direction, other._direction)
        && ObjectUtils.equals(_position, other._position);
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();
    ToStringStyle style = ToStringStyle.SHORT_PREFIX_STYLE;
    style.appendStart(sb, this);
    style.append(sb, "position", getPosition(), null);
    style.append(sb, "type", getPaymentType(), null);
    style.append(sb, "direction", getDirection(), null);
    style.append(sb, "index", getIndex(), null);
    style.append(sb, "amount", getAmount(), null);
    style.appendEnd(sb, this);
    return sb.toString();
  }
  
}
