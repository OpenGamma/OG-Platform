/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.forwardcurve;

import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public abstract class ForwardCurveSpecification {
  private final ForwardCurveInstrumentProvider _curveInstrumentProvider;
  private final String _name;
  private final UniqueIdentifiable _target;

  public ForwardCurveSpecification(final String name, final UniqueIdentifiable target, final ForwardCurveInstrumentProvider curveInstrumentProvider) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(target, "target");
    ArgumentChecker.notNull(curveInstrumentProvider, "curve instrument provider");
    _name = name;
    _target = target;
    _curveInstrumentProvider = curveInstrumentProvider;
  }
  
  public String getName() {
    return _name;
  }

  public UniqueIdentifiable getTarget() {
    return _target;
  }

  public ForwardCurveInstrumentProvider getCurveInstrumentProvider() {
    return _curveInstrumentProvider;
  }

  @Override
  public int hashCode() {
    return getName().hashCode() + getTarget().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ForwardCurveSpecification)) {
      return false;
    }
    final ForwardCurveSpecification other = (ForwardCurveSpecification) obj;
    return getName().equals(other.getName()) &&
        getTarget().equals(other.getTarget()) &&
        getCurveInstrumentProvider().equals(other.getCurveInstrumentProvider());
  }

}
