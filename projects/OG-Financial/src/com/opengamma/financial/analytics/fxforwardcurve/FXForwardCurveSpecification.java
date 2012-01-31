/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fxforwardcurve;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public class FXForwardCurveSpecification {
  private final FXForwardCurveInstrumentProvider _curveInstrumentProvider;
  private final String _name;
  private final UnorderedCurrencyPair _target;

  public FXForwardCurveSpecification(final String name, final UnorderedCurrencyPair target, final FXForwardCurveInstrumentProvider curveInstrumentProvider) {
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

  public UnorderedCurrencyPair getTarget() {
    return _target;
  }

  public FXForwardCurveInstrumentProvider getCurveInstrumentProvider() {
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
    if (!(obj instanceof FXForwardCurveSpecification)) {
      return false;
    }
    final FXForwardCurveSpecification other = (FXForwardCurveSpecification) obj;
    return getName().equals(other.getName()) &&
        getTarget().equals(other.getTarget()) &&
        getCurveInstrumentProvider().equals(other.getCurveInstrumentProvider());
  }


}
