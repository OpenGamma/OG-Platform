/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

/**
 * @author emcleod
 * 
 */
public abstract class Order {
  public enum OrderType {
    ZEROTH, FIRST, MIXED_SECOND, SECOND, MIXED_THIRD, THIRD
  }

  public abstract <T> T accept(OrderVisitor<T> visitor);

  public abstract OrderType getOrderType();
}
