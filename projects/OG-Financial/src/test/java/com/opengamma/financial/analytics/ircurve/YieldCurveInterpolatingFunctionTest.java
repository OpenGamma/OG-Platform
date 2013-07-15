/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;

import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.analytics.math.interpolation.DoubleQuadraticInterpolator1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class YieldCurveInterpolatingFunctionTest {

  @Test
  public void simpleTest() {
    ArrayList<Double> xs = Lists.newArrayList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0);
    ArrayList<Double> ys = Lists.newArrayList(1.0, 2.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
    DoubleQuadraticInterpolator1D interpolator = new DoubleQuadraticInterpolator1D();

    InterpolatedDoublesCurve inputCurve = InterpolatedDoublesCurve.from(xs, ys, interpolator);

    NodalDoublesCurve interpolatedCurve = YieldCurveInterpolatingFunction.interpolateCurve(inputCurve);

    AssertJUnit.assertNotSame(0, interpolatedCurve.getXData().length);
    AssertJUnit.assertNotSame(0, interpolatedCurve.getYData().length);
    AssertJUnit.assertEquals(xs.get(0), interpolatedCurve.getXData()[0]);
    AssertJUnit.assertEquals(xs.get(xs.size() - 1),
        interpolatedCurve.getXData()[interpolatedCurve.getXData().length - 1]);

    for (int i = 0; i < interpolatedCurve.getXData().length; i++) {
      double x = interpolatedCurve.getXData()[i];
      double y = interpolatedCurve.getYData()[i];
      AssertJUnit.assertEquals(inputCurve.getYValue(x), y);
    }
    
    for (int i = 1; i < interpolatedCurve.getXData().length; i++) {
      double x = interpolatedCurve.getXData()[i];
      double prevX = interpolatedCurve.getXData()[i-1];
      assertTrue(prevX < x);
    }
  }

}
