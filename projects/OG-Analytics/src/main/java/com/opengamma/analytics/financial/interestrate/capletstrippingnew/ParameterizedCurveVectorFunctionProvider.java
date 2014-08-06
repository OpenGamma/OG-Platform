/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.math.function.ParameterizedCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class ParameterizedCurveVectorFunctionProvider extends DoublesVectorFunctionProvider {

  private final ParameterizedCurve _pCurve;

  public ParameterizedCurveVectorFunctionProvider(final ParameterizedCurve pCurve) {
    ArgumentChecker.notNull(pCurve, "pCurve");
    _pCurve = pCurve;
  }

  @Override
  public VectorFunction from(final double[] x) {
    return new ParameterizedCurveVectorFunction(x, _pCurve);
  }

}
