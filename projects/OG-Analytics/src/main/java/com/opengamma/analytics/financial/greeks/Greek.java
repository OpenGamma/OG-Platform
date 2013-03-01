/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.greeks;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.pnl.UnderlyingType;

/**
 * Definition of commonly-used greeks.
 */
public abstract class Greek implements Comparable<Greek> {
  /**
   * First-order sensitivity with respect to cost of carry
   */
  public static final Greek CARRY_RHO = new Greek(new NthOrderUnderlying(1, UnderlyingType.COST_OF_CARRY), "CarryRho") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitCarryRho();
    }

  };
  /**
   * First-order sensitivity with respect to spot
   */
  public static final Greek DELTA = new Greek(new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE), "Delta") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDelta();
    }

  };
  /**
   * Second-order sensitivity with respect to spot and time
   */
  public static final Greek DELTA_BLEED = new Greek(new MixedOrderUnderlying(Arrays.asList(new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE), new NthOrderUnderlying(1,
      UnderlyingType.TIME))),
      "DeltaBleed") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDeltaBleed();
    }

  };
  /**
   * 
   */
  public static final Greek DRIFTLESS_THETA = new Greek(null, "DriftlessTheta") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDriftlessTheta();
    }

  };
  /**
   * Third-order sensitivity; first with respect to implied volatility, second with respect to spot
   */
  public static final Greek DVANNA_DVOL = new Greek(new MixedOrderUnderlying(Arrays.asList(new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VOLATILITY), new NthOrderUnderlying(2,
      UnderlyingType.SPOT_PRICE))), "DVannaDVol") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDVannaDVol();
    }

  };
  /**
   * 
   */
  public static final Greek DZETA_DVOL = new Greek(null, "DZetaDVol") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitDZetaDVol();
    }

  };
  /**
   * 
   */
  public static final Greek ELASTICITY = new Greek(null, "Elasticity") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitElasticity();
    }

  };
  /**
   * Fair price
   */
  public static final Greek FAIR_PRICE = new Greek(new NthOrderUnderlying(0, null), "FairPrice") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitPrice();
    }

  };
  /**
   * Second-order sensitivity with respect to spot
   */
  public static final Greek GAMMA = new Greek(new NthOrderUnderlying(2, UnderlyingType.SPOT_PRICE), "Gamma") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGamma();
    }

  };
  /**
   * Third-order sensitivity; first with respect to time, second with respect to spot
   */
  public static final Greek GAMMA_BLEED = new Greek(new MixedOrderUnderlying(Arrays.asList(new NthOrderUnderlying(1, UnderlyingType.TIME), new NthOrderUnderlying(2,
      UnderlyingType.SPOT_PRICE))),
      "GammaBleed") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGammaBleed();
    }

  };
  /**
   * 
   */
  public static final Greek GAMMA_P = new Greek(null, "GammaP") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGammaP();
    }

  };
  /**
   * 
   */
  public static final Greek GAMMA_P_BLEED = new Greek(null, "GammaPBleed") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitGammaPBleed();
    }

  };
  /**
   * First-order sensitivity with respect to yield
   */
  public static final Greek PHI = new Greek(new NthOrderUnderlying(1, UnderlyingType.YIELD), "Phi") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitPhi();
    }

  };
  /**
   * First-order sensitivity with respect to interest rate
   */
  public static final Greek RHO = new Greek(new NthOrderUnderlying(1, UnderlyingType.INTEREST_RATE), "Rho") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitRho();
    }

  };
  /**
   * Third-order sensitivity with respect to spot
   */
  public static final Greek SPEED = new Greek(new NthOrderUnderlying(3, UnderlyingType.SPOT_PRICE), "Speed") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitSpeed();
    }

  };
  /**
   * 
   */
  public static final Greek SPEED_P = new Greek(null, "SpeedP") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitSpeedP();
    }

  };
  /**
   * First-order sensitivity with respect to strike
   */
  public static final Greek STRIKE_DELTA = new Greek(new NthOrderUnderlying(1, UnderlyingType.STRIKE), "StrikeDelta") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitStrikeDelta();
    }

  };
  /**
   * First-order sensitivity with respect to strike
   */
  public static final Greek DUAL_DELTA = new Greek(new NthOrderUnderlying(1, UnderlyingType.STRIKE), "DualDelta") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitStrikeDelta();
    }

  };
  /**
   * Second-order sensitivity with respect to strike
   */
  public static final Greek STRIKE_GAMMA = new Greek(new NthOrderUnderlying(2, UnderlyingType.STRIKE), "StrikeGamma") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitStrikeGamma();
    }

  };
  /**
   * Second-order sensitivity with respect to strike
   */
  public static final Greek DUAL_GAMMA = new Greek(new NthOrderUnderlying(2, UnderlyingType.STRIKE), "DualGamma") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitStrikeGamma();
    }

  };
  /**
   * First-order sensitivity with respect to time
   */
  public static final Greek THETA = new Greek(new NthOrderUnderlying(1, UnderlyingType.TIME), "Theta") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitTheta();
    }

  };
  /**
   * Third-order sensitivity with respect to implied volatility
   */
  public static final Greek ULTIMA = new Greek(new NthOrderUnderlying(3, UnderlyingType.IMPLIED_VOLATILITY), "Ultima") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitUltima();
    }

  };
  /**
   * Second-order sensitivity with respect to spot and implied volatility
   */
  public static final Greek VANNA = new Greek(new MixedOrderUnderlying(Arrays.asList(new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE), new NthOrderUnderlying(1,
      UnderlyingType.IMPLIED_VOLATILITY))), "Vanna") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVanna();
    }

  };
  /**
   * Third-order sensitivity with respect to implied variance
   */
  public static final Greek VARIANCE_ULTIMA = new Greek(new NthOrderUnderlying(3, UnderlyingType.IMPLIED_VARIANCE), "VarianceUltima") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVarianceUltima();
    }

  };
  /**
   * Second-order sensitivity with respect to spot and implied variance
   */
  public static final Greek VARIANCE_VANNA = new Greek(new MixedOrderUnderlying(Arrays.asList(new NthOrderUnderlying(1, UnderlyingType.SPOT_PRICE), new NthOrderUnderlying(1,
      UnderlyingType.IMPLIED_VARIANCE))), "VarianceVanna") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVarianceVanna();
    }

  };
  /**
   * First-order sensitivity with respect to implied variance
   */
  public static final Greek VARIANCE_VEGA = new Greek(new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VARIANCE), "VarianceVega") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVarianceVega();
    }

  };
  /**
   * Second-order sensitivity with respect to implied variance
   */
  public static final Greek VARIANCE_VOMMA = new Greek(new NthOrderUnderlying(2, UnderlyingType.IMPLIED_VARIANCE), "VarianceVomma") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVarianceVomma();
    }

  };
  /**
   * First-order sensitivity with respect to implied volatility
   */
  public static final Greek VEGA = new Greek(new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VOLATILITY), "Vega") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVega();
    }

  };
  /**
   * Second-order sensitivity with respect to implied volatility and time
   */
  public static final Greek VEGA_BLEED = new Greek(new MixedOrderUnderlying(Arrays.asList(new NthOrderUnderlying(1, UnderlyingType.IMPLIED_VOLATILITY),
      new NthOrderUnderlying(1, UnderlyingType.TIME))), "VegaBleed") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVegaBleed();
    }

  };
  /**
   * First-order sensitivity with respect to percentage volatility
   */
  public static final Greek VEGA_P = new Greek(null, "VegaP") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVegaP();
    }

  };
  /**
   * Second-order sensitivity with respect to implied volatility
   */
  public static final Greek VOMMA = new Greek(new NthOrderUnderlying(2, UnderlyingType.IMPLIED_VOLATILITY), "Vomma") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVomma();
    }

  };
  /**
   * 
   */
  public static final Greek VOMMA_P = new Greek(null, "VommaP") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitVommaP();
    }

  };
  /**
   * 
   */
  public static final Greek ZETA = new Greek(null, "Zeta") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZeta();
    }

  };
  /**
   * 
   */
  public static final Greek ZETA_BLEED = new Greek(null, "ZetaBleed") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZetaBleed();
    }

  };
  /**
   * Third-order sensitivity; first with respect to time, second with respect to spot
   */
  public static final Greek ZOMMA = new Greek(new MixedOrderUnderlying(Arrays.asList(new NthOrderUnderlying(1, UnderlyingType.TIME),
      new NthOrderUnderlying(2, UnderlyingType.SPOT_PRICE))), "Zomma") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZomma();
    }

  };
  /**
   * 
   */
  public static final Greek ZOMMA_P = new Greek(null, "ZommaP") {

    @Override
    public <T> T accept(final GreekVisitor<T> visitor) {
      return visitor.visitZommaP();
    }

  };
  /** A set containing all greeks that can be calculated */
  private static final Set<Greek> ALL_GREEKS;

  //REVIEW elaine 9-7-2010 This is not ideal, because adding a new greek definition means remembering to put it in here
  static {
    ALL_GREEKS = new HashSet<>();
    ALL_GREEKS.add(CARRY_RHO);
    ALL_GREEKS.add(DELTA);
    ALL_GREEKS.add(DELTA_BLEED);
    ALL_GREEKS.add(DRIFTLESS_THETA);
    ALL_GREEKS.add(DUAL_DELTA);
    ALL_GREEKS.add(DUAL_GAMMA);
    ALL_GREEKS.add(DVANNA_DVOL);
    ALL_GREEKS.add(DZETA_DVOL);
    ALL_GREEKS.add(ELASTICITY);
    ALL_GREEKS.add(FAIR_PRICE);
    ALL_GREEKS.add(GAMMA);
    ALL_GREEKS.add(GAMMA_BLEED);
    ALL_GREEKS.add(GAMMA_P);
    ALL_GREEKS.add(GAMMA_P_BLEED);
    ALL_GREEKS.add(PHI);
    ALL_GREEKS.add(RHO);
    ALL_GREEKS.add(SPEED);
    ALL_GREEKS.add(SPEED_P);
    ALL_GREEKS.add(STRIKE_DELTA);
    ALL_GREEKS.add(STRIKE_GAMMA);
    ALL_GREEKS.add(THETA);
    ALL_GREEKS.add(ULTIMA);
    ALL_GREEKS.add(VANNA);
    ALL_GREEKS.add(VARIANCE_ULTIMA);
    ALL_GREEKS.add(VARIANCE_VEGA);
    ALL_GREEKS.add(VARIANCE_VOMMA);
    ALL_GREEKS.add(VEGA);
    ALL_GREEKS.add(VEGA_BLEED);
    ALL_GREEKS.add(VEGA_P);
    ALL_GREEKS.add(VOMMA);
    ALL_GREEKS.add(VOMMA_P);
    ALL_GREEKS.add(ZETA);
    ALL_GREEKS.add(ZETA_BLEED);
    ALL_GREEKS.add(ZOMMA);
    ALL_GREEKS.add(ZOMMA_P);
  }

  /**
   * @return A set containing all greeks
   */
  public static final Set<Greek> getAllGreeks() {
    return ALL_GREEKS;
  }

  /** The underlying of the greek */
  private final Underlying _underlying;
  /** The name of the greek */
  private final String _name;

  /**
   * @param underlying The underlying
   * @param name The name
   */
  public Greek(final Underlying underlying, final String name) {
    _underlying = underlying;
    _name = name;
  }

  /**
   * accept() method for the visitor pattern
   * @param visitor A visitor
   * @param <T> The type of the result
   * @return The result returned by the visitor
   */
  public abstract <T> T accept(GreekVisitor<T> visitor);

  /**
   * @return The underlying
   */
  public Underlying getUnderlying() {
    return _underlying;
  }

  @Override
  public String toString() {
    return _name;
  }

  @Override
  public int compareTo(final Greek other) {
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
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (this == obj) {
      return true;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Greek other = (Greek) obj;
    return ObjectUtils.equals(_name, other._name) && ObjectUtils.equals(_underlying, other._underlying);
  }

}
