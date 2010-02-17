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

import com.opengamma.financial.sensitivity.Sensitivity;

/**
 * @author emcleod
 * 
 */
public class ParametricVaRDataBundleTest {
  private final Map<Sensitivity, DoubleMatrix1D> VECTOR = Collections.<Sensitivity, DoubleMatrix1D> singletonMap(Sensitivity.VALUE_DELTA, DoubleFactory1D.dense
      .make(new double[] { 4 }));
  private final Map<Sensitivity, DoubleMatrix2D> MATRIX = Collections.<Sensitivity, DoubleMatrix2D> singletonMap(Sensitivity.VALUE_DELTA, DoubleFactory2D.dense
      .make(new double[][] { new double[] { 2 } }));
  private final ParametricVaRDataBundle DATA = new ParametricVaRDataBundle(VECTOR, MATRIX);

  @Test(expected = IllegalArgumentException.class)
  public void testNullSensitivities() {
    new ParametricVaRDataBundle(null, MATRIX);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullCovariances() {
    new ParametricVaRDataBundle(VECTOR, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadSensitivitySensitivity() {
    DATA.getSensitivityVector(Sensitivity.VALUE_VEGA);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testBadSensitivityCovariance() {
    DATA.getCovarianceMatrix(Sensitivity.VALUE_VEGA);
  }

  @Test
  public void test() {
    assertEquals(VECTOR.get(Sensitivity.VALUE_DELTA), DATA.getSensitivityVector(Sensitivity.VALUE_DELTA));
    assertEquals(MATRIX.get(Sensitivity.VALUE_DELTA), DATA.getCovarianceMatrix(Sensitivity.VALUE_DELTA));
  }
}
