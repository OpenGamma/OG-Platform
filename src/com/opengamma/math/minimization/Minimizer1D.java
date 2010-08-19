/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public interface Minimizer1D extends Minimizer<Function1D<Double, Double>, Double> {

  double minimize(Function1D<Double, Double> function, double startPosition, double lowerBound, double upperBound);

}
