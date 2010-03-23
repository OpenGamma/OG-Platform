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
public class MixedThirdOrder extends Order {
  // TODO I'm not thrilled with this class - it makes sense that this class is
  // the container for F_{x,y,z} and F_{x,y,y} but the get methods seem quite
  // inelegant for the second case
  private final FirstOrder _firstVariable;
  private final FirstOrder _secondVariable;
  private final FirstOrder _thirdVariable;

  public MixedThirdOrder(final Underlying firstVariable, final Underlying secondVariable, final Underlying thirdVariable) {
    _firstVariable = new FirstOrder(firstVariable);
    _secondVariable = new FirstOrder(secondVariable);
    _thirdVariable = new FirstOrder(thirdVariable);
  }

  public MixedThirdOrder(final FirstOrder firstVariable, final FirstOrder secondVariable, final FirstOrder thirdVariable) {
    _firstVariable = firstVariable;
    _secondVariable = secondVariable;
    _thirdVariable = thirdVariable;
  }

  public MixedThirdOrder(final FirstOrder firstOrder, final SecondOrder secondOrder) {
    _firstVariable = firstOrder;
    _secondVariable = new FirstOrder(secondOrder.getVariable());
    _thirdVariable = new FirstOrder(secondOrder.getVariable());
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
    return visitor.visitMixedThirdOrder();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.Order#getOrderType()
   */
  @Override
  public OrderType getOrderType() {
    return OrderType.MIXED_THIRD;
  }

  public FirstOrder getFirstVariable() {
    return _firstVariable;
  }

  public FirstOrder getSecondVariable() {
    return _secondVariable;
  }

  public FirstOrder getThirdVariable() {
    return _thirdVariable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "MixedThirdOrder [" + _firstVariable + ", " + _secondVariable + ", " + _thirdVariable + "]";
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
    result = prime * result + ((_thirdVariable == null) ? 0 : _thirdVariable.hashCode());
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
    final MixedThirdOrder other = (MixedThirdOrder) obj;
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
    if (_thirdVariable == null) {
      if (other._thirdVariable != null)
        return false;
    } else if (!_thirdVariable.equals(other._thirdVariable))
      return false;
    return true;
  }
}
