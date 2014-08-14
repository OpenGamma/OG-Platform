/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function;

import com.opengamma.util.ArgumentChecker;

/**
 * A provider of a {@link ParameterizedCurveVectorFunction}
 */
public class ParameterizedCurveVectorFunctionProvider extends DoublesVectorFunctionProvider {

  private final ParameterizedCurve _pCurve;

  public ParameterizedCurveVectorFunctionProvider(final ParameterizedCurve pCurve) {
    ArgumentChecker.notNull(pCurve, "pCurve");
    _pCurve = pCurve;
  }

  /**
   * produces a {@link VectorFunction} which builds a {@link ParameterizedCurve} from the input vector (treated 
   * as curve parameters), then samples the curve at the smaplePoints, to produce the output vector. 
   * @param samplePoints points where we sample the curve 
   * @return a {@link ParameterizedCurveVectorFunction}
   */
  @Override
  public VectorFunction from(final double[] samplePoints) {
    return new ParameterizedCurveVectorFunction(samplePoints, _pCurve);
  }

}
