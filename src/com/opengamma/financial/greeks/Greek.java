/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import com.opengamma.financial.pnl.Underlying;

public enum Greek {

  PRICE {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitPrice();
    }

    @Override
    public Order getOrder() {
      return new ZerothOrder();
    }

  },
  ZETA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZeta();
    }

    @Override
    public Order getOrder() {
      throw new UnsupportedOperationException();
    }

  },
  CARRY_RHO {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitCarryRho();
    }

    @Override
    public Order getOrder() {
      return new FirstOrder(Underlying.COST_OF_CARRY);
    }

  },
  DELTA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDelta();
    }

    @Override
    public Order getOrder() {
      return new FirstOrder(Underlying.SPOT_PRICE);
    }

  },
  DRIFTLESS_THETA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDriftlessTheta();
    }

    @Override
    public Order getOrder() {
      throw new UnsupportedOperationException();
    }

  },
  DZETA_DVOL {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDZetaDVol();
    }

    @Override
    public Order getOrder() {
      throw new UnsupportedOperationException();
    }

  },
  ELASTICITY {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitElasticity();
    }

    @Override
    public Order getOrder() {
      return new SecondOrder(Underlying.SPOT_PRICE);
    }

  },
  PHI {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitPhi();
    }

    @Override
    public Order getOrder() {
      return new FirstOrder(Underlying.YIELD);
    }

  },
  RHO {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitRho();
    }

    @Override
    public Order getOrder() {
      return new FirstOrder(Underlying.INTEREST_RATE);
    }

  },
  STRIKE_DELTA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitStrikeDelta();
    }

    @Override
    public Order getOrder() {
      return new FirstOrder(Underlying.STRIKE);
    }

  },
  THETA {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitTheta();
    }

    @Override
    public Order getOrder() {
      return new FirstOrder(Underlying.TIME);
    }

  },
  VARIANCE_VEGA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVarianceVega();
    }

    @Override
    public Order getOrder() {
      return new FirstOrder(Underlying.IMPLIED_VARIANCE);
    }

  },
  VEGA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVega();
    }

    @Override
    public Order getOrder() {
      return new FirstOrder(Underlying.IMPLIED_VOLATILITY);
    }

  },
  VEGA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVegaP();
    }

    @Override
    public Order getOrder() {
      return new FirstOrder(Underlying.IMPLIED_VOLATILITY);
    }

  },
  ZETA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZetaBleed();
    }

    @Override
    public Order getOrder() {
      throw new UnsupportedOperationException();
    }

  },
  DELTA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDeltaBleed();
    }

    @Override
    public Order getOrder() {
      return new MixedSecondOrder(new FirstOrder(Underlying.SPOT_PRICE), new FirstOrder(Underlying.TIME));
    }

  },
  GAMMA {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGamma();
    }

    @Override
    public Order getOrder() {
      return new SecondOrder(Underlying.SPOT_PRICE);
    }

  },
  GAMMA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGammaP();
    }

    @Override
    public Order getOrder() {
      return new SecondOrder(Underlying.SPOT_PRICE);
    }

  },
  STRIKE_GAMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitStrikeGamma();
    }

    @Override
    public Order getOrder() {
      return new SecondOrder(Underlying.STRIKE);
    }

  },
  VANNA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVanna();
    }

    @Override
    public Order getOrder() {
      return new MixedSecondOrder(new FirstOrder(Underlying.SPOT_PRICE), new FirstOrder(Underlying.IMPLIED_VOLATILITY));
    }

  },
  VARIANCE_VANNA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVarianceVanna();
    }

    @Override
    public Order getOrder() {
      return new MixedSecondOrder(new FirstOrder(Underlying.SPOT_PRICE), new FirstOrder(Underlying.IMPLIED_VARIANCE));
    }

  },
  VARIANCE_VOMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVarianceVomma();
    }

    @Override
    public Order getOrder() {
      return new SecondOrder(Underlying.IMPLIED_VARIANCE);
    }

  },
  VEGA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVegaBleed();
    }

    @Override
    public Order getOrder() {
      return new MixedSecondOrder(new FirstOrder(Underlying.IMPLIED_VOLATILITY), new FirstOrder(Underlying.TIME));
    }

  },
  VOMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVomma();
    }

    @Override
    public Order getOrder() {
      return new SecondOrder(Underlying.IMPLIED_VOLATILITY);
    }

  },
  VOMMA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVommaP();
    }

    @Override
    public Order getOrder() {
      return new SecondOrder(Underlying.IMPLIED_VOLATILITY);
    }

  },
  DVANNA_DVOL {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDVannaDVol();
    }

    @Override
    public Order getOrder() {
      return new MixedThirdOrder(new FirstOrder(Underlying.IMPLIED_VOLATILITY), new SecondOrder(Underlying.SPOT_PRICE));
    }

  },
  GAMMA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGammaBleed();
    }

    @Override
    public Order getOrder() {
      return new MixedThirdOrder(new FirstOrder(Underlying.TIME), new SecondOrder(Underlying.SPOT_PRICE));
    }

  },
  GAMMA_P_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGammaPBleed();
    }

    @Override
    public Order getOrder() {
      return new MixedThirdOrder(new FirstOrder(Underlying.TIME), new SecondOrder(Underlying.SPOT_PRICE));
    }

  },
  SPEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitSpeed();
    }

    @Override
    public Order getOrder() {
      return new ThirdOrder(Underlying.SPOT_PRICE);
    }

  },
  SPEED_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitSpeedP();
    }

    @Override
    public Order getOrder() {
      return new ThirdOrder(Underlying.SPOT_PRICE);
    }

  },
  ULTIMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitUltima();
    }

    @Override
    public Order getOrder() {
      return new ThirdOrder(Underlying.IMPLIED_VOLATILITY);
    }

  },
  VARIANCE_ULTIMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVarianceUltima();
    }

    @Override
    public Order getOrder() {
      return new ThirdOrder(Underlying.IMPLIED_VARIANCE);
    }

  },
  ZOMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZomma();
    }

    @Override
    public Order getOrder() {
      return new SecondOrder(Underlying.IMPLIED_VOLATILITY);
    }

  },
  ZOMMA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZommaP();
    }

    @Override
    public Order getOrder() {
      return new SecondOrder(Underlying.IMPLIED_VOLATILITY);
    }

  };

  public abstract <T> T accept(GreekVisitor<T> visitor);

  public abstract Order getOrder();

}
