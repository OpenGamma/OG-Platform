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
import com.opengamma.util.money.Currency;

/**
 * Class describing a single-barrier FX option. The class wraps a vanilla European FX option ({@code ForexOptionVanilla}) and a {@code BarrierType}.
 * It is suppose that the barrier has not been activated yet (and thus there is no flag indicated if the activation took place already).
 */
public class ForexOptionSingleBarrier implements ForexDerivative {

  /**
   * The underlying vanilla Forex option.
   */
  private final ForexOptionVanilla _underlyingOption;
  /**
   * The barrier description.
   */
  private final Barrier _barrier;
  /**
   * The amount paid back to the option holder in case the option expires inactive (in domestic currency).
   */
  private final double _rebate;

  /**
   * Constructor from the details with 0 rebate.
   * @param underlyingOption The underlying option
   * @param barrier The barrier
   */
  public ForexOptionSingleBarrier(final ForexOptionVanilla underlyingOption, final Barrier barrier) {
    Validate.notNull(underlyingOption, "underlying option");
    Validate.notNull(barrier, "barrier");
    _underlyingOption = underlyingOption;
    _barrier = barrier;
    _rebate = 0.0;
  }

  /**
   * Constructor from the details with 0 rebate.
   * @param underlyingOption The underlying option
   * @param barrier The barrier.
   * @param rebate The rebate amount (in domestic currency).
   */
  public ForexOptionSingleBarrier(final ForexOptionVanilla underlyingOption, final Barrier barrier, final double rebate) {
    Validate.notNull(underlyingOption, "underlying option");
    Validate.notNull(barrier, "barrier");
    Validate.isTrue(rebate >= 0.0, "Rebate is positive or null");
    _underlyingOption = underlyingOption;
    _barrier = barrier;
    _rebate = rebate;
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

  /**
   * Gets the rebate amount (in domestic currency).
   * @return The rebate.
   */
  public double getRebate() {
    return _rebate;
  }

  /**
   * Gets the first currency.
   * @return The currency.
   */
  public Currency getCurrency1() {
    return _underlyingOption.getCurrency1();
  }

  /**
   * Gets the second currency.
   * @return The currency.
   */
  public Currency getCurrency2() {
    return _underlyingOption.getCurrency2();
  }

  @Override
  public <S, T> T accept(final ForexDerivativeVisitor<S, T> visitor, final S data) {
    return visitor.visitForexOptionSingleBarrier(this, data);
  }

  @Override
  public <T> T accept(final ForexDerivativeVisitor<?, T> visitor) {
    return visitor.visitForexOptionSingleBarrier(this);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _barrier.hashCode();
    long temp;
    temp = Double.doubleToLongBits(_rebate);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + _underlyingOption.hashCode();
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
    ForexOptionSingleBarrier other = (ForexOptionSingleBarrier) obj;
    if (_barrier != other._barrier) {
      return false;
    }
    if (Double.doubleToLongBits(_rebate) != Double.doubleToLongBits(other._rebate)) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingOption, other._underlyingOption)) {
      return false;
    }
    return true;
  }

}
