/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public interface VectorMinimizer extends Minimizer<Function1D<DoubleMatrix1D, Double>, DoubleMatrix1D> {
}
