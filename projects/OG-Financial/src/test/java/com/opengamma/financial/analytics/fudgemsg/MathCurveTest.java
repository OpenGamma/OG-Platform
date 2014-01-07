/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.NelsonSiegelBondCurveModel;
import com.opengamma.analytics.financial.interestrate.NelsonSiegelSvennsonBondCurveModel;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.NodalTenorDoubleCurve;
import com.opengamma.analytics.math.function.Function;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.LinearInterpolator1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
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
  public void testFunctionalCurve_Unserializable() {
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double x) {
        return x * x;
      }

    };
    cycleObject(Curve.class, FunctionalDoublesCurve.from(f));
  }

  private void assertCurveEquals(final Curve<Double, Double> c1, final Curve<Double, Double> c2) {
    if (c1 != c2) {
      assertEquals(c1.getName(), c2.getName());
      for (double x = 0.1d; x < 100.0d; x += 5.00000001d) {
        assertEquals(c1.getYValue(x), c2.getYValue(x));
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFunctionalCurve_NSS() {
    final NelsonSiegelSvennsonBondCurveModel curveBondModel = new NelsonSiegelSvennsonBondCurveModel();
    final Function1D<Double, Double> f = curveBondModel.getParameterizedFunction().asFunctionOfArguments(new DoubleMatrix1D(new double[] {1, 2, 3, 4, 5, 6 }));
    Curve<Double, Double> c1 = FunctionalDoublesCurve.from(f);
    Curve<Double, Double> c2 = cycleObject(Curve.class, c1);
    assertCurveEquals(c1, c2);
    c1 = FunctionalDoublesCurve.from(f, "NAME");
    c2 = cycleObject(Curve.class, c1);
    assertCurveEquals(c1, c2);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFunctionalCurve_NS() {
    final NelsonSiegelBondCurveModel curveBondModel = new NelsonSiegelBondCurveModel();
    final Function1D<Double, Double> func = curveBondModel.getParameterizedFunction().asFunctionOfArguments(new DoubleMatrix1D(new double[] {1, 2, 3, 4 }));
    Curve<Double, Double> c1 = FunctionalDoublesCurve.from(func);
    Curve<Double, Double> c2 = cycleObject(Curve.class, c1);
    assertCurveEquals(c1, c2);
    c1 = FunctionalDoublesCurve.from(func, "NAME");
    c2 = cycleObject(Curve.class, c1);
    assertCurveEquals(c1, c2);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testNodalDoubleCurve() {
    NodalTenorDoubleCurve c1 = NodalTenorDoubleCurve.from(new Tenor[] { Tenor.ONE_DAY, Tenor.ONE_YEAR }, new Double[] { 1.2345, 67.89 });
    NodalTenorDoubleCurve c2 = cycleObject(NodalTenorDoubleCurve.class, c1);
    assertTrue(c1.equals(c2));
  }
}
