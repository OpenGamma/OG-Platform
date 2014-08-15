/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test simple a simple function a * Math.sinh(b * x)
 */
@Test(groups = TestGroup.UNIT)
public class ParameterizedCurveVectorFunctionTest {

  private static final ParameterizedCurve s_PCurve;

  static {
    s_PCurve = new ParameterizedCurve() {

      @Override
      public Double evaluate(final Double x, final DoubleMatrix1D parameters) {
        final double a = parameters.getEntry(0);
        final double b = parameters.getEntry(1);
        return a * Math.sinh(b * x);
      }

      @Override
      public int getNumParameters() {
        return 2;
      }
    };
  }

  @Test
  public void test() {
    final ParameterizedCurveVectorFunctionProvider pro = new ParameterizedCurveVectorFunctionProvider(s_PCurve);
    final VectorFunction f = pro.from(new double[] {-1.0, 0.0, 1.0 });
    final DoubleMatrix1D y = f.evaluate(new DoubleMatrix1D(0.5, 2.0));
    assertEquals(0.5 * Math.sinh(-2.0), y.getEntry(0), 1e-14);
    assertEquals(0.0, y.getEntry(1), 1e-14);
    assertEquals(0.5 * Math.sinh(2.0), y.getEntry(2), 1e-14);
  }

}
