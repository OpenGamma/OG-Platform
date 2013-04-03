/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.curve;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.analytics.math.curve.NodalObjectsCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.language.error.InvokeInvalidArgumentException;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link GetCurveYValuesFunction} class.
 */
@Test(groups = TestGroup.UNIT)
public class GetCurveYValuesFunctionTest {

  public void testDoublesCurve() {
    final Curve<Double, Double> curve = new FunctionalDoublesCurve(new Function<Double, Double>() {
      @Override
      public Double evaluate(Double... x) {
        return x[0] + 3d;
      }
    });
    final Double[] result = GetCurveYValuesFunction.invoke(curve, new Double[] {1d, 2d, 3d });
    assertEquals(result.length, 3);
    assertEquals(result[0], 4d);
    assertEquals(result[1], 5d);
    assertEquals(result[2], 6d);
  }

  @SuppressWarnings("unchecked")
  public void testNodalObjectCurve() {
    final Curve<String, String> curve = new NodalObjectsCurve<String, String>(Arrays.<String>asList("A", "B", "C"), Arrays.<String>asList("Foo", "Bar", "Cow"), true);
    try {
      GetCurveYValuesFunction.invoke((Curve<Double, Double>) (Curve<?, ?>) curve, new Double[] {1d, 2d, 3d });
      fail("expected exception");
    } catch (InvokeInvalidArgumentException e) {
      assertEquals(e.getParameterIndex(), (Integer) 0);
    }
  }

  public void testNodalDoublesCurve() {
    final Curve<Double, Double> curve = new NodalDoublesCurve(new double[] {1d, 2d, 3d }, new double[] {4d, 5d, 6d }, true);
    final Double[] result = GetCurveYValuesFunction.invoke(curve, new Double[] {1d, 2d, 3d });
    assertEquals(result.length, 3);
    assertEquals(result[0], 4d);
    assertEquals(result[1], 5d);
    assertEquals(result[2], 6d);
    try {
      GetCurveYValuesFunction.invoke(curve, new Double[] {4d });
      fail("expected exception");
    } catch (InvokeInvalidArgumentException e) {
      assertEquals(e.getParameterIndex(), (Integer) 1);
    }
  }

}
