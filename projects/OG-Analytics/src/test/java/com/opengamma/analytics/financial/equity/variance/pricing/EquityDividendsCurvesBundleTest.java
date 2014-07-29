/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class EquityDividendsCurvesBundleTest {

  private static final double SPOT = 100;
  private static final double[] TAU = new double[] {0.1, 0.6, 1.1, 1.6, 2.1, 2.6};
  private static final double[] ALPHA = new double[] {5, 4, 3, 2, 1, 0};
  private static final double[] BETA = new double[] {0, 0.01, 0.02, 0.03, 0.04, 0.05};
  private static final double MU = 0.1;
  private static final YieldAndDiscountCurve DISCOUNT_CURVE = YieldCurve.from(ConstantDoublesCurve.from(MU));
  private static final AffineDividends DIVIDENDS = new AffineDividends(TAU, ALPHA, BETA);
  private static final EquityDividendsCurvesBundle DIV_CURVES = new EquityDividendsCurvesBundle(SPOT, DISCOUNT_CURVE, DIVIDENDS);

  @Test(enabled = false)
  public void printCurves() {
    for (int i = 0; i < 101; i++) {
      double t = 3.0 * i / 100.;
      System.out.println(t + "\t" + DIV_CURVES.getF(t) + "\t" + DIV_CURVES.getR(t) + "\t" + DIV_CURVES.getD(t));
    }
  }

  @Test
  public void fowardDropTest() {
    final double eps = 1e-15;
    final int n = TAU.length;
    for (int i = 0; i < n; i++) {
      double fm = DIV_CURVES.getF(TAU[i] - eps);
      double f = DIV_CURVES.getF(TAU[i]);
      double fExpected = fm * (1 - BETA[i]) - ALPHA[i];
      assertEquals(fExpected, f, 1e-12);
    }
  }

  @Test
  public void discountDividendDropTest() {
    final double eps = 1e-15;
    final int n = TAU.length;
    for (int i = 0; i < n; i++) {
      double dm = DIV_CURVES.getD(TAU[i] - eps);
      double d = DIV_CURVES.getD(TAU[i]);
      double dExpected = dm * (1 - BETA[i]) - ALPHA[i];
      assertEquals(dExpected, d, 1e-12);
    }
  }

  @Test
  public void growthFactorDropTest() {
    final double eps = 1e-15;
    final int n = TAU.length;
    for (int i = 0; i < n; i++) {
      double rm = DIV_CURVES.getR(TAU[i] - eps);
      double r = DIV_CURVES.getR(TAU[i]);
      double rExpected = rm * (1 - BETA[i]);
      assertEquals(rExpected, r, 1e-12);
    }
  }

}
