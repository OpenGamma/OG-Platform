/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.sensitivity;

import com.opengamma.financial.greeks.Order;

/**
 * @author emcleod
 * 
 */
public class OrderSensitivityVisitor implements SensitivityVisitor<Order> {

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.financial.sensitivity.SensitivityVisitor#visitConvexity()
   */
  @Override
  public Order visitConvexity() {
    return Order.SECOND;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.sensitivity.SensitivityVisitor#visitPV01()
   */
  @Override
  public Order visitPV01() {
    return Order.FIRST;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.financial.sensitivity.SensitivityVisitor#visitValueDelta()
   */
  @Override
  public Order visitValueDelta() {
    return Order.FIRST;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.financial.sensitivity.SensitivityVisitor#visitValueGamma()
   */
  @Override
  public Order visitValueGamma() {
    return Order.SECOND;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.financial.sensitivity.SensitivityVisitor#visitValueTheta()
   */
  @Override
  public Order visitValueTheta() {
    return Order.FIRST;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.financial.sensitivity.SensitivityVisitor#visitValueVega()
   */
  @Override
  public Order visitValueVega() {
    return Order.FIRST;
  }

}
