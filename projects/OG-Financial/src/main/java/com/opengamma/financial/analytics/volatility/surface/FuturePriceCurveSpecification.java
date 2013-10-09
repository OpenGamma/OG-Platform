/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import org.apache.commons.lang.Validate;

import com.opengamma.core.config.Config;
import com.opengamma.id.UniqueIdentifiable;

/**
 * 
 */
@Config(description = "Future price curve specification")
public class FuturePriceCurveSpecification {
  private String _name;
  private UniqueIdentifiable _target;
  private FuturePriceCurveInstrumentProvider<?> _curveInstrumentProvider;

  public FuturePriceCurveSpecification(final String name, final UniqueIdentifiable target, final FuturePriceCurveInstrumentProvider<?> curveInstrumentProvider) {
    Validate.notNull(name, "name");
    Validate.notNull(target, "target");
    Validate.notNull(curveInstrumentProvider, "curve instrument provider");
    _name = name;
    _target = target;
    _curveInstrumentProvider = curveInstrumentProvider;
  }

  public String getName() {
    return _name;
  }

  public FuturePriceCurveInstrumentProvider<?> getCurveInstrumentProvider() {
    return _curveInstrumentProvider;
  }

  public UniqueIdentifiable getTarget() {
    return _target;
  }

  @Override
  public int hashCode() {
    return getName().hashCode() * getTarget().hashCode();
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return false;
    }
    if (!(o instanceof FuturePriceCurveSpecification)) {
      return false;
    }
    final FuturePriceCurveSpecification other = (FuturePriceCurveSpecification) o;
    return other.getName().equals(getName()) &&
           other.getCurveInstrumentProvider().equals(getCurveInstrumentProvider()) &&
           other.getTarget().equals(getTarget());
  }
}
