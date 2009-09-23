/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

/**
 * 
 * @author emcleod
 */
public final class Price implements Greek {
  private static final String NAME = "PRICE";

  @Override
  public <T> T accept(GreekVisitor<T> visitor) {
    return visitor.visitPrice(this);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String toString() {
    return "Price";
  }
}
