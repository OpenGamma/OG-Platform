/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.forex.definition;

import javax.time.calendar.ZonedDateTime;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.forex.calculator.ForexConverter;
import com.opengamma.financial.forex.calculator.ForexDefinitionVisitor;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.financial.model.option.definition.Barrier;

/**
 * Class describing a single-barrier FX option definition. The class wraps a vanilla European FX option ({@code ForexOptionVanillaDefinition}) and a 
 * {code BarrierType}.
 */
public class ForexOptionSingleBarrierDefinition implements ForexConverter<ForexDerivative> {
  private final ForexOptionVanillaDefinition _underlyingOption;
  private final Barrier _barrier;

  /**
   * Constructor from the details
   * @param underlyingOption The underlying (vanilla) option
   * @param barrier The barrier type
   */
  public ForexOptionSingleBarrierDefinition(final ForexOptionVanillaDefinition underlyingOption, final Barrier barrier) {
    Validate.notNull(underlyingOption, "underlying option");
    Validate.notNull(barrier, "barrier");
    _underlyingOption = underlyingOption;
    _barrier = barrier;
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

  @Override
  public ForexOptionSingleBarrier toDerivative(final ZonedDateTime date, final String... yieldCurveNames) {
    Validate.notNull(date, "date");
    Validate.notNull(yieldCurveNames, "yield curve names");
    final ForexOptionVanilla underlying = _underlyingOption.toDerivative(date, yieldCurveNames);
    return new ForexOptionSingleBarrier(underlying, _barrier);
  }

  @Override
  public <U, V> V accept(final ForexDefinitionVisitor<U, V> visitor, final U data) {
    return visitor.visitForexOptionSingleBarrierDefiniton(this, data);
  }

  @Override
  public <V> V accept(final ForexDefinitionVisitor<?, V> visitor) {
    return visitor.visitForexOptionSingleBarrierDefiniton(this);
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
    if (!(obj instanceof ForexOptionSingleBarrierDefinition)) {
      return false;
    }
    final ForexOptionSingleBarrierDefinition other = (ForexOptionSingleBarrierDefinition) obj;
    if (_barrier != other._barrier) {
      return false;
    }
    if (!ObjectUtils.equals(_underlyingOption, other._underlyingOption)) {
      return false;
    }
    return true;
  }

}
