/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public interface ScalarMinimizerWithGradient extends MinimizerWithGradient<Function1D<Double, Double>, Function1D<Double, Double>, Double> {
  double minimize(Function1D<Double, Double> f, Function1D<Double, Double> fPrime, double startPosition, double lowerBound, double upperBound);
}
