/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import java.util.Set;

import com.opengamma.financial.pnl.Underlying;

/**
 * 
 */
public abstract class Order {
  public enum OrderType {
    ZEROTH, FIRST, MIXED_SECOND, SECOND, MIXED_THIRD, THIRD
  }

  public abstract <T> T accept(OrderVisitor<T> visitor);

  public abstract OrderType getOrderType();

  public abstract Set<Underlying> getUnderlyings();
}
