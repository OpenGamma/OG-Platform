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
public final class Theta implements Greek {
  private static final String NAME = "THETA";

  @Override
  public <T> T accept(GreekVisitor<T> visitor) {
    return visitor.visitTheta(this);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String toString() {
    return "Theta";
  }
}
