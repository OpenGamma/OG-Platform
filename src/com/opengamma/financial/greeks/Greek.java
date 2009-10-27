/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

public enum Greek {

  /**
   * 
   * Zeroth order greeks
   * 
   */
  PRICE {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitPrice();
    }
  },
  ZETA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZeta();
    }

  },

  /**
   * 
   * First order greeks
   * 
   */
  CARRY_RHO {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitCarryRho();
    }

  },
  DELTA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDelta();
    }

  },
  DRIFTLESS_THETA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDriftlessTheta();
    }

  },
  DZETA_DVOL {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDZetaDVol();
    }

  },
  ELASTICITY {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitElasticity();
    }

  },
  PHI {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitPhi();
    }

  },
  RHO {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitRho();
    }
  },
  STRIKE_DELTA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitStrikeDelta();
    }

  },
  THETA {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitTheta();
    }
  },
  TIME_BUCKETED_RHO {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitTimeBucketedRho();
    }
  },
  VARIANCE_VEGA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVarianceVega();
    }

  },
  VEGA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVega();
    }

  },
  VEGA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVegaP();
    }

  },
  ZETA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZetaBleed();
    }

  },

  /**
   * 
   * Second order greeks
   * 
   */
  DDELTA_DVAR {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDDeltaDVar();
    }

  },
  DELTA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDeltaBleed();
    }

  },
  GAMMA {
    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGamma();
    }
  },
  GAMMA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGammaP();
    }

  },
  STRIKE_GAMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitStrikeGamma();
    }

  },
  VANNA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVanna();
    }
  },
  VARIANCE_VOMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVarianceVomma();
    }

  },
  VEGA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVegaBleed();
    }

  },
  VOMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVomma();
    }

  },
  VOMMA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVommaP();
    }

  },

  /**
   * 
   * Third order greeks
   * 
   */
  DVANNA_DVOL {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDVannaDVol();
    }

  },
  GAMMA_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGammaBleed();
    }

  },
  GAMMA_P_BLEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGammaPBleed();
    }

  },
  SPEED {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitSpeed();
    }

  },
  SPEED_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitSpeedP();
    }

  },
  ULTIMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitUltima();
    }

  },
  VARIANCE_ULTIMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVarianceUltima();
    }

  },
  ZOMMA {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZomma();
    }

  },
  ZOMMA_P {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZommaP();
    }

  };

  public abstract <T> T accept(GreekVisitor<T> visitor);
}
