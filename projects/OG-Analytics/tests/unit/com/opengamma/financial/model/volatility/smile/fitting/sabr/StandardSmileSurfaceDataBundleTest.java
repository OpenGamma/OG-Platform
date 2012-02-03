/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting.sabr;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.apache.commons.lang.ArrayUtils;
import org.testng.annotations.Test;

import com.opengamma.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DFactory;

/**
 * 
 */
public class StandardSmileSurfaceDataBundleTest {
  private static final double[] FORWARDS = new double[] {1.1, 1.15, 1.3};
  private static final double[] EXPIRIES = new double[] {0, 0.25, 1};
  private static final double[][] STRIKES = new double[][] {
    new double[] {1.1, 1.15, 1.3, 1.5, 1.7, 2},
    new double[] {1.2, 1.25, 1.4, 1.6, 1.8, 2.1},
    new double[] {1.31, 1.36, 1.51, 1.71, 1.91, 2.21}};
  private static final double[][] VOLS = new double[][] {
    new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.1},
    new double[] {0.11, 0.11, 0.11, 0.11, 0.11, 0.11},
    new double[] {0.12, 0.12, 0.12, 0.12, 0.12, 0.12}};
  private static final double[][] DECREASING_VARIANCE = new double[][] {
    new double[] {0.1, 0.01, 0.001, 0.0001, 0.00001, 0.000001},
    new double[] {0.11, 0.011, 0.0011, 0.00011, 0.000011, 0.0000011},
    new double[] {0.12, 0.00012, 0.00012, 0.00012, 0.000012, 0.0000012}};
  private static final boolean IS_CALL_DATA = true;
  private static final Interpolator1D INTERPOLATOR = Interpolator1DFactory.DOUBLE_QUADRATIC_INSTANCE;
  private static final ForwardCurve FORWARD_CURVE = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, FORWARDS, INTERPOLATOR));
  private static final StandardSmileSurfaceDataBundle DATA = new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, VOLS, IS_CALL_DATA);
  private static final double EPS = 1e-15;

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullForwards() {
    new StandardSmileSurfaceDataBundle(null, EXPIRIES, STRIKES, VOLS, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiries1() {
    new StandardSmileSurfaceDataBundle(FORWARDS, null, STRIKES, VOLS, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStrikes1() {
    new StandardSmileSurfaceDataBundle(FORWARDS, EXPIRIES, null, VOLS, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVols1() {
    new StandardSmileSurfaceDataBundle(FORWARDS, EXPIRIES, STRIKES, null, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInterpolator() {
    new StandardSmileSurfaceDataBundle(FORWARDS, EXPIRIES, STRIKES, VOLS, IS_CALL_DATA, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullForwardCurve() {
    new StandardSmileSurfaceDataBundle(null, EXPIRIES, STRIKES, VOLS, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullExpiries2() {
    new StandardSmileSurfaceDataBundle(FORWARD_CURVE, null, STRIKES, VOLS, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStrikes2() {
    new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, null, VOLS, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVols2() {
    new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, null, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testForwardLength() {
    new StandardSmileSurfaceDataBundle(new double[]{1, 2, 3, 4}, EXPIRIES, STRIKES, VOLS, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStrikeLength1() {
    new StandardSmileSurfaceDataBundle(FORWARDS, EXPIRIES, new double[][] {new double[] {1, 2, 3, 4, 5, 6}}, VOLS, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testVolLength1() {
    new StandardSmileSurfaceDataBundle(FORWARDS, EXPIRIES, STRIKES, new double[][] {new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.1}}, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testVolLength2() {
    new StandardSmileSurfaceDataBundle(FORWARDS, EXPIRIES, STRIKES, new double[][] {
        new double[] {0.1, 0.1, 0.1, 0.1},
        new double[] {0.11, 0.11, 0.11, 0.11},
        new double[] {0.12, 0.12, 0.12, 0.12}}, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadVolData1() {
    new StandardSmileSurfaceDataBundle(FORWARDS, EXPIRIES, STRIKES, DECREASING_VARIANCE, IS_CALL_DATA, INTERPOLATOR);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testStrikeLength2() {
    new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, new double[][] {new double[] {1, 2, 3, 4, 5, 6}}, VOLS, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testVolLength3() {
    new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, new double[][] {new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.1}}, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testVolLength4() {
    new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, new double[][] {
        new double[] {0.1, 0.1, 0.1, 0.1},
        new double[] {0.11, 0.11, 0.11, 0.11},
        new double[] {0.12, 0.12, 0.12, 0.12}}, IS_CALL_DATA);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBadVolData2() {
    new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, DECREASING_VARIANCE, IS_CALL_DATA);
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
    assertEquals(IS_CALL_DATA, DATA.isCallData());
    StandardSmileSurfaceDataBundle other = new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, VOLS, IS_CALL_DATA);
    assertEquals(DATA, other);
    assertEquals(DATA.hashCode(), other.hashCode());
    other = new StandardSmileSurfaceDataBundle(FORWARDS, EXPIRIES, STRIKES, VOLS, IS_CALL_DATA, INTERPOLATOR);
    assertArrayEquals(DATA.getExpiries(), other.getExpiries(), 0);
    for (int i = 0; i < STRIKES.length; i++) {
      assertArrayEquals(DATA.getStrikes()[i], other.getStrikes()[i], 0);
      assertArrayEquals(DATA.getVolatilities()[i], other.getVolatilities()[i], 0);
    }
    assertEquals(DATA.isCallData(), other.isCallData());
    assertArrayEquals(ArrayUtils.toPrimitive(DATA.getForwardCurve().getForwardCurve().getXData()),
        ArrayUtils.toPrimitive(other.getForwardCurve().getForwardCurve().getXData()), 0);
    assertArrayEquals(ArrayUtils.toPrimitive(DATA.getForwardCurve().getForwardCurve().getYData()),
        ArrayUtils.toPrimitive(other.getForwardCurve().getForwardCurve().getYData()), 0);
    assertEquals(((InterpolatedDoublesCurve) DATA.getForwardCurve().getForwardCurve()).getInterpolator(),
        ((InterpolatedDoublesCurve) other.getForwardCurve().getForwardCurve()).getInterpolator());
    final ForwardCurve otherCurve = new ForwardCurve(InterpolatedDoublesCurve.from(EXPIRIES, EXPIRIES, INTERPOLATOR));
    other = new StandardSmileSurfaceDataBundle(otherCurve, EXPIRIES, STRIKES, VOLS, IS_CALL_DATA);
    assertFalse(DATA.equals(other));
    other = new StandardSmileSurfaceDataBundle(FORWARD_CURVE, new double[] {0, 0.01, 0.02}, STRIKES, VOLS, IS_CALL_DATA);
    assertFalse(DATA.equals(other));
    other = new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, VOLS, VOLS, IS_CALL_DATA);
    assertFalse(DATA.equals(other));
    other = new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, STRIKES, IS_CALL_DATA);
    assertFalse(DATA.equals(other));
    other = new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, VOLS, !IS_CALL_DATA);
    assertFalse(DATA.equals(other));
  }

  @Test
  public void testBumpedPoint() {
    final SmileSurfaceDataBundle bumped = DATA.withBumpedPoint(2, 3, 0.05);
    final double[][] bumpedVols = new double[VOLS.length][];
    for (int i = 0; i < VOLS.length; i++) {
      bumpedVols[i] = VOLS[i];
    }
    bumpedVols[2][3] += 0.05;
    assertEquals(bumped, new StandardSmileSurfaceDataBundle(FORWARD_CURVE, EXPIRIES, STRIKES, bumpedVols, IS_CALL_DATA));
  }
}
