/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.integration.RungeKuttaIntegrator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ForwardRateTest {

  private static final RungeKuttaIntegrator1D INTEGRATOR = new RungeKuttaIntegrator1D();
  private static final Interpolator1D INTERPOLATOR = Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE;
  private static final double[] T = new double[] {0.0, 0.05, 0.1, 0.2, 0.5, 1.0, 2, 3, 5, 7, 10 };
  private static final double[] R = new double[] {0.01, 0.01, 0.012, 0.014, 0.02, 0.03, 0.03, 0.029, 0.024, 0.023, 0.02 };
  private static final double[] P;
  private static final YieldAndDiscountCurve YIELD_CURVE;
  private static final YieldAndDiscountCurve DISCOUNT_CURVE;

  static {
    final int n = T.length;
    P = new double[n];
    for (int i = 0; i < n; i++) {
      P[i] = Math.exp(-T[i] * R[i]);
    }

    final InterpolatedDoublesCurve ratesCurve = InterpolatedDoublesCurve.from(T, R, INTERPOLATOR);
    final InterpolatedDoublesCurve disCurve = InterpolatedDoublesCurve.from(T, P, INTERPOLATOR);
    YIELD_CURVE = new YieldCurve("yield curve", ratesCurve);
    DISCOUNT_CURVE = new DiscountCurve("discount curve", disCurve);

  }

  @Test
  public void test() {
    final int n = T.length;
    //    final double range = T[n - 1] - T[0];
    //    for (int i = 0; i < 300; i++) {
    //      final double t = T[0] + (range) * i / (299.);
    //      final double r = YIELD_CURVE.getInterestRate(t);
    //      final double f = YIELD_CURVE.getForwardRate(t);
    //      System.out.println(t + "\t" + r + "\t" + f);
    //    }

    final Function1D<Double, Double> fwd1 = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double t) {
        return YIELD_CURVE.getForwardRate(t);
      }
    };

    final Function1D<Double, Double> fwd2 = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double t) {
        return DISCOUNT_CURVE.getForwardRate(t);
      }
    };

    for (int i = 0; i < n; i++) {
      final double df1 = Math.exp(-INTEGRATOR.integrate(fwd1, 0.0, T[i]));
      final double df2 = Math.exp(-INTEGRATOR.integrate(fwd2, 0.0, T[i]));
      assertEquals(P[i], df1, 1e-9);
      assertEquals(P[i], df2, 1e-9);
    }

  }
}
