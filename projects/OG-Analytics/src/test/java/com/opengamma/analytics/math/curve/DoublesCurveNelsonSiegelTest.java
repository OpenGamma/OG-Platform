/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;

import com.opengamma.analytics.math.differentiation.ScalarFirstOrderDifferentiator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class DoublesCurveNelsonSiegelTest {

  private static final String CURVE_NAME = "NS curve";

  private static final double BETA0 = 0.03;
  private static final double BETA1 = -0.02;
  private static final double BETA2 = 0.06;
  private static final double LAMBDA = 2.0;
  private static final double[] PARAMETERS = new double[] {BETA0, BETA1, BETA2, LAMBDA };

  private static final DoublesCurveNelsonSiegel CURVE_NS = new DoublesCurveNelsonSiegel(CURVE_NAME, BETA0, BETA1, BETA2, LAMBDA);

  private static final double TOLERANCE_YIELD = 1.0E-15;
  private static final double TOLERANCE_SENSITIVITY = 1.0E-5;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullParameters() {
    new DoublesCurveNelsonSiegel(CURVE_NAME, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void nullName() {
    new DoublesCurveNelsonSiegel(null, BETA0, BETA1, BETA2, LAMBDA);
  }

  @Test
  public void constructors() {
    assertEquals("DoublesCurveNelsonSiegel: two constructors", CURVE_NS, new DoublesCurveNelsonSiegel(CURVE_NAME, PARAMETERS));
  }

  @Test
  public void yValue() {
    final int nbPoint = 10;
    final double timeMax = 9.0;
    final double valueComputed0 = CURVE_NS.getYValue(0.0);
    final double valueExpected0 = BETA0 + BETA1;
    assertEquals("DoublesCurveNelsonSiegel: value", valueExpected0, valueComputed0, TOLERANCE_YIELD);
    for (int loopt = 1; loopt <= nbPoint; loopt++) {
      // Implementation note: start at 1 to avoid 0 (treated tested separately)
      final double t = loopt * timeMax / nbPoint;
      final double valueComputed = CURVE_NS.getYValue(t);
      final double tl = t / LAMBDA;
      final double exptl = Math.exp(-tl);
      final double valueExpected = BETA0 + BETA1 * (1 - exptl) / tl + BETA2 * ((1 - exptl) / tl - exptl);
      assertEquals("DoublesCurveNelsonSiegel: value", valueExpected, valueComputed, TOLERANCE_YIELD);
    }
  }

  @Test
  /**
   * Tests the parameters sensitivity with a finite difference comparison.
   */
  public void yValueParameterSensitivity() {
    final int nbPoint = 10;
    final double timeMax = 9.0;
    final double bump = 0.00001;
    final Double[] sensitivityComputed0 = CURVE_NS.getYValueParameterSensitivity(0.0);
    final double[] sensitivityExpected0 = new double[] {1.0, 1.0, 0.0, 0.0 };
    assertArrayEquals("DoublesCurveNelsonSiegel: parameter sensitivity", sensitivityExpected0, ArrayUtils.toPrimitive(sensitivityComputed0), TOLERANCE_SENSITIVITY);
    final double[][] parametersBumped = new double[4][];
    final DoublesCurveNelsonSiegel[] curveBumped = new DoublesCurveNelsonSiegel[4];
    for (int loopp = 0; loopp < 4; loopp++) {
      parametersBumped[loopp] = PARAMETERS.clone();
      parametersBumped[loopp][loopp] += bump;
      curveBumped[loopp] = new DoublesCurveNelsonSiegel(CURVE_NAME, parametersBumped[loopp]);
    }
    for (int loopt = 1; loopt <= nbPoint; loopt++) {
      // Implementation note: start at 1 to avoid 0 (treated tested separately)
      final double t = loopt * timeMax / nbPoint;
      final double valueComputed = CURVE_NS.getYValue(t);
      final Double[] sensitivityComputed = CURVE_NS.getYValueParameterSensitivity(t);
      final double[] sensitivityExpected = new double[4];
      for (int loopp = 0; loopp < 4; loopp++) {
        final double valueBumped = curveBumped[loopp].getYValue(t);
        sensitivityExpected[loopp] = (valueBumped - valueComputed) / bump;
      }
      assertArrayEquals("DoublesCurveNelsonSiegel: parameter sensitivity " + loopt, sensitivityExpected, ArrayUtils.toPrimitive(sensitivityComputed), TOLERANCE_SENSITIVITY);
    }
  }

  @Test(enabled = false)
  public void analysis() {
    final int nbPoint = 50;
    final double timeMax = 20.0;
    final double[] value = new double[nbPoint + 1];
    final double[] t = new double[nbPoint + 1];
    for (int loopt = 0; loopt <= nbPoint; loopt++) {
      t[loopt] = loopt * timeMax / nbPoint;
      value[loopt] = CURVE_NS.getYValue(t[loopt]);
    }
  }

  @Test
  public void testDerivative() {
    final Function1D<Double, Double> func = CURVE_NS.toFunction1D();
    final ScalarFirstOrderDifferentiator diff = new ScalarFirstOrderDifferentiator();
    final Function1D<Double, Double> grad = diff.differentiate(func);

    for (int i = 0; i < 50; i++) {
      final double t = 0 + 10.0 * i / 99.;
      final double fd = grad.evaluate(t);
      final double anal = CURVE_NS.getDyDx(t);
      assertEquals("t=" + t, fd, anal, 1e-12);
    }

  }

}
