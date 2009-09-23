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
public class TimeBucketedRho implements Greek {
  private static final String NAME = "TIME-BUCKETED RHO";

  @Override
  public <T> T accept(GreekVisitor<T> visitor) {
    return visitor.visitTimeBucketedRho(this);
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String toString() {
    return "Time-bucketed rho";
  }
}
