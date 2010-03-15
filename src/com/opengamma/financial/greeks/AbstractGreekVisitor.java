/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import org.apache.commons.lang.NotImplementedException;

/**
 * @author emcleod
 * 
 */
public abstract class AbstractGreekVisitor<T> implements GreekVisitor<T> {

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitCarryRho()
   */
  @Override
  public T visitCarryRho() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitDVannaDVol()
   */
  @Override
  public T visitDVannaDVol() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitDZetaDVol()
   */
  @Override
  public T visitDZetaDVol() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitDelta()
   */
  @Override
  public T visitDelta() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitDeltaBleed()
   */
  @Override
  public T visitDeltaBleed() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitDriftlessTheta()
   */
  @Override
  public T visitDriftlessTheta() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitElasticity()
   */
  @Override
  public T visitElasticity() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitGamma()
   */
  @Override
  public T visitGamma() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitGammaBleed()
   */
  @Override
  public T visitGammaBleed() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitGammaP()
   */
  @Override
  public T visitGammaP() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitGammaPBleed()
   */
  @Override
  public T visitGammaPBleed() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitPhi()
   */
  @Override
  public T visitPhi() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitPrice()
   */
  @Override
  public T visitPrice() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitRho()
   */
  @Override
  public T visitRho() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitSpeed()
   */
  @Override
  public T visitSpeed() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitSpeedP()
   */
  @Override
  public T visitSpeedP() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitStrikeDelta()
   */
  @Override
  public T visitStrikeDelta() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitStrikeGamma()
   */
  @Override
  public T visitStrikeGamma() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitTheta()
   */
  @Override
  public T visitTheta() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitUltima()
   */
  @Override
  public T visitUltima() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVanna()
   */
  @Override
  public T visitVanna() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVarianceUltima()
   */
  @Override
  public T visitVarianceUltima() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVarianceVanna()
   */
  @Override
  public T visitVarianceVanna() {

    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVarianceVega()
   */
  @Override
  public T visitVarianceVega() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVarianceVomma()
   */
  @Override
  public T visitVarianceVomma() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVega()
   */
  @Override
  public T visitVega() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVegaBleed()
   */
  @Override
  public T visitVegaBleed() {

    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVegaP()
   */
  @Override
  public T visitVegaP() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVomma()
   */
  @Override
  public T visitVomma() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVommaP()
   */
  @Override
  public T visitVommaP() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitZeta()
   */
  @Override
  public T visitZeta() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitZetaBleed()
   */
  @Override
  public T visitZetaBleed() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitZomma()
   */
  @Override
  public T visitZomma() {
    throw new NotImplementedException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitZommaP()
   */
  @Override
  public T visitZommaP() {
    throw new NotImplementedException();
  }

}
