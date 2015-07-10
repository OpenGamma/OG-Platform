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
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class StandardSmileSurfaceDataBundleTest {
  private static final double SPOT = 1.0;
  private static final double[] FORWARDS = new double[] {1.1, 1.15, 1.3 };
  private static final double[] EXPIRIES = new double[] {0.1, 0.25, 1 };
  private static final double[][] STRIKES = new double[][] {
      new double[] {1.1, 1.15, 1.3, 1.5, 1.7 },
      new double[] {1.1, 1.15, 1.3, 1.5, 1.7, 2 },
      new double[] {1.15, 1.3, 1.5, 1.7, 2 } };

  private static final double[][] VOLS = new double[][] {
      new double[] {0.1, 0.1, 0.1, 0.1, 0.1 },
      new double[] {0.11, 0.11, 0.11, 0.11, 0.11, 0.11 },
      new double[] {0.12, 0.12, 0.12, 0.12, 0.12 } };

  private static final double[][] DECREASING_VARIANCE = new double[][] {
      new double[] {0.1, 0.01, 0.001, 0.0001, 0.00001, 0.000001 },
      new double[] {0.11, 0.011, 0.0011, 0.00011, 0.000011, 0.0000011 },
      new double[] {0.12, 0.00012, 0.00012, 0.00012, 0.000012, 0.0000012 } };
  private static final Interpolator1D INTERPOLATOR = Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE;
  private static final ForwardCurve FORWARD_CURVE;
  private static final StandardSmileSurfaceDataBundle DATA;
  private static final double EPS = 1e-15;

  static {
    final double[] t = ArrayUtils.add(EXPIRIES, 0, 0.0);
    final double[] f = ArrayUtils.add(FORWARDS, 0, SPOT);

    FORWARD_CURVE = new ForwardCurve(InterpolatedDoublesCurve.from(t, f, INTERPOLATOR));
    DATA = new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, VOLS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullForwards() {
    new StandardSmileSurfaceDataBundle(SPOT, null, EXPIRIES, STRIKES, VOLS, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiries1() {
    new StandardSmileSurfaceDataBundle(SPOT, FORWARDS, null, STRIKES, VOLS, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStrikes1() {
    new StandardSmileSurfaceDataBundle(SPOT, FORWARDS, EXPIRIES, null, VOLS, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVols1() {
    new StandardSmileSurfaceDataBundle(SPOT, FORWARDS, EXPIRIES, STRIKES, null, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator() {
    new StandardSmileSurfaceDataBundle(SPOT, FORWARDS, EXPIRIES, STRIKES, VOLS, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullForwardCurve() {
    new StandardSmileSurfaceDataBundle(null, EXPIRIES, STRIKES, VOLS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiries2() {
    new StandardSmileSurfaceDataBundle(FORWARD_CURVE, null, STRIKES, VOLS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStrikes2() {
    new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, null, VOLS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVols2() {
    new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testForwardLength() {
    new StandardSmileSurfaceDataBundle(SPOT, new double[] {1, 2, 3, 4 }, EXPIRIES, STRIKES, VOLS, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStrikeLength1() {
    new StandardSmileSurfaceDataBundle(SPOT, FORWARDS, EXPIRIES, new double[][] {new double[] {1, 2, 3, 4, 5, 6 } }, VOLS, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testVolLength1() {
    new StandardSmileSurfaceDataBundle(SPOT, FORWARDS, EXPIRIES, STRIKES, new double[][] {new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.1 } }, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testVolLength2() {
    new StandardSmileSurfaceDataBundle(SPOT, FORWARDS, EXPIRIES, STRIKES, new double[][] {
        new double[] {0.1, 0.1, 0.1, 0.1 },
        new double[] {0.11, 0.11, 0.11, 0.11 },
        new double[] {0.12, 0.12, 0.12, 0.12 } }, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadVolData1() {
    new StandardSmileSurfaceDataBundle(SPOT, FORWARDS, EXPIRIES, STRIKES, DECREASING_VARIANCE, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStrikeLength2() {
    new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, new double[][] {new double[] {1, 2, 3, 4, 5, 6 } }, VOLS);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testVolLength3() {
    new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, new double[][] {new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.1 } });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testVolLength4() {
    new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, new double[][] {
        new double[] {0.1, 0.1, 0.1, 0.1 },
        new double[] {0.11, 0.11, 0.11, 0.11 },
        new double[] {0.12, 0.12, 0.12, 0.12 } });
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadVolData2() {
    new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, DECREASING_VARIANCE);
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
    for (int i = 0; i < STRIKES.length; i++) {
      assertArrayEquals(STRIKES[i], DATA.getStrikes()[i], 0);
      assertArrayEquals(VOLS[i], DATA.getVolatilities()[i], 0);
    }
    assertEquals(FORWARD_CURVE, DATA.getForwardCurve());
    //   assertEquals(IS_CALL_DATA, DATA.isCallData());
    StandardSmileSurfaceDataBundle other = new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, VOLS);
    assertEquals(DATA, other);
    assertEquals(DATA.hashCode(), other.hashCode());
    other = new StandardSmileSurfaceDataBundle(SPOT, FORWARDS, EXPIRIES, STRIKES, VOLS, INTERPOLATOR);
    assertArrayEquals(DATA.getExpiries(), other.getExpiries(), 0);
    for (int i = 0; i < STRIKES.length; i++) {
      assertArrayEquals(DATA.getStrikes()[i], other.getStrikes()[i], 0);
      assertArrayEquals(DATA.getVolatilities()[i], other.getVolatilities()[i], 0);
    }

    assertArrayEquals(ArrayUtils.toPrimitive(DATA.getForwardCurve().getForwardCurve().getXData()),
        ArrayUtils.toPrimitive(other.getForwardCurve().getForwardCurve().getXData()), 0);
    assertArrayEquals(ArrayUtils.toPrimitive(DATA.getForwardCurve().getForwardCurve().getYData()),
        ArrayUtils.toPrimitive(other.getForwardCurve().getForwardCurve().getYData()), 0);
    assertEquals(((InterpolatedDoublesCurve) DATA.getForwardCurve().getForwardCurve()).getInterpolator(),
        ((InterpolatedDoublesCurve) other.getForwardCurve().getForwardCurve()).getInterpolator());
    final ForwardCurve otherCurve = new ForwardCurve(InterpolatedDoublesCurve.from(ArrayUtils.add(EXPIRIES, 0, 0), ArrayUtils.add(EXPIRIES, 0, SPOT), INTERPOLATOR));
    other = new StandardSmileSurfaceDataBundle(otherCurve, EXPIRIES, STRIKES, VOLS);
    assertFalse(DATA.equals(other));
    other = new StandardSmileSurfaceDataBundle(FORWARD_CURVE, new double[] {0, 0.01, 0.02 }, STRIKES, VOLS);
    assertFalse(DATA.equals(other));
    other = new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, VOLS, VOLS);
    assertFalse(DATA.equals(other));
    other = new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, STRIKES);
    assertFalse(DATA.equals(other));
    //    other = new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, VOLS, !IS_CALL_DATA);
    //    assertFalse(DATA.equals(other));
  }

  @Test
  public void testBumpedPoint() {
    final SmileSurfaceDataBundle bumped = DATA.withBumpedPoint(2, 3, 0.05);
    final double[][] bumpedVols = new double[VOLS.length][];
    for (int i = 0; i < VOLS.length; i++) {
      bumpedVols[i] = VOLS[i];
    }
    bumpedVols[2][3] += 0.05;
    assertEquals(bumped, new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, bumpedVols));
  }
}
