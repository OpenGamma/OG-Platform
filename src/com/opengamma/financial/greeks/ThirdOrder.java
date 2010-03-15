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
public class ThirdOrder extends Order {
  private final Underlying _variable;

  public ThirdOrder(final Underlying variable) {
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
    return visitor.visitThirdOrder();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.OrderClass#getOrderType()
   */
  @Override
  public OrderType getOrderType() {
    return OrderType.THIRD;
  }

  public Underlying getVariable() {
    return _variable;
  }

}
