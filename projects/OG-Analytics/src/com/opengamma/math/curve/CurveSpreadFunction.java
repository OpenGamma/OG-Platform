/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import com.opengamma.math.function.Function;

/**
 * Given an array of curves, returns a function {@link Function} that will apply the spread operation to 
 * each of the curves for the input value.
 */
public interface CurveSpreadFunction extends Function<Curve<Double, Double>, Function<Double, Double>> {

  /**
   * The string representing the spread operation 
   * @return The operation name
   */
  String getOperationName();
}
