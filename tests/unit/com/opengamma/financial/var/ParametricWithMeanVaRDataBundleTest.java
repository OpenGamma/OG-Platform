/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.var;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

import com.opengamma.financial.greeks.value.ValueGreek;

/**
 * @author emcleod
 * 
 */
public class ParametricWithMeanVaRDataBundleTest {
  private final Map<ValueGreek, DoubleMatrix1D> MEAN = Collections
      .<ValueGreek, DoubleMatrix1D> singletonMap(ValueGreek.VALUE_DELTA, DoubleFactory1D.dense.make(new double[] { 2 }));
  private final Map<ValueGreek, DoubleMatrix1D> VECTOR = Collections.<ValueGreek, DoubleMatrix1D> singletonMap(ValueGreek.VALUE_DELTA, DoubleFactory1D.dense
      .make(new double[] { 4 }));
  private final Map<ValueGreek, DoubleMatrix2D> MATRIX = Collections.<ValueGreek, DoubleMatrix2D> singletonMap(ValueGreek.VALUE_DELTA, DoubleFactory2D.dense
      .make(new double[][] { new double[] { 2 } }));
  private final ParametricWithMeanVaRDataBundle DATA = new ParametricWithMeanVaRDataBundle(MEAN, VECTOR, MATRIX);

  @Test(expected = IllegalArgumentException.class)
  public void testNullMeans() {
    new ParametricWithMeanVaRDataBundle(null, VECTOR, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadValueGreekMean() {
    DATA.getMean(ValueGreek.VALUE_VEGA);
  }

  @Test
  public void test() {
    assertEquals(MEAN.get(ValueGreek.VALUE_DELTA), DATA.getMean(ValueGreek.VALUE_DELTA));
  }
}
