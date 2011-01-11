/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public interface ScalarMinimizer extends Minimizer<Function1D<Double, Double>, Double> {

  double minimize(Function1D<Double, Double> function, double startPosition, double lowerBound, double upperBound);

}
