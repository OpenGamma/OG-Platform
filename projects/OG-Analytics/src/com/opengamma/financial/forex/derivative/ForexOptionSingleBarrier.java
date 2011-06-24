/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.derivative;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.calculator.ForexDerivativeVisitor;
import com.opengamma.financial.model.option.definition.Barrier;

/**
 * Class describing a single-barrier FX option. The class wraps a vanilla European FX option ({@code ForexOptionVanilla}) and a {@code BarrierType}.
 */
public class ForexOptionSingleBarrier implements ForexDerivative {
  private final ForexOptionVanilla _underlyingOption;
  private final Barrier _barrier;

  /**
   * @param underlyingOption The underlying option
   * @param barrier The barrier
   */
  public ForexOptionSingleBarrier(final ForexOptionVanilla underlyingOption, final Barrier barrier) {
    Validate.notNull(underlyingOption, "underlying option");
    Validate.notNull(barrier, "barrier");
    _underlyingOption = underlyingOption;
    _barrier = barrier;
  }

  /**
   * @return The underlying (vanilla) option
   */
  public ForexOptionVanilla getUnderlyingOption() {
    return _underlyingOption;
  }

  /**
   * @return The barrier 
   */
  public Barrier getBarrier() {
    return _barrier;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _barrier.hashCode();
    result = prime * result + _underlyingOption.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ForexOptionSingleBarrier)) {
      return false;
    }
    final ForexOptionSingleBarrier other = (ForexOptionSingleBarrier) obj;
    if (!ObjectUtils.equals(_underlyingOption, other._underlyingOption)) {
      return false;
    }
    if (_barrier != other._barrier) {
      return false;
    }
    return true;
  }

  @Override
  public <S, T> T accept(final ForexDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitForexOptionSingleBarrier(this, data);
  }

  @Override
  public <T> T accept(final ForexDerivativeVisitor<?, T> visitor) {
    return visitor.visitForexOptionSingleBarrier(this);
  }

}
