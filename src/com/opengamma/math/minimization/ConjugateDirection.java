/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class ConjugateDirection implements MinimizerND {

  @Override
  public DoubleMatrix1D minimize(Function1D<DoubleMatrix1D, Double> function, DoubleMatrix1D startPosition) {
    return null;
  }

}
