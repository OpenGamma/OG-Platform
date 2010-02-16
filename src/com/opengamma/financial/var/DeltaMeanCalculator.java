/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.linalg.Algebra;

import com.opengamma.financial.greeks.value.ValueGreek;
import com.opengamma.math.function.Function1D;

/**
 * @author emcleod
 * 
 */
public class DeltaMeanCalculator extends Function1D<ParametricWithMeanVaRDataBundle, Double> {
  private final Algebra _algebra = new Algebra();

  /*
   * (non-Javadoc)
   * 
   * @see com.opengamma.math.function.Function1D#evaluate(java.lang.Object)
   */
  @Override
  public Double evaluate(final ParametricWithMeanVaRDataBundle data) {
    if (data == null)
      throw new IllegalArgumentException("Data were null");
    final DoubleMatrix1D delta = data.getValueGreekVector(ValueGreek.VALUE_DELTA);
    final int s1 = delta.size();
    if (s1 == 0)
      throw new IllegalArgumentException("Value delta vector contained no data");
    final DoubleMatrix1D mean = data.getMean(ValueGreek.VALUE_DELTA);
    final int s2 = mean.size();
    if (s2 == 0)
      throw new IllegalArgumentException("Mean vector contained no data");
    if (s1 != s2)
      throw new IllegalArgumentException("Value delta and mean vectors were of different size");
    return _algebra.mult(data.getValueGreekVector(ValueGreek.VALUE_DELTA), data.getMean(ValueGreek.VALUE_DELTA));
  }

}
