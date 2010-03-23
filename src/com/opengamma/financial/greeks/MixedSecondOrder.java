/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import com.opengamma.financial.pnl.Underlying;

/**
 * @author emcleod
 * 
 */
public class MixedSecondOrder extends Order {
  private final FirstOrder _firstVariable;
  private final FirstOrder _secondVariable;

  public MixedSecondOrder(final Underlying firstVariable, final Underlying secondVariable) {
    _firstVariable = new FirstOrder(firstVariable);
    _secondVariable = new FirstOrder(secondVariable);
  }

  public MixedSecondOrder(final FirstOrder firstVariable, final FirstOrder secondVariable) {
    _firstVariable = firstVariable;
    _secondVariable = secondVariable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.financial.greeks.Order#accept(com.opengamma.financial.greeks
   * .OrderVisitor)
   */
  @Override
  public <T> T accept(final OrderVisitor<T> visitor) {
    return visitor.visitMixedSecondOrder();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.Order#getOrderType()
   */
  @Override
  public OrderType getOrderType() {
    return OrderType.MIXED_SECOND;
  }

  public FirstOrder getFirstVariable() {
    return _firstVariable;
  }

  public FirstOrder getSecondVariable() {
    return _secondVariable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "MixedSecondOrder[" + _firstVariable + ", " + _secondVariable + "]";
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_firstVariable == null) ? 0 : _firstVariable.hashCode());
    result = prime * result + ((_secondVariable == null) ? 0 : _secondVariable.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final MixedSecondOrder other = (MixedSecondOrder) obj;
    if (_firstVariable == null) {
      if (other._firstVariable != null)
        return false;
    } else if (!_firstVariable.equals(other._firstVariable))
      return false;
    if (_secondVariable == null) {
      if (other._secondVariable != null)
        return false;
    } else if (!_secondVariable.equals(other._secondVariable))
      return false;
    return true;
  }
}
