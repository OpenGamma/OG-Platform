/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding.newton;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public interface NewtonRootFinderMatrixInitializationFunction {

  DoubleMatrix2D getInitializedMatrix(Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianFunction, DoubleMatrix1D x);
}
