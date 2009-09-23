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
public interface GreekVisitor<T> {

  public T visitPrice(Price price);

  public T visitDelta(Delta delta);

  public T visitGamma(Gamma gamma);

  public T visitRho(Rho rho);

  public T visitTheta(Theta theta);

  public T visitTimeBucketedRho(TimeBucketedRho rho);
}
