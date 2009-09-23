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
public final class Rho implements Greek {
  private static final String NAME = "RHO";

  @Override
  public <T> T accept(GreekVisitor<T> visitor) {
    return visitor.visitRho(this);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String toString() {
    return "Rho";
  }
}
