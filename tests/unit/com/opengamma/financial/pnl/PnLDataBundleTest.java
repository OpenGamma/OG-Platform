/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.pnl;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;

/**
 * @author emcleod
 *
 */
public class PnLDataBundleTest {
  private static final Map<Underlying, DoubleMatrix1D[]> U;
  private static final Map<Sensitivity, Matrix<?>> M;

  static {
    U = Collections.<Underlying, DoubleMatrix1D[]> singletonMap(Underlying.SPOT_PRICE, new DoubleMatrix1D[] { new DoubleMatrix1D(new double[] { 34 }) });
    M = Collections.<Sensitivity, Matrix<?>> singletonMap(Sensitivity.VALUE_DELTA, new DoubleMatrix2D(new double[][] { { 1 } }));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullArray() {
    new PnLDataBundle(null, M);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyArray() {
    new PnLDataBundle(Collections.<Underlying, DoubleMatrix1D[]> emptyMap(), M);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullMatrices() {
    new PnLDataBundle(U, null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyMatrices() {
    new PnLDataBundle(U, Collections.<Sensitivity, Matrix<?>> emptyMap());
  }
}
