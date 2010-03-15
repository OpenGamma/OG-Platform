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
public class FirstOrder extends Order {
  private final Underlying _variable;

  public FirstOrder(final Underlying variable) {
    _variable = variable;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.financial.greeks.OrderClass#accept(com.opengamma.financial
   * .greeks.OrderClassVisitor)
   */
  @Override
  public <T> T accept(final OrderVisitor<T> visitor) {
    return visitor.visitFirstOrder();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.OrderClass#getOrderType()
   */
  @Override
  public OrderType getOrderType() {
    return OrderType.FIRST;
  }

  public Underlying getVariable() {
    return _variable;
  }
}
