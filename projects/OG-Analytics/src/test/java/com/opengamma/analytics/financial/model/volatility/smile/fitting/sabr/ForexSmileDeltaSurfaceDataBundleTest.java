/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.sabr;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaParameters;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolator;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ForexSmileDeltaSurfaceDataBundleTest {
  private static final double[] FORWARDS = new double[] {1.34, 1.35, 1.36, 1.38, 1.4, 1.43, 1.45, 1.48, 1.5, 1.52 };
  private static final double[] EXPIRIES = new double[] {7. / 365, 14 / 365., 21 / 365., 1 / 12., 3 / 12., 0.5, 0.75, 1, 5, 10 };
  private static final double[] DELTAS = new double[] {0.15, 0.25 };
  private static final double[] ATM = new double[] {0.17045, 0.1688, 0.167425, 0.1697, 0.1641, 0.1642, 0.1641, 0.1642, 0.138, 0.12515 };
  private static final double[][] RR = new double[][] { {-0.0168, -0.02935, -0.039125, -0.047325, -0.058325, -0.06055, -0.0621, -0.063, -0.032775, -0.023925 },
      {-0.012025, -0.02015, -0.026, -0.0314, -0.0377, -0.03905, -0.0396, -0.0402, -0.02085, -0.015175 } };
  private static final double[][] STRANGLE = new double[][] { {0.00665, 0.00725, 0.00835, 0.009075, 0.013175, 0.01505, 0.01565, 0.0163, 0.009275, 0.007075, },
      {0.002725, 0.00335, 0.0038, 0.004, 0.0056, 0.0061, 0.00615, 0.00635, 0.00385, 0.002575 } };
  private static final boolean IS_CALL_DATA = true;
  private static final double[][] STRIKES;
  private static final double[][] VOLS;
  private static final CombinedInterpolatorExtrapolator INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
      Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  private static final ForwardCurve FORWARD_CURVE = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARDS, INTERPOLATOR));
  private static final ForexSmileDeltaSurfaceDataBundle DATA = new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, DELTAS, ATM, RR, STRANGLE, IS_CALL_DATA);
  private static final double EPS = 1e-15;

  static {
    STRIKES = DATA.getStrikes();
    VOLS = DATA.getVolatilities();
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullForwards() {
    new ForexSmileDeltaSurfaceDataBundle(null, EXPIRIES, DELTAS, ATM, RR, STRANGLE, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiries1() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARDS, null, DELTAS, ATM, RR, STRANGLE, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDeltas1() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARDS, EXPIRIES, null, ATM, RR, STRANGLE, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullATM1() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARDS, EXPIRIES, DELTAS, null, RR, STRANGLE, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRR1() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARDS, EXPIRIES, DELTAS, ATM, null, STRANGLE, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStrangle1() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARDS, EXPIRIES, DELTAS, ATM, RR, null, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARDS, EXPIRIES, DELTAS, ATM, RR, STRANGLE, IS_CALL_DATA, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullForwardCurve() {
    new ForexSmileDeltaSurfaceDataBundle(null, EXPIRIES, DELTAS, ATM, RR, STRANGLE, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiries2() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, null, DELTAS, ATM, RR, STRANGLE, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullDeltas2() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, null, ATM, RR, STRANGLE, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullATM2() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, DELTAS, null, RR, STRANGLE, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRR2() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, DELTAS, ATM, null, STRANGLE, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStrangle2() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, DELTAS, ATM, RR, null, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testForwardLength() {
    new ForexSmileDeltaSurfaceDataBundle(new double[] {2 }, EXPIRIES, DELTAS, ATM, RR, STRANGLE, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testATMLength1() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARDS, EXPIRIES, DELTAS, new double[] {2 }, RR, STRANGLE, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDeltaLength1() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARDS, EXPIRIES, new double[0], ATM, RR, STRANGLE, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRRLength1() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARDS, EXPIRIES, DELTAS, ATM, new double[][] {ATM }, STRANGLE, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStrangleLength1() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARDS, EXPIRIES, DELTAS, ATM, RR, new double[][] {ATM }, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRR1() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARDS, EXPIRIES, DELTAS, ATM, new double[][] {ATM, ATM, new double[] {1, 2 } }, STRANGLE, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStrangle1() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARDS, EXPIRIES, DELTAS, ATM, RR, new double[][] {ATM, ATM, new double[] {1, 2 } }, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testATMLength2() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, DELTAS, new double[] {2 }, RR, STRANGLE, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDeltaLength2() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, new double[0], ATM, RR, STRANGLE, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRRLength2() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, DELTAS, ATM, new double[][] {ATM }, STRANGLE, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStrangleLength2() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, DELTAS, ATM, RR, new double[][] {ATM }, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRR2() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, DELTAS, ATM, new double[][] {ATM, ATM, new double[] {1, 2 } }, STRANGLE, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStrangle2() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, DELTAS, ATM, RR, new double[][] {ATM, ATM, new double[] {1, 2 } }, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullForwardCurve3() {
    new ForexSmileDeltaSurfaceDataBundle(null, EXPIRIES, STRIKES, VOLS, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiries3() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, null, STRIKES, VOLS, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStrikes() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, null, VOLS, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVols() {
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, null, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthStrikes() {
    final double[][] strikes = new double[][] {STRIKES[0] };
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, strikes, VOLS, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthVols1() {
    final double[][] vols = new double[][] {VOLS[0] };
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, vols, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthVols2() {
    final double[][] vols = new double[VOLS.length][VOLS[0].length - 1];
    for (int i = 0; i < vols.length; i++) {
      for (int j = 0; j < vols[i].length - 1; j++) {
        vols[i][j] = VOLS[i][j];
      }
    }
    new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, vols, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowExpiryIndexForBump() {
    DATA.withBumpedPoint(-1, 1, 0.01);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighExpiryIndexForBump() {
    DATA.withBumpedPoint(100, 1, 0.01);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testLowStrikeIndexForBump() {
    DATA.withBumpedPoint(1, -1, 0.01);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testHighStrikeIndexForBump() {
    DATA.withBumpedPoint(1, 100, 0.01);
  }

  @Test
  public void testObject() {
    assertArrayEquals(FORWARDS, DATA.getForwards(), EPS);
    assertArrayEquals(EXPIRIES, DATA.getExpiries(), 0);
    final int n = DELTAS.length;
    for (int i = 0; i < EXPIRIES.length; i++) {
      final double[] rr = new double[n];
      final double[] s = new double[n];
      for (int j = 0; j < n; j++) {
        rr[j] = RR[j][i];
        s[j] = STRANGLE[j][i];
      }
      final SmileDeltaParameters cal = new SmileDeltaParameters(EXPIRIES[i], ATM[i], DELTAS, rr, s);
      assertArrayEquals(cal.getStrike(FORWARD_CURVE.getForward(EXPIRIES[i])), DATA.getStrikes()[i], EPS);
      assertArrayEquals(cal.getVolatility(), DATA.getVolatilities()[i], EPS);
    }
    assertEquals(FORWARD_CURVE, DATA.getForwardCurve());
    //   assertEquals(IS_CALL_DATA, DATA.isCallData());
    ForexSmileDeltaSurfaceDataBundle other = new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, DELTAS, ATM, RR, STRANGLE, IS_CALL_DATA);
    assertEquals(DATA, other);
    assertEquals(DATA.hashCode(), other.hashCode());
    other = new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, VOLS, IS_CALL_DATA);
    assertEquals(DATA, other);
    assertEquals(DATA.hashCode(), other.hashCode());
    other = new ForexSmileDeltaSurfaceDataBundle(FORWARDS, EXPIRIES, DELTAS, ATM, RR, STRANGLE, IS_CALL_DATA, INTERPOLATOR);
    assertArrayEquals(DATA.getExpiries(), other.getExpiries(), 0);
    for (int i = 0; i < EXPIRIES.length; i++) {
      assertArrayEquals(DATA.getStrikes()[i], other.getStrikes()[i], 0);
      assertArrayEquals(DATA.getVolatilities()[i], other.getVolatilities()[i], 0);
    }
    //  assertEquals(DATA.isCallData(), other.isCallData());
    assertArrayEquals(ArrayUtils.toPrimitive(DATA.getForwardCurve().getForwardCurve().getXData()),
        ArrayUtils.toPrimitive(other.getForwardCurve().getForwardCurve().getXData()), 0);
    assertArrayEquals(ArrayUtils.toPrimitive(DATA.getForwardCurve().getForwardCurve().getYData()),
        ArrayUtils.toPrimitive(other.getForwardCurve().getForwardCurve().getYData()), 0);
    assertEquals(((InterpolatedDoublesCurve) DATA.getForwardCurve().getForwardCurve()).getInterpolator(),
        ((InterpolatedDoublesCurve) other.getForwardCurve().getForwardCurve()).getInterpolator());
    final ForwardCurve otherCurve = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, EXPIRIES, INTERPOLATOR));
    other = new ForexSmileDeltaSurfaceDataBundle(otherCurve, EXPIRIES, DELTAS, ATM, RR, STRANGLE, IS_CALL_DATA);
    assertFalse(DATA.equals(other));
    other = new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, new double[] {7. / 365, 14 / 365., 21 / 365., 1 / 12., 3 / 12., 0.5, 0.75, 1, 5, 9 }, DELTAS, ATM, RR, STRANGLE, IS_CALL_DATA);
    assertFalse(DATA.equals(other));
    other = new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, new double[] {0.15, 0.35 }, ATM, RR, STRANGLE, IS_CALL_DATA);
    assertFalse(DATA.equals(other));
    other = new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, DELTAS, FORWARDS, RR, STRANGLE, IS_CALL_DATA);
    assertFalse(DATA.equals(other));
    other = new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, DELTAS, ATM, STRANGLE, STRANGLE, IS_CALL_DATA);
    assertFalse(DATA.equals(other));
    other = new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, DELTAS, ATM, RR, RR, IS_CALL_DATA);
    assertFalse(DATA.equals(other));
    //    other = new ForexSmileDeltaSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, DELTAS, ATM, RR, STRANGLE, !IS_CALL_DATA);
    //    assertFalse(DATA.equals(other));
  }

  @Test
  public void testBumpedPoint() {
    final SmileSurfaceDataBundle bumped = DATA.withBumpedPoint(0, 1, 0.05);
    final double[][] vols = DATA.getVolatilities();
    final double[][] bumpedVols = new double[vols.length][];
    for (int i = 0; i < vols.length; i++) {
      bumpedVols[i] = new double[vols[i].length];
      System.arraycopy(vols[i], 0, bumpedVols[i], 0, vols[i].length);
    }
    bumpedVols[0][1] += 0.05;
    for (int i = 0; i < vols.length; i++) {
      assertArrayEquals(bumpedVols[i], bumped.getVolatilities()[i], 0);
    }
  }
}
