/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import com.opengamma.analytics.math.function.Function;

/**
 * Given an array of curves, returns a function {@link Function} that will apply a spread operation to 
 * each of the curves.
 */
public interface CurveSpreadFunction extends Function<Curve<Double, Double>, Function<Double, Double>> {

  /**
   * The string representing the spread operation 
   * @return The operation name
   */
  String getOperationName();
}
