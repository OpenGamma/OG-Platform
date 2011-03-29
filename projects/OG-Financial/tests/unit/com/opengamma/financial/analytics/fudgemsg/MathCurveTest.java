/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.interestrate.NelsonSiegelSvennsonBondCurveModel;
import com.opengamma.math.curve.ConstantDoublesCurve;
import com.opengamma.math.curve.Curve;
import com.opengamma.math.curve.FunctionalDoublesCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.LinearInterpolator1D;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class MathCurveTest extends AnalyticsTestBase {

  @SuppressWarnings("unchecked")
  @Test
  public void testConstantCurve() {
    Curve<Double, Double> c1 = ConstantDoublesCurve.from(4.);
    Curve<Double, Double> c2 = cycleObject(Curve.class, c1);
    assertEquals(c1, c2);
    c1 = ConstantDoublesCurve.from(4., "NAME");
    c2 = cycleObject(Curve.class, c1);
    assertEquals(c1, c2);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testInterpolatedCurve() {
    Curve<Double, Double> c1 = InterpolatedDoublesCurve.from(new double[] {1, 2, 3, 4}, new double[] {4, 5, 6, 7}, new LinearInterpolator1D());
    Curve<Double, Double> c2 = cycleObject(Curve.class, c1);
    assertEquals(c1, c2);
    c1 = InterpolatedDoublesCurve.from(new double[] {1, 2, 3, 4}, new double[] {4, 5, 6, 7}, new LinearInterpolator1D(), "NAME");
    c2 = cycleObject(Curve.class, c1);
    assertEquals(c1, c2);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testWrongFunctionType() {
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return x * x;
      }

    };
    cycleObject(Curve.class, FunctionalDoublesCurve.from(f));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFunctionalCurve() {
    final Function1D<Double, Double> f = new NelsonSiegelSvennsonBondCurveModel(new DoubleMatrix1D(new double[] {1, 2, 3, 4, 5, 6}));
    Curve<Double, Double> c1 = FunctionalDoublesCurve.from(f);
    Curve<Double, Double> c2 = cycleObject(Curve.class, c1);
    assertEquals(c1, c2);
    c1 = FunctionalDoublesCurve.from(f, "NAME");
    c2 = cycleObject(Curve.class, c1);
    assertEquals(c1, c2);
  }
}
