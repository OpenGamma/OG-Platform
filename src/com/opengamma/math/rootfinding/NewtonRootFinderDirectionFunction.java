/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public interface NewtonRootFinderDirectionFunction {

  DoubleMatrix1D getDirection(Function1D<DoubleMatrix1D, DoubleMatrix1D> f, DoubleMatrix2D estimate, DoubleMatrix1D y);
}
