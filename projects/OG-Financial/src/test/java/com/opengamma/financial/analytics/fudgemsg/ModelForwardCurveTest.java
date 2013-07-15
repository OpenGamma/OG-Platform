/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurveAffineDividends;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.util.serialization.InvokedSerializedForm;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class ModelForwardCurveTest extends AnalyticsTestBase {

  private static final double[] EXPIRIES = new double[] {1, 2, 3, 4, 5};
  private static final double[] FORWARD = new double[] {100, 101, 102, 103, 104};
  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final double EPS = 1e-12;


  @Test
  public void testCurve1() {
    final double spot = 100;
    final ForwardCurve curve1 = new ForwardCurve(spot);
    final ForwardCurve curve2 = cycleObject(ForwardCurve.class, curve1);
    assertEquals(curve1.getSpot(), curve2.getSpot(), EPS);
    assertTrue(curve2.getForwardCurve() instanceof ConstantDoublesCurve);
    assertTrue(curve2.getDriftCurve() instanceof ConstantDoublesCurve);
    assertCurveEquals(curve1.getForwardCurve(), curve2.getForwardCurve());
    assertCurveEquals(curve1.getDriftCurve(), curve2.getDriftCurve());
  }

  @Test
  public void testCurve2() {
    final ForwardCurve curve1 = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARD, INTERPOLATOR));
    final ForwardCurve curve2 = cycleObject(ForwardCurve.class, curve1);
    assertEquals(curve1.getSpot(), curve2.getSpot(), EPS);
    assertTrue(curve2.getForwardCurve() instanceof InterpolatedDoublesCurve);
    assertTrue(curve2.getDriftCurve() instanceof FunctionalDoublesCurve);
    assertCurveEquals(curve1.getForwardCurve(), curve2.getForwardCurve());
    assertCurveEquals(curve1.getDriftCurve(), curve2.getDriftCurve());
  }

  @Test
  public void testCurve3() {
    final double spot = 100;
    final Curve<Double, Double> driftCurve = InterpolatedDoublesCurve.from(EXPIRIES, FORWARD, INTERPOLATOR);
    final ForwardCurve curve1 = new ForwardCurve(spot, driftCurve);
    final ForwardCurve curve2 = cycleObject(ForwardCurve.class, curve1);
    assertTrue(curve2.getForwardCurve() instanceof FunctionalDoublesCurve);
    assertTrue(curve2.getDriftCurve() instanceof InterpolatedDoublesCurve);
    assertCurveEquals(curve1.getForwardCurve(), curve2.getForwardCurve());
    assertCurveEquals(curve1.getDriftCurve(), curve2.getDriftCurve());
  }

  @Test
  public void testCurve4() {
    final double spot = 100;
    final double drift = 1.5;
    final ForwardCurve curve1 = new ForwardCurve(spot, drift);
    final ForwardCurve curve2 = cycleObject(ForwardCurve.class, curve1);
    assertTrue(curve2.getForwardCurve() instanceof FunctionalDoublesCurve);
    assertTrue(curve2.getDriftCurve() instanceof ConstantDoublesCurve);
    assertCurveEquals(curve1.getForwardCurve(), curve2.getForwardCurve());
    assertCurveEquals(curve1.getDriftCurve(), curve2.getDriftCurve());
  }

  @Test
  public void testCurve5() {
    final ForwardCurve curve1 = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARD, INTERPOLATOR), InterpolatedDoublesCurve.from(FORWARD, EXPIRIES, INTERPOLATOR));
    final ForwardCurve curve2 = cycleObject(ForwardCurve.class, curve1);
    assertEquals(curve1.getSpot(), curve2.getSpot(), EPS);
    assertTrue(curve2.getForwardCurve() instanceof InterpolatedDoublesCurve);
    assertTrue(curve2.getDriftCurve() instanceof InterpolatedDoublesCurve);
    assertCurveEquals(curve1.getForwardCurve(), curve2.getForwardCurve());
    assertCurveEquals(curve1.getDriftCurve(), curve2.getDriftCurve());
  }
  
  protected static Curve<Double, Double> getForwardCurve() {
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(final Double x) {
        return x * x;
      }
    }; 
    return new FunctionalDoublesCurve(f) {
      public Object writeReplace() {
        return new InvokedSerializedForm(ModelForwardCurveTest.class, "getForwardCurve");
      }
    };
  }
  
  @Test
  public void testCurve6() {
    final ForwardCurve curve1 = new ForwardCurve(getForwardCurve());
    final ForwardCurve curve2 = cycleObject(ForwardCurve.class, curve1);
    assertEquals(curve1.getSpot(), curve2.getSpot(), EPS);
    assertTrue(curve2.getForwardCurve() instanceof FunctionalDoublesCurve);
    assertTrue(curve2.getDriftCurve() instanceof FunctionalDoublesCurve);
    assertCurveEquals(curve1.getForwardCurve(), curve2.getForwardCurve());
    assertCurveEquals(curve1.getDriftCurve(), curve2.getDriftCurve());
  }
  @Test
  public void testForwardCurveAffineDividends() {
    final double spot = 100.0;
    final YieldAndDiscountCurve riskFreeCurve = YieldCurve.from(ConstantDoublesCurve.from(0.0));
    final double[] tau = new double[] {0.25, 0.5, 0.75, 1, 2, 3, 4};
    final double[] alpha = new double[] {0.23, 0.24, 0.25, 0.26, 0, 0, 0};
    final double[] beta = new double[] {0, 0, 0, 0, 0.15, 0.2, 0.3};
    final AffineDividends dividends = new AffineDividends(tau, alpha, beta);
    final ForwardCurveAffineDividends curve1 = new ForwardCurveAffineDividends(spot, riskFreeCurve, dividends);
    final ForwardCurveAffineDividends curve2 = cycleObject(ForwardCurveAffineDividends.class, curve1);
    assertEquals(curve1.getSpot(), curve2.getSpot(), EPS);
    assertTrue(curve2.getForwardCurve() instanceof FunctionalDoublesCurve);
    assertTrue(curve2.getDriftCurve() instanceof FunctionalDoublesCurve);
    assertCurveEquals(curve1.getForwardCurve(), curve2.getForwardCurve());
    assertCurveEquals(curve1.getDriftCurve(), curve2.getDriftCurve());
    assertTrue(curve1.equals(curve2));
    assertTrue(curve1.getRiskFreeCurve().equals(curve2.getRiskFreeCurve()));
    assertTrue(curve1.getDividends().equals(curve2.getDividends()));
    
  }

  private void assertCurveEquals(final Curve<Double, Double> c1, final Curve<Double, Double> c2) {
    if (c1 != c2) {
      for (double x = 0.1; x < 100.0; x += 5.00000001) {
        assertEquals(c1.getYValue(x), c2.getYValue(x));
      }
    }
  }
}
