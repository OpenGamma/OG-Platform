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
public abstract class AbstractGreekVisitor<T> implements GreekVisitor<T> {

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitCarryRho()
   */
  @Override
  public T visitCarryRho() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitDVannaDVol()
   */
  @Override
  public T visitDVannaDVol() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitDZetaDVol()
   */
  @Override
  public T visitDZetaDVol() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitDelta()
   */
  @Override
  public T visitDelta() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitDeltaBleed()
   */
  @Override
  public T visitDeltaBleed() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitDriftlessTheta()
   */
  @Override
  public T visitDriftlessTheta() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitElasticity()
   */
  @Override
  public T visitElasticity() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitGamma()
   */
  @Override
  public T visitGamma() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitGammaBleed()
   */
  @Override
  public T visitGammaBleed() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitGammaP()
   */
  @Override
  public T visitGammaP() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitGammaPBleed()
   */
  @Override
  public T visitGammaPBleed() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitPhi()
   */
  @Override
  public T visitPhi() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitPrice()
   */
  @Override
  public T visitPrice() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitRho()
   */
  @Override
  public T visitRho() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitSpeed()
   */
  @Override
  public T visitSpeed() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitSpeedP()
   */
  @Override
  public T visitSpeedP() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitStrikeDelta()
   */
  @Override
  public T visitStrikeDelta() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitStrikeGamma()
   */
  @Override
  public T visitStrikeGamma() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitTheta()
   */
  @Override
  public T visitTheta() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitUltima()
   */
  @Override
  public T visitUltima() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVanna()
   */
  @Override
  public T visitVanna() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVarianceUltima()
   */
  @Override
  public T visitVarianceUltima() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVarianceVanna()
   */
  @Override
  public T visitVarianceVanna() {

    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVarianceVega()
   */
  @Override
  public T visitVarianceVega() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVarianceVomma()
   */
  @Override
  public T visitVarianceVomma() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVega()
   */
  @Override
  public T visitVega() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVegaBleed()
   */
  @Override
  public T visitVegaBleed() {

    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVegaP()
   */
  @Override
  public T visitVegaP() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVomma()
   */
  @Override
  public T visitVomma() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitVommaP()
   */
  @Override
  public T visitVommaP() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitZeta()
   */
  @Override
  public T visitZeta() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitZetaBleed()
   */
  @Override
  public T visitZetaBleed() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitZomma()
   */
  @Override
  public T visitZomma() {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.financial.greeks.GreekVisitor#visitZommaP()
   */
  @Override
  public T visitZommaP() {
    throw new UnsupportedOperationException();
  }

}
