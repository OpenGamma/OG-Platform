/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.forex.definition;

import org.apache.commons.lang.ObjectUtils;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinitionVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.Barrier;
import com.opengamma.analytics.financial.model.option.definition.Barrier.BarrierType;
import com.opengamma.util.ArgumentChecker;

/**
 * Class describing a single-barrier FX option definition. The class wraps a vanilla European FX option ({@code ForexOptionVanillaDefinition}) and a
 * {@link BarrierType}.
 * It is suppose that the barrier has not been activated yet (and thus there is no flag indicated if the activation took place already).
 */
public class ForexOptionSingleBarrierDefinition implements InstrumentDefinition<InstrumentDerivative> {

  /**
   * The underlying vanilla Forex option.
   */
  private final ForexOptionVanillaDefinition _underlyingOption;
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
   * @param underlyingOption The underlying (vanilla) option
   * @param barrier The barrier type
   */
  public ForexOptionSingleBarrierDefinition(final ForexOptionVanillaDefinition underlyingOption, final Barrier barrier) {
    ArgumentChecker.notNull(underlyingOption, "underlying option");
    ArgumentChecker.notNull(barrier, "barrier");
    _underlyingOption = underlyingOption;
    _barrier = barrier;
    _rebate = 0.0;
  }

  /**
   * Constructor from the details.
   * @param underlyingOption The underlying (vanilla) option.
   * @param barrier The barrier type.
   * @param rebate The rebate amount (in domestic currency).
   */
  public ForexOptionSingleBarrierDefinition(final ForexOptionVanillaDefinition underlyingOption, final Barrier barrier, final double rebate) {
    ArgumentChecker.notNull(underlyingOption, "underlying option");
    ArgumentChecker.notNull(barrier, "barrier");
    _underlyingOption = underlyingOption;
    _barrier = barrier;
    _rebate = rebate;
  }

  /**
   * @return The underlying (vanilla) option
   */
  public ForexOptionVanillaDefinition getUnderlyingOption() {
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
   * {@inheritDoc}
   * @deprecated Use the method that does not take yield curve names
   */
  @Deprecated
  @Override
  public ForexOptionSingleBarrier toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    throw new UnsupportedOperationException(this.getClass().getCanonicalName());
  }

  @Override
  public ForexOptionSingleBarrier toDerivative(final ZonedDateTime date) {
    ArgumentChecker.notNull(date, "date");
    final ForexOptionVanilla underlying = _underlyingOption.toDerivative(date);
    return new ForexOptionSingleBarrier(underlying, _barrier);
  }

  @Override
  public <U, V> V accept(final InstrumentDefinitionVisitor<U, V> visitor, final U data) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForexOptionSingleBarrierDefiniton(this, data);
  }

  @Override
  public <V> V accept(final InstrumentDefinitionVisitor<?, V> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitForexOptionSingleBarrierDefiniton(this);
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
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ForexOptionSingleBarrierDefinition other = (ForexOptionSingleBarrierDefinition) obj;
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
