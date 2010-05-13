/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import com.google.common.collect.Sets;
import com.opengamma.financial.pnl.UnderlyingType;

public enum Greek {

  FAIR_PRICE {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitPrice();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(0, null);
    }

  },
  ZETA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZeta();
    }

    @Override
    public Underlying getUnderlying() {
      return null;
    }

  },
  CARRY_RHO {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitCarryRho();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(1, UnderlyingType.COST_OF_CARRY);
    }

  },
  DELTA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDelta();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE);
    }

  },
  DRIFTLESS_THETA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDriftlessTheta();
    }

    @Override
    public Underlying getUnderlying() {
      return null;
    }

  },
  DZETA_DVOL {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDZetaDVol();
    }

    @Override
    public Underlying getUnderlying() {
      return null;
    }

  },
  ELASTICITY {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitElasticity();
    }

    @Override
    public Underlying getUnderlying() {
      return null;
    }

  },
  PHI {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitPhi();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(1, UnderlyingType.YIELD);
    }

  },
  RHO {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitRho();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(1, UnderlyingType.INTEREST_RATE);
    }

  },
  STRIKE_DELTA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitStrikeDelta();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(1, UnderlyingType.STRIKE);
    }

  },
  THETA {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitTheta();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(1, UnderlyingType.TIME);
    }

  },
  VARIANCE_VEGA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVarianceVega();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VARIANCE);
    }

  },
  VEGA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVega();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VOLATILITY);
    }

  },
  VEGA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVegaP();
    }

    @Override
    public Underlying getUnderlying() {
      return null;
    }

  },
  ZETA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZetaBleed();
    }

    @Override
    public Underlying getUnderlying() {
      return null;
    }

  },
  DELTA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDeltaBleed();
    }

    @Override
    public Underlying getUnderlying() {
      return new MixedOrderUnderlying(Sets.newHashSet(new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE), new NthOrderUnderlying(1, UnderlyingType.TIME)));
    }

  },
  GAMMA {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGamma();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(2, UnderlyingType.SPOT_PRICE);
    }

  },
  GAMMA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGammaP();
    }

    @Override
    public Underlying getUnderlying() {
      return null;
    }

  },
  STRIKE_GAMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitStrikeGamma();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(2, UnderlyingType.STRIKE);
    }

  },
  VANNA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVanna();
    }

    @Override
    public Underlying getUnderlying() {
      return new MixedOrderUnderlying(Sets.newHashSet(new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE), new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VOLATILITY)));
    }

  },
  VARIANCE_VANNA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVarianceVanna();
    }

    @Override
    public Underlying getUnderlying() {
      return new MixedOrderUnderlying(Sets.newHashSet(new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE), new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VARIANCE)));
    }

  },
  VARIANCE_VOMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVarianceVomma();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(2, UnderlyingType.IMPLIED_VARIANCE);
    }

  },
  VEGA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVegaBleed();
    }

    @Override
    public Underlying getUnderlying() {
      return new MixedOrderUnderlying(Sets.newHashSet(new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VOLATILITY), new NthOrderUnderlying(1, UnderlyingType.TIME)));
    }

  },
  VOMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVomma();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(2, UnderlyingType.IMPLIED_VOLATILITY);
    }

  },
  VOMMA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVommaP();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(2, UnderlyingType.IMPLIED_VOLATILITY);
    }

  },
  DVANNA_DVOL {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDVannaDVol();
    }

    @Override
    public Underlying getUnderlying() {
      return new MixedOrderUnderlying(Sets.newHashSet(new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VOLATILITY), new NthOrderUnderlying(2, UnderlyingType.SPOT_PRICE)));
    }

  },
  GAMMA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGammaBleed();
    }

    @Override
    public Underlying getUnderlying() {
      return new MixedOrderUnderlying(Sets.newHashSet(new NthOrderUnderlying(1, UnderlyingType.TIME), new NthOrderUnderlying(2, UnderlyingType.SPOT_PRICE)));
    }

  },
  GAMMA_P_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGammaPBleed();
    }

    @Override
    public Underlying getUnderlying() {
      return null;
    }

  },
  SPEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitSpeed();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(3, UnderlyingType.SPOT_PRICE);
    }

  },
  SPEED_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitSpeedP();
    }

    @Override
    public Underlying getUnderlying() {
      return null;
    }

  },
  ULTIMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitUltima();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(3, UnderlyingType.IMPLIED_VOLATILITY);
    }

  },
  VARIANCE_ULTIMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVarianceUltima();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(3, UnderlyingType.IMPLIED_VARIANCE);
    }

  },
  ZOMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZomma();
    }

    @Override
    public Underlying getUnderlying() {
      return new NthOrderUnderlying(3, UnderlyingType.IMPLIED_VOLATILITY);
    }

  },
  ZOMMA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZommaP();
    }

    @Override
    public Underlying getUnderlying() {
      return null;
    }

  };

  public abstract <T> T accept(GreekVisitor<T> visitor);

  public abstract Underlying getUnderlying();

}
