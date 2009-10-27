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

  public T visitZeta();

  public T visitCarryRho();

  public T visitDelta();

  public T visitDriftlessTheta();

  public T visitDZetaDVol();

  public T visitElasticity();

  public T visitPhi();

  public T visitRho();

  public T visitStrikeDelta();

  public T visitTheta();

  public T visitTimeBucketedRho();

  public T visitVarianceVega();

  public T visitVega();

  public T visitVegaP();

  public T visitZetaBleed();

  public T visitDDeltaDVar();

  public T visitDeltaBleed();

  public T visitGamma();

  public T visitGammaP();

  public T visitStrikeGamma();

  public T visitVanna();

  public T visitVarianceVomma();

  public T visitVegaBleed();

  public T visitVomma();

  public T visitVommaP();

  public T visitDVannaDVol();

  public T visitGammaBleed();

  public T visitGammaPBleed();

  public T visitSpeed();

  public T visitSpeedP();

  public T visitUltima();

  public T visitVarianceUltima();

  public T visitZomma();

  public T visitZommaP();
}
