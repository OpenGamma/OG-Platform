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

  public T visitPrice();

  public T visitDelta();

  public T visitGamma();

  public T visitRho();

  public T visitTheta();

  public T visitTimeBucketedRho();
}
