/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.greeks;

import org.apache.commons.lang.NotImplementedException;

/**
 * @param <T> Return type of visitor
 */
public abstract class AbstractGreekVisitor<T> implements GreekVisitor<T> {

  @Override
  public T visitCarryRho() {
    throw new NotImplementedException();
  }

  @Override
  public T visitDVannaDVol() {
    throw new NotImplementedException();
  }

  @Override
  public T visitDZetaDVol() {
    throw new NotImplementedException();
  }

  @Override
  public T visitDelta() {
    throw new NotImplementedException();
  }

  @Override
  public T visitDeltaBleed() {
    throw new NotImplementedException();
  }

  @Override
  public T visitDriftlessTheta() {
    throw new NotImplementedException();
  }

  @Override
  public T visitElasticity() {
    throw new NotImplementedException();
  }

  @Override
  public T visitGamma() {
    throw new NotImplementedException();
  }

  @Override
  public T visitGammaBleed() {
    throw new NotImplementedException();
  }

  @Override
  public T visitGammaP() {
    throw new NotImplementedException();
  }

  @Override
  public T visitGammaPBleed() {
    throw new NotImplementedException();
  }

  @Override
  public T visitPhi() {
    throw new NotImplementedException();
  }

  @Override
  public T visitPrice() {
    throw new NotImplementedException();
  }

  @Override
  public T visitRho() {
    throw new NotImplementedException();
  }

  @Override
  public T visitSpeed() {
    throw new NotImplementedException();
  }

  @Override
  public T visitSpeedP() {
    throw new NotImplementedException();
  }

  @Override
  public T visitStrikeDelta() {
    throw new NotImplementedException();
  }

  @Override
  public T visitStrikeGamma() {
    throw new NotImplementedException();
  }

  @Override
  public T visitDualDelta() {
    return visitStrikeDelta();
  }

  @Override
  public T visitDualGamma() {
    return visitStrikeGamma();
  }

  @Override
  public T visitTheta() {
    throw new NotImplementedException();
  }

  @Override
  public T visitUltima() {
    throw new NotImplementedException();
  }

  @Override
  public T visitVanna() {
    throw new NotImplementedException();
  }

  @Override
  public T visitVarianceUltima() {
    throw new NotImplementedException();
  }

  @Override
  public T visitVarianceVanna() {

    throw new NotImplementedException();
  }

  @Override
  public T visitVarianceVega() {
    throw new NotImplementedException();
  }

  @Override
  public T visitVarianceVomma() {
    throw new NotImplementedException();
  }

  @Override
  public T visitVega() {
    throw new NotImplementedException();
  }

  @Override
  public T visitVegaBleed() {

    throw new NotImplementedException();
  }

  @Override
  public T visitVegaP() {
    throw new NotImplementedException();
  }

  @Override
  public T visitVomma() {
    throw new NotImplementedException();
  }

  @Override
  public T visitVommaP() {
    throw new NotImplementedException();
  }

  @Override
  public T visitZeta() {
    throw new NotImplementedException();
  }

  @Override
  public T visitZetaBleed() {
    throw new NotImplementedException();
  }

  @Override
  public T visitZomma() {
    throw new NotImplementedException();
  }

  @Override
  public T visitZommaP() {
    throw new NotImplementedException();
  }

}
