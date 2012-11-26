/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.derivative;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.util.ArgumentChecker;

/**
 * energy future option commodity derivative
 */
public class EnergyFutureOption extends CommodityFutureOption<EnergyFuture> {

  /**
   * Constructor for future options
   *
   * @param expiry Time (in years as a double) until the date-time at which the future expires
   * @param underlying Underlying future
   * @param strike Strike price
   * @param exerciseType Exercise type - European or American
   * @param isCall Call if true, Put if false
   */
  public EnergyFutureOption(final double expiry, final EnergyFuture underlying, final double strike, final ExerciseDecisionType exerciseType, final boolean isCall) {
    super(expiry, underlying, strike, exerciseType, isCall);
  }

  @Override
  public <S, T> T accept(final InstrumentDerivativeVisitor<S, T> visitor, final S data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEnergyFutureOption(this, data);
  }

  @Override
  public <T> T accept(final InstrumentDerivativeVisitor<?, T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitEnergyFutureOption(this);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof EnergyFutureOption)) {
      return false;
    }
    return super.equals(obj);
  }
}
