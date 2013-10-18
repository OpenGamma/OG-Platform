/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.surface;

import com.opengamma.core.config.Config;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
@Config(description = "Future price curve specification")
public class FuturePriceCurveSpecification {
  private final String _name;
  private final UniqueIdentifiable _target;
  private final FuturePriceCurveInstrumentProvider<?> _curveInstrumentProvider;
  private final boolean _useUnderlyingForExpiry;

  public FuturePriceCurveSpecification(final String name, final UniqueIdentifiable target, final FuturePriceCurveInstrumentProvider<?> curveInstrumentProvider) {
    this(name, target, curveInstrumentProvider, false);
  }

  public FuturePriceCurveSpecification(final String name, final UniqueIdentifiable target, final FuturePriceCurveInstrumentProvider<?> curveInstrumentProvider,
      final boolean useUnderlyingForExpiry) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(curveInstrumentProvider, "curve instrument provider");
    _name = name;
    _target = target;
    _curveInstrumentProvider = curveInstrumentProvider;
    _useUnderlyingForExpiry = useUnderlyingForExpiry;
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

  public boolean isUseUnderlyingSecurityForExpiry() {
    return _useUnderlyingForExpiry;
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
           other.getTarget().equals(getTarget()) &&
           other.isUseUnderlyingSecurityForExpiry() == isUseUnderlyingSecurityForExpiry();
  }
}
