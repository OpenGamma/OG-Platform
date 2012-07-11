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

public class DoublesCurveNelsonSiegelTest {

  private static final String CURVE_NAME = "NS curve";

  private static final double BETA0 = 0.03;
  private static final double BETA1 = -0.02;
  private static final double BETA2 = 0.06;
  private static final double LAMBDA = 2.0;
  private static final double[] PARAMETERS = new double[] {BETA0, BETA1, BETA2, LAMBDA};

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
    int nbPoint = 10;
    double timeMax = 9.0;
    double valueComputed0 = CURVE_NS.getYValue(0.0);
    double valueExpected0 = BETA0 + BETA1;
    assertEquals("DoublesCurveNelsonSiegel: value", valueExpected0, valueComputed0, TOLERANCE_YIELD);
    for (int loopt = 1; loopt <= nbPoint; loopt++) {
      // Implementation note: start at 1 to avoid 0 (treated tested separately)
      double t = loopt * timeMax / nbPoint;
      double valueComputed = CURVE_NS.getYValue(t);
      double tl = t / LAMBDA;
      double exptl = Math.exp(-tl);
      double valueExpected = BETA0 + BETA1 * (1 - exptl) / tl + BETA2 * ((1 - exptl) / tl - exptl);
      assertEquals("DoublesCurveNelsonSiegel: value", valueExpected, valueComputed, TOLERANCE_YIELD);
    }
  }

  @Test
  /**
   * Tests the parameters sensitivity with a finite difference comparison.
   */
  public void yValueParameterSensitivity() {
    int nbPoint = 10;
    double timeMax = 9.0;
    double bump = 0.00001;
    Double[] sensitivityComputed0 = CURVE_NS.getYValueParameterSensitivity(0.0);
    double[] sensitivityExpected0 = new double[] {1.0, 1.0, 0.0, 0.0};
    assertArrayEquals("DoublesCurveNelsonSiegel: parameter sensitivity", sensitivityExpected0, ArrayUtils.toPrimitive(sensitivityComputed0), TOLERANCE_SENSITIVITY);
    double[][] parametersBumped = new double[4][];
    DoublesCurveNelsonSiegel[] curveBumped = new DoublesCurveNelsonSiegel[4];
    for (int loopp = 0; loopp < 4; loopp++) {
      parametersBumped[loopp] = PARAMETERS.clone();
      parametersBumped[loopp][loopp] += bump;
      curveBumped[loopp] = new DoublesCurveNelsonSiegel(CURVE_NAME, parametersBumped[loopp]);
    }
    for (int loopt = 1; loopt <= nbPoint; loopt++) {
      // Implementation note: start at 1 to avoid 0 (treated tested separately)
      double t = loopt * timeMax / nbPoint;
      double valueComputed = CURVE_NS.getYValue(t);
      Double[] sensitivityComputed = CURVE_NS.getYValueParameterSensitivity(t);
      double[] sensitivityExpected = new double[4];
      for (int loopp = 0; loopp < 4; loopp++) {
        double valueBumped = curveBumped[loopp].getYValue(t);
        sensitivityExpected[loopp] = (valueBumped - valueComputed) / bump;
      }
      assertArrayEquals("DoublesCurveNelsonSiegel: parameter sensitivity " + loopt, sensitivityExpected, ArrayUtils.toPrimitive(sensitivityComputed), TOLERANCE_SENSITIVITY);
    }
  }

  @Test(enabled = false)
  public void analysis() {
    int nbPoint = 50;
    double timeMax = 20.0;
    double[] value = new double[nbPoint + 1];
    double[] t = new double[nbPoint + 1];
    for (int loopt = 0; loopt <= nbPoint; loopt++) {
      t[loopt] = loopt * timeMax / nbPoint;
      value[loopt] = CURVE_NS.getYValue(t[loopt]);
    }
    int test = 0;
    test++;
  }

}
