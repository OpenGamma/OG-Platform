/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.greeks;

import com.google.common.collect.Sets;
import com.opengamma.financial.pnl.UnderlyingType;

/**
 * Definition of commonly-used greeks.
 */
public abstract class Greek implements Comparable<Greek> {
  /**
   * First-order sensitivity with respect to  cost of carry
   */
  public static final Greek CARRY_RHO = new Greek(new NthOrderUnderlying(1, UnderlyingType.COST_OF_CARRY), "CarryRho") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitCarryRho();
    }

  };
  /**
   * First-order sensitivity with respect to  spot 
   */
  public static final Greek DELTA = new Greek(new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE), "Delta") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitDelta();
    }

  };
  /**
   * Second-order sensitivity with respect to  spot and time
   */
  public static final Greek DELTA_BLEED = new Greek(new MixedOrderUnderlying(Sets.newHashSet(new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE), new NthOrderUnderlying(1,
      UnderlyingType.TIME))), "DeltaBleed") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitDeltaBleed();
    }

  };
  /**
   * 
   */
  public static final Greek DRIFTLESS_THETA = new Greek(null, "DriftlessTheta") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitDriftlessTheta();
    }

  };
  /**
   * Third-order sensitivity; first with respect to implied volatility, second with respect to spot
   */
  public static final Greek DVANNA_DVOL = new Greek(new MixedOrderUnderlying(Sets.newHashSet(new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VOLATILITY), new NthOrderUnderlying(
      2, UnderlyingType.SPOT_PRICE))), "DVannaDVol") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitDVannaDVol();
    }

  };
  /**
   * 
   */
  public static final Greek DZETA_DVOL = new Greek(null, "DZetaDVol") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitDZetaDVol();
    }

  };
  /**
   * 
   */
  public static final Greek ELASTICITY = new Greek(null, "Elasticity") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitElasticity();
    }

  };
  /**
   * Fair price 
   */
  public static final Greek FAIR_PRICE = new Greek(new NthOrderUnderlying(0, null), "FairPrice") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitPrice();
    }

  };
  /**
   * Second-order sensitivity with respect to  spot
   */
  public static final Greek GAMMA = new Greek(new NthOrderUnderlying(2, UnderlyingType.SPOT_PRICE), "Gamma") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitGamma();
    }

  };
  /**
   * Third-order sensitivity; first with respect to time, second with respect to spot
   */
  public static final Greek GAMMA_BLEED = new Greek(new MixedOrderUnderlying(Sets.newHashSet(new NthOrderUnderlying(1, UnderlyingType.TIME), new NthOrderUnderlying(2,
      UnderlyingType.SPOT_PRICE))), "GammaBleed") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitGammaBleed();
    }

  };
  /**
   * 
   */
  public static final Greek GAMMA_P = new Greek(null, "GammaP") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitGammaP();
    }

  };
  /**
   * 
   */
  public static final Greek GAMMA_P_BLEED = new Greek(null, "GammaPBleed") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitGammaPBleed();
    }

  };
  /**
   * First-order sensitivity with respect to  yield
   */
  public static final Greek PHI = new Greek(new NthOrderUnderlying(1, UnderlyingType.YIELD), "Phi") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitPhi();
    }

  };
  /**
   * First-order sensitivity with respect to  interest rate
   */
  public static final Greek RHO = new Greek(new NthOrderUnderlying(1, UnderlyingType.INTEREST_RATE), "Rho") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitRho();
    }

  };
  /**
   * Third-order sensitivity with respect to spot
   */
  public static final Greek SPEED = new Greek(new NthOrderUnderlying(3, UnderlyingType.SPOT_PRICE), "Speed") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitSpeed();
    }

  };
  /**
   * 
   */
  public static final Greek SPEED_P = new Greek(null, "SpeedP") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitSpeedP();
    }

  };
  /**
   * First-order sensitivity with respect to  strike
   */
  public static final Greek STRIKE_DELTA = new Greek(new NthOrderUnderlying(1, UnderlyingType.STRIKE), "StrikeDelta") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitStrikeDelta();
    }

  };
  /**
   * Second-order sensitivity with respect to  strike
   */
  public static final Greek STRIKE_GAMMA = new Greek(new NthOrderUnderlying(2, UnderlyingType.STRIKE), "StrikeGamma") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitStrikeGamma();
    }

  };
  /**
   * First-order sensitivity with respect to time 
   */
  public static final Greek THETA = new Greek(new NthOrderUnderlying(1, UnderlyingType.TIME), "Theta") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitTheta();
    }

  };
  /**
   * Third-order sensitivity with respect to implied volatility
   */
  public static final Greek ULTIMA = new Greek(new NthOrderUnderlying(3, UnderlyingType.IMPLIED_VOLATILITY), "Ultima") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitUltima();
    }

  };
  /**
   * Second-order sensitivity with respect to spot and implied volatility
   */
  public static final Greek VANNA = new Greek(new MixedOrderUnderlying(Sets.newHashSet(new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE), new NthOrderUnderlying(1,
      UnderlyingType.IMPLIED_VOLATILITY))), "Vanna") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitVanna();
    }

  };
  /**
   * Third-order sensitivity with respect to implied variance
   */
  public static final Greek VARIANCE_ULTIMA = new Greek(new NthOrderUnderlying(3, UnderlyingType.IMPLIED_VARIANCE), "VarianceUltima") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitVarianceUltima();
    }

  };
  /**
   * Second-order sensitivity with respect to spot and implied variance
   */
  public static final Greek VARIANCE_VANNA = new Greek(new MixedOrderUnderlying(Sets.newHashSet(new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE), new NthOrderUnderlying(1,
      UnderlyingType.IMPLIED_VARIANCE))), "VarianceVanna") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitVarianceVanna();
    }

  };
  /**
   * First-order sensitivity with respect to  implied variance
   */
  public static final Greek VARIANCE_VEGA = new Greek(new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VARIANCE), "VarianceVega") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitVarianceVega();
    }

  };
  /**
   * Second-order sensitivity with respect to  implied variance
   */
  public static final Greek VARIANCE_VOMMA = new Greek(new NthOrderUnderlying(2, UnderlyingType.IMPLIED_VARIANCE), "VarianceVomma") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitVarianceVomma();
    }

  };
  /**
   * First-order sensitivity with respect to  implied volatility
   */
  public static final Greek VEGA = new Greek(new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VOLATILITY), "Vega") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitVega();
    }

  };
  /**
   * Second-order sensitivity with respect to implied volatility and time
   */
  public static final Greek VEGA_BLEED = new Greek(new MixedOrderUnderlying(Sets.newHashSet(new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VOLATILITY), new NthOrderUnderlying(1,
      UnderlyingType.TIME))), "VegaBleed") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitVegaBleed();
    }

  };
  /**
   * First-order sensitivity with respect to percentage volatility
   */
  public static final Greek VEGA_P = new Greek(null, "VegaP") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitVegaP();
    }

  };
  /**
   * Second-order sensitivity with respect to implied volatility
   */
  public static final Greek VOMMA = new Greek(new NthOrderUnderlying(2, UnderlyingType.IMPLIED_VOLATILITY), "Vomma") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitVomma();
    }

  };
  /**
   * 
   */
  public static final Greek VOMMA_P = new Greek(null, "VommaP") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitVommaP();
    }

  };
  /**
   * 
   */
  public static final Greek ZETA = new Greek(null, "Zeta") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitZeta();
    }

  };
  /**
   * 
   */
  public static final Greek ZETA_BLEED = new Greek(null, "ZetaBleed") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitZetaBleed();
    }

  };
  /**
   * Third-order sensitivity; first with respect to time, second with respect to spot
   */
  public static final Greek ZOMMA = new Greek(new MixedOrderUnderlying(Sets.newHashSet(new NthOrderUnderlying(1, UnderlyingType.TIME), new NthOrderUnderlying(2,
      UnderlyingType.SPOT_PRICE))), "Zomma") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitZomma();
    }

  };
  /**
   * 
   */
  public static final Greek ZOMMA_P = new Greek(null, "ZommaP") {

    @Override
    public <T> T accept(GreekVisitor<T> visitor) {
      return visitor.visitZommaP();
    }

  };
  private final Underlying _underlying;
  private final String _name;

  public Greek(Underlying underlying, String name) {
    _underlying = underlying;
    _name = name;
  }

  public abstract <T> T accept(GreekVisitor<T> visitor);

  public Underlying getUnderlying() {
    return _underlying;
  }

  @Override
  public String toString() {
    return _name;
  }

  public int compareTo(Greek other) {
    return _name.compareTo(other._name);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    result = prime * result + ((_underlying == null) ? 0 : _underlying.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    Greek other = (Greek) obj;
    if (_name == null) {
      if (other._name != null) {
        return false;
      }
    } else if (!_name.equals(other._name)) {
      return false;
    }
    if (_underlying == null) {
      if (other._underlying != null) {
        return false;
      }
    } else if (!_underlying.equals(other._underlying)) {
      return false;
    }
    return true;
  }

}
