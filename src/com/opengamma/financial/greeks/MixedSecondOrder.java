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
}
