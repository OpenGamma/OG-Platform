/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.pnl;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.opengamma.financial.greeks.Greek;
import com.opengamma.financial.sensitivity.Sensitivity;
import com.opengamma.financial.sensitivity.ValueGreek;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.ColtMatrixAlgebra;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.matrix.Matrix;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 *
 */
public class SensitivityPnLCalculatorTest {
  private static final MatrixAlgebra ALGEBRA = new ColtMatrixAlgebra();
  private static final Function1D<PnLDataBundle, Double[]> CALCULATOR = new SensitivityPnLCalculator(ALGEBRA);

  @Test(expected = IllegalArgumentException.class)
  public void testNull() {
    new SensitivityPnLCalculator(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullData() {
    CALCULATOR.evaluate((PnLDataBundle) null);
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testUnsupportedData() {
    final Map<Underlying, DoubleMatrix1D[]> u = Collections.<Underlying, DoubleMatrix1D[]> singletonMap(Underlying.SPOT_PRICE, new DoubleMatrix1D[] { new DoubleMatrix1D(
        new double[] { 3.4 }) });
    final Map<Sensitivity<?>, Matrix<?>> m = Collections.<Sensitivity<?>, Matrix<?>> singletonMap(new ValueGreek(Greek.SPEED), new DoubleMatrix2D(new double[][] { { 2 } }));
    CALCULATOR.evaluate(new PnLDataBundle(u, m));
  }

  @Test
  public void test() {
    final int n = 100;
    final DoubleMatrix1D[] spotChange = new DoubleMatrix1D[n];
    final DoubleMatrix1D[] volChange = new DoubleMatrix1D[n];
    for (int i = 0; i < n; i++) {
      spotChange[i] = new DoubleMatrix1D(new double[] { 0.01 });
      volChange[i] = new DoubleMatrix1D(new double[] { 0.1 });
    }
    final Map<Underlying, DoubleMatrix1D[]> u = new HashMap<Underlying, DoubleMatrix1D[]>();
    u.put(Underlying.SPOT_PRICE, spotChange);
    u.put(Underlying.IMPLIED_VOLATILITY, volChange);
    final Map<Sensitivity<?>, Matrix<?>> m = new HashMap<Sensitivity<?>, Matrix<?>>();
    m.put(new ValueGreek(Greek.DELTA), new DoubleMatrix1D(new double[] { 1000 }));
    m.put(new ValueGreek(Greek.GAMMA), new DoubleMatrix2D(new double[][] { { -20000 } }));
    m.put(new ValueGreek(Greek.VEGA), new DoubleMatrix1D(new double[] { 300 }));
    m.put(new ValueGreek(Greek.VANNA), new DoubleMatrix2D(new double[][] { { 4000 } }));
    final PnLDataBundle data = new PnLDataBundle(u, m);
    final Double[] pnl = CALCULATOR.evaluate(data);
    for (int i = 0; i < n; i++) {
      assertEquals(pnl[i], 43, 1e-12);
    }
  }
}
