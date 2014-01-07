/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.ShiftType;
import com.opengamma.analytics.math.curve.AddCurveSpreadFunction;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.MultiplyCurveSpreadFunction;
import com.opengamma.analytics.math.curve.SpreadDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Tests the functionality in {@link YieldCurveUtils}
 */
@Test(groups = TestGroup.UNIT)
public class YieldCurveUtilsTest {
  /** Adds yield curves */
  private static final AddCurveSpreadFunction ADD_CURVE_FUNCTION = new AddCurveSpreadFunction();
  /** Multiplies yield curves */
  private static final MultiplyCurveSpreadFunction MULTIPLY_CURVE_FUNCTION = new MultiplyCurveSpreadFunction();
  /** The interpolator for the original curve */
  private static final Interpolator1D INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.DOUBLE_QUADRATIC,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR, Interpolator1DFactory.LINEAR_EXTRAPOLATOR);
  /** The step interpolator used for bucketed shifts */
  private static final Interpolator1D STEP_INTERPOLATOR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.STEP,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  /** Time points of the original curve */
  private static final double[] T = new double[] {1, 2, 3, 4, 5, 6, 7};
  /** Yield points of the original curve */
  private static final double[] Y = new double[] {0.01, 0.015, 0.017, 0.02, 0.025, 0.035, 0.05};
  /** The original interpolated curve */
  private static final DoublesCurve INTERPOLATED_CURVE = InterpolatedDoublesCurve.fromSorted(T, Y, INTERPOLATOR);
  /** The original yield curve */
  private static final YieldCurve ORIGINAL_CURVE = new YieldCurve("ORIGINAL", INTERPOLATED_CURVE);


  /**
   * Tests a null yield curve for parallel shifts
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve1() {
    YieldCurveUtils.withParallelShift(null, 100, ShiftType.ABSOLUTE);
  }

  /**
   * Tests a null shift type for parallel shifts
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullShiftType1() {
    YieldCurveUtils.withParallelShift(ORIGINAL_CURVE, 100, null);
  }

  /**
   * Tests a null yield curve for bucketed shifts
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve2() {
    YieldCurveUtils.withBucketedShifts(null, new ArrayList<DoublesPair>(), new ArrayList<Double>(), ShiftType.ABSOLUTE);
  }

  /**
   * Tests a null list of buckets
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullBuckets() {
    YieldCurveUtils.withBucketedShifts(ORIGINAL_CURVE, null, new ArrayList<Double>(), ShiftType.ABSOLUTE);
  }

  /**
   * Tests a null list of bucketed shifts
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullShifts1() {
    YieldCurveUtils.withBucketedShifts(ORIGINAL_CURVE, new ArrayList<DoublesPair>(), null, ShiftType.ABSOLUTE);
  }

  /**
   * Tests a null shift type for bucketed shifts
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullShiftType2() {
    YieldCurveUtils.withBucketedShifts(ORIGINAL_CURVE, new ArrayList<DoublesPair>(), new ArrayList<Double>(), null);
  }

  /**
   * Tests the case where there is not an equal number of shifts and buckets
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDifferentLengthShifts1() {
    YieldCurveUtils.withBucketedShifts(ORIGINAL_CURVE, Arrays.asList(DoublesPair.of(1., 2.)), new ArrayList<Double>(), ShiftType.ABSOLUTE);
  }

  /**
   * Tests a null yield curve for point shifts
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurve3() {
    YieldCurveUtils.withPointShifts(null, new ArrayList<Double>(), new ArrayList<Double>(), ShiftType.ABSOLUTE);
  }

  /**
   * Tests a null list of points
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullPoints() {
    YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, null, new ArrayList<Double>(), ShiftType.ABSOLUTE);
  }

  /**
   * Tests a null list of points shifts
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullShifts2() {
    YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, new ArrayList<Double>(), null, ShiftType.ABSOLUTE);
  }

  /**
   * Tests a null shift type for point shifts
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullShiftType3() {
    YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, new ArrayList<Double>(), new ArrayList<Double>(), null);
  }

  /**
   * Tests the case where there is not an equal number of shifts and points
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDifferentLengthShifts2() {
    YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, Arrays.asList(3., 5.), new ArrayList<Double>(), ShiftType.ABSOLUTE);
  }

  /**
   * Tests that only interpolated curves are handled.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testConstantCurvePointsShift() {
    YieldCurveUtils.withPointShifts(new YieldCurve("NAME", ConstantDoublesCurve.from(0.)), new ArrayList<Double>(), new ArrayList<Double>(), ShiftType.ABSOLUTE);
  }

  /**
   * Tests that doing a parallel absolute shift of zero returns a different yield curve with the
   * same shape.
   */
  @Test
  public void testZeroAbsoluteParallelShift() {
    final YieldCurve shiftedCurve = YieldCurveUtils.withParallelShift(ORIGINAL_CURVE, 0, ShiftType.ABSOLUTE);
    assertEquals("ORIGINAL_WithParallelShift", shiftedCurve.getName());
    assertFalse(shiftedCurve.getCurve().equals(ORIGINAL_CURVE.getCurve()));
    assertTrue(shiftedCurve.getCurve() instanceof SpreadDoublesCurve);
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-15);
  }

  /**
   * Tests that doing a parallel relative shift of zero returns a different yield curve with the same
   * shape.
   */
  @Test
  public void testZeroRelativeParallelShift() {
    final YieldCurve shiftedCurve = YieldCurveUtils.withParallelShift(ORIGINAL_CURVE, 0, ShiftType.RELATIVE);
    assertEquals("ORIGINAL_WithParallelShift", shiftedCurve.getName());
    assertFalse(shiftedCurve.getCurve().equals(ORIGINAL_CURVE.getCurve()));
    assertTrue(shiftedCurve.getCurve() instanceof SpreadDoublesCurve);
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-15);
  }

  /**
   * Tests absolute parallel shifts. A round trip (+ve shift -> -ve shift) should return the same
   * curve as the original.
   */
  @Test
  public void testAbsoluteParallelShift() {
    final double shift = 0.03;
    YieldCurve shiftedCurve = YieldCurveUtils.withParallelShift(ORIGINAL_CURVE, shift, ShiftType.ABSOLUTE);
    final double[] y = new double[T.length];
    for (int i = 0; i < y.length; i++) {
      y[i] = Y[i] + shift;
    }
    final DoublesCurve expectedCurve = InterpolatedDoublesCurve.from(T, y, INTERPOLATOR);
    assertCurveEquals(expectedCurve, shiftedCurve.getCurve(), 1e-9);
    shiftedCurve = YieldCurveUtils.withParallelShift(shiftedCurve, -shift, ShiftType.ABSOLUTE);
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-9);
  }

  /**
   * Tests relative parallel shifts. A round trip (shift -> -shift / (1 + shift)) should return the same
   * curve as the original.
   */
  @Test
  public void testRelativeParallelShift() {
    final double shift = 0.03;
    YieldCurve shiftedCurve = YieldCurveUtils.withParallelShift(ORIGINAL_CURVE, shift, ShiftType.RELATIVE);
    final double[] y = new double[T.length];
    for (int i = 0; i < y.length; i++) {
      y[i] = Y[i] * (1 + shift);
    }
    final DoublesCurve expectedCurve = InterpolatedDoublesCurve.from(T, y, INTERPOLATOR);
    assertCurveEquals(expectedCurve, shiftedCurve.getCurve(), 1e-9);
    shiftedCurve = YieldCurveUtils.withParallelShift(shiftedCurve, -shift / (1 + shift), ShiftType.RELATIVE);
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-9);
  }

  /**
   * Tests that performing absolute shifts with an empty list of buckets returns a different yield curve with the same
   * shape.
   */
  @Test
  public void testEmptyBucketedAbsoluteShifts() {
    final List<DoublesPair> buckets = new ArrayList<>();
    final List<Double> shifts = new ArrayList<>();
    final YieldCurve shiftedCurve = YieldCurveUtils.withBucketedShifts(ORIGINAL_CURVE, buckets, shifts, ShiftType.ABSOLUTE);
    assertEquals("ORIGINAL_WithBucketedShifts", shiftedCurve.getName());
    assertFalse(ORIGINAL_CURVE.equals(shiftedCurve));
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-15);
  }

  /**
   * Tests that performing relative shifts with an empty list of buckets returns a different yield curve with the same
   * shape.
   */
  @Test
  public void testEmptyBucketedRelativeShifts() {
    final List<DoublesPair> buckets = new ArrayList<>();
    final List<Double> shifts = new ArrayList<>();
    final YieldCurve shiftedCurve = YieldCurveUtils.withBucketedShifts(ORIGINAL_CURVE, buckets, shifts, ShiftType.RELATIVE);
    assertEquals("ORIGINAL_WithBucketedShifts", shiftedCurve.getName());
    assertFalse(ORIGINAL_CURVE.equals(shiftedCurve));
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-15);
  }

  /**
   * Tests that performing absolute bucketed shifts of zero returns a different yield curve with the same shape.
   */
  @Test
  public void testZeroBucketedAbsoluteShifts() {
    final List<DoublesPair> buckets = Arrays.asList(DoublesPair.of(1., 2.), DoublesPair.of(2., 3.), DoublesPair.of(3., 4.));
    final List<Double> shifts = Arrays.asList(0., 0., 0.);
    final YieldCurve shiftedCurve = YieldCurveUtils.withBucketedShifts(ORIGINAL_CURVE, buckets, shifts, ShiftType.ABSOLUTE);
    assertFalse(ORIGINAL_CURVE.equals(shiftedCurve));
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-15);
  }

  /**
   * Tests that performing relative bucketed shifts of zero returns a different yield curve with the same shape.
   */
  @Test
  public void testZeroBucketedRelativeShifts() {
    final List<DoublesPair> buckets = Arrays.asList(DoublesPair.of(1., 2.), DoublesPair.of(2., 3.), DoublesPair.of(3., 4.));
    final List<Double> shifts = Arrays.asList(0., 0., 0.);
    final YieldCurve shiftedCurve = YieldCurveUtils.withBucketedShifts(ORIGINAL_CURVE, buckets, shifts, ShiftType.RELATIVE);
    assertFalse(ORIGINAL_CURVE.equals(shiftedCurve));
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-15);
  }

  /**
   * Tests that a single absolute shift over the entire relevant curve range is equivalent to a parallel shift.
   */
  @Test
  public void testSingleBucketedAbsoluteShift() {
    final double startTime = -2;
    final double endTime = 20;
    final double shift = 0.02;
    final List<DoublesPair> buckets = Arrays.asList(DoublesPair.of(startTime, endTime));
    final List<Double> shifts = Arrays.asList(shift);
    final List<Double> inverseShifts = Arrays.asList(-shift);
    YieldCurve shiftedCurve = YieldCurveUtils.withBucketedShifts(ORIGINAL_CURVE, buckets, shifts, ShiftType.ABSOLUTE);
    assertCurveEquals(YieldCurveUtils.withParallelShift(ORIGINAL_CURVE, shift, ShiftType.ABSOLUTE).getCurve(), shiftedCurve.getCurve(), startTime, endTime, 0.0001, 1e-9);
    shiftedCurve = YieldCurveUtils.withBucketedShifts(shiftedCurve, buckets, inverseShifts, ShiftType.ABSOLUTE);
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-9);
  }

  /**
   * Tests that a single relative shift over the entire relevant curve range is equivalent to a parallel shift.
   */
  @Test
  public void testSingleBucketedRelativeShift() {
    final double startTime = -2;
    final double endTime = 20;
    final double shift = 0.02;
    final List<DoublesPair> buckets = Arrays.asList(DoublesPair.of(startTime, endTime));
    final List<Double> shifts = Arrays.asList(shift);
    final List<Double> inverseShifts = Arrays.asList(-shift / (1 + shift));
    YieldCurve shiftedCurve = YieldCurveUtils.withBucketedShifts(ORIGINAL_CURVE, buckets, shifts, ShiftType.RELATIVE);
    assertCurveEquals(YieldCurveUtils.withParallelShift(ORIGINAL_CURVE, shift, ShiftType.RELATIVE).getCurve(), shiftedCurve.getCurve(), startTime, endTime, 0.0001, 1e-9);
    shiftedCurve = YieldCurveUtils.withBucketedShifts(shiftedCurve, buckets, inverseShifts, ShiftType.RELATIVE);
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-9);
  }

  /**
   * Tests a series of contiguous absolute bucketed shifts.
   */
  @Test
  public void testBucketedAbsoluteShifts1() {
    final List<DoublesPair> buckets = Arrays.asList(DoublesPair.of(1., 5.), DoublesPair.of(5., 7.5), DoublesPair.of(7.5, 10));
    final List<Double> shifts = Arrays.asList(3., 4., 5.);
    final List<Double> inverseShifts = Arrays.asList(-3., -4., -5.);
    final DoublesCurve expectedSpreadCurve = InterpolatedDoublesCurve.from(new double[] {0, 1, 5, 7.5, 10}, new double[] {0, 3., 4., 5., 0.}, STEP_INTERPOLATOR);
    final DoublesCurve expectedCurve = SpreadDoublesCurve.from(ADD_CURVE_FUNCTION, ORIGINAL_CURVE.getCurve(), expectedSpreadCurve);
    YieldCurve shiftedCurve = YieldCurveUtils.withBucketedShifts(ORIGINAL_CURVE, buckets, shifts, ShiftType.ABSOLUTE);
    assertCurveEquals(expectedCurve, shiftedCurve.getCurve(), 1e-9);
    shiftedCurve = YieldCurveUtils.withBucketedShifts(shiftedCurve, buckets, inverseShifts, ShiftType.ABSOLUTE);
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-9);
  }

  /**
   * Tests a series of contiguous relative bucketed shifts.
   */
  @Test
  public void testBucketedRelativeShifts1() {
    final List<DoublesPair> buckets = Arrays.asList(DoublesPair.of(1., 5.), DoublesPair.of(5., 7.5), DoublesPair.of(7.5, 10));
    final List<Double> shifts = Arrays.asList(3., 4., 5.);
    final List<Double> inverseShifts = Arrays.asList(-0.75, -0.8, -5. / 6);
    final DoublesCurve expectedSpreadCurve = InterpolatedDoublesCurve.from(new double[] {0, 1, 5, 7.5, 10}, new double[] {1, 4., 5., 6., 1.}, STEP_INTERPOLATOR);
    final DoublesCurve expectedCurve = SpreadDoublesCurve.from(MULTIPLY_CURVE_FUNCTION, ORIGINAL_CURVE.getCurve(), expectedSpreadCurve);
    YieldCurve shiftedCurve = YieldCurveUtils.withBucketedShifts(ORIGINAL_CURVE, buckets, shifts, ShiftType.RELATIVE);
    assertCurveEquals(expectedCurve, shiftedCurve.getCurve(), 1e-9);
    shiftedCurve = YieldCurveUtils.withBucketedShifts(shiftedCurve, buckets, inverseShifts, ShiftType.RELATIVE);
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-9);
  }

  /**
   * Tests a series of non-contiguous absolute bucketed shifts.
   */
  @Test
  public void testBucketedAbsoluteShifts2() {
    final List<DoublesPair> buckets = Arrays.asList(DoublesPair.of(1., 2.), DoublesPair.of(2.5, 3.5), DoublesPair.of(5., 7.5), DoublesPair.of(7.5, 10));
    final List<Double> shifts = Arrays.asList(3., 4., 5., 6.);
    final List<Double> inverseShifts = Arrays.asList(-3., -4., -5., -6.);
    final DoublesCurve expectedSpreadCurve = InterpolatedDoublesCurve.from(new double[] {0, 1, 2, 2.5, 3.5, 5, 7.5, 10}, new double[] {0, 3., 0, 4., 0., 5., 6., 0.}, STEP_INTERPOLATOR);
    final DoublesCurve expectedCurve = SpreadDoublesCurve.from(ADD_CURVE_FUNCTION, ORIGINAL_CURVE.getCurve(), expectedSpreadCurve);
    YieldCurve shiftedCurve = YieldCurveUtils.withBucketedShifts(ORIGINAL_CURVE, buckets, shifts, ShiftType.ABSOLUTE);
    assertCurveEquals(expectedCurve, shiftedCurve.getCurve(), 1e-9);
    shiftedCurve = YieldCurveUtils.withBucketedShifts(shiftedCurve, buckets, inverseShifts, ShiftType.ABSOLUTE);
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-9);
  }

  /**
   * Tests a series of non-contiguous relative bucketed shifts.
   */
  @Test
  public void testBucketedRelativeShifts2() {
    final List<DoublesPair> buckets = Arrays.asList(DoublesPair.of(1., 2.), DoublesPair.of(2.5, 3.5), DoublesPair.of(5., 7.5), DoublesPair.of(7.5, 10));
    final List<Double> shifts = Arrays.asList(3., 4., 5., 6.);
    final List<Double> inverseShifts = Arrays.asList(-0.75, -0.8, -5. / 6, -6. / 7);
    final DoublesCurve expectedSpreadCurve = InterpolatedDoublesCurve.from(new double[] {0, 1, 2, 2.5, 3.5, 5, 7.5, 10}, new double[] {1, 4., 1, 5., 1., 6., 7., 1.}, STEP_INTERPOLATOR);
    final DoublesCurve expectedCurve = SpreadDoublesCurve.from(MULTIPLY_CURVE_FUNCTION, ORIGINAL_CURVE.getCurve(), expectedSpreadCurve);
    YieldCurve shiftedCurve = YieldCurveUtils.withBucketedShifts(ORIGINAL_CURVE, buckets, shifts, ShiftType.RELATIVE);
    assertCurveEquals(expectedCurve, shiftedCurve.getCurve(), 1e-9);
    shiftedCurve = YieldCurveUtils.withBucketedShifts(shiftedCurve, buckets, inverseShifts, ShiftType.RELATIVE);
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-9);
  }

  /**
   * Tests that empty point shifts returns a different curve with the same shape.
   */
  @Test
  public void testEmptyPointAbsoluteShifts() {
    final List<Double> points = new ArrayList<>();
    final List<Double> shifts = new ArrayList<>();
    final YieldCurve shiftedCurve = YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, points, shifts, ShiftType.ABSOLUTE);
    assertEquals("ORIGINAL_WithPointShifts", shiftedCurve.getName());
    assertFalse(ORIGINAL_CURVE.equals(shiftedCurve));
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-15);
  }

  /**
   * Tests that empty point shifts returns a different curve with the same shape.
   */
  @Test
  public void testEmptyPointRelativeShifts() {
    final List<Double> points = new ArrayList<>();
    final List<Double> shifts = new ArrayList<>();
    final YieldCurve shiftedCurve = YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, points, shifts, ShiftType.RELATIVE);
    assertEquals("ORIGINAL_WithPointShifts", shiftedCurve.getName());
    assertFalse(ORIGINAL_CURVE.equals(shiftedCurve));
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-15);
  }

  /**
   * Tests that a series of zero absolute point shifts returns a different curve with the same shape.
   */
  @Test
  public void testPointZeroAbsoluteShifts() {
    List<Double> points = Arrays.asList(1., 2., 3., 4.);
    final List<Double> shifts = Arrays.asList(0., 0., 0., 0.);
    YieldCurve shiftedCurve = YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, points, shifts, ShiftType.ABSOLUTE);
    assertEquals("ORIGINAL_WithPointShifts", shiftedCurve.getName());
    assertFalse(ORIGINAL_CURVE.equals(shiftedCurve));
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-9);
    points = Arrays.asList(1.1, 2.1, 3.1, 4.1);
    final double[] newT = new double[] {1, 1.1, 2, 2.1, 3, 3.1, 4, 4.1, 5, 6, 7};
    final double[] newY = new double[] {0.01, INTERPOLATED_CURVE.getYValue(1.1), 0.015, INTERPOLATED_CURVE.getYValue(2.1), 0.017, INTERPOLATED_CURVE.getYValue(3.1), 0.02, INTERPOLATED_CURVE.getYValue(4.1), 0.025, 0.035, 0.05};
    final DoublesCurve expectedCurve = InterpolatedDoublesCurve.from(newT, newY, INTERPOLATOR);
    shiftedCurve = YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, points, shifts, ShiftType.ABSOLUTE);
    assertEquals("ORIGINAL_WithPointShifts", shiftedCurve.getName());
    assertCurveEquals(expectedCurve, shiftedCurve.getCurve(), 1e-9);
  }

  /**
   * Tests that a series of zero relative point shifts returns a different curve with the same shape.
   */
  @Test
  public void testPointZeroRelativeShifts() {
    List<Double> points = Arrays.asList(1., 2., 3., 4.);
    final List<Double> shifts = Arrays.asList(0., 0., 0., 0.);
    YieldCurve shiftedCurve = YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, points, shifts, ShiftType.RELATIVE);
    assertEquals("ORIGINAL_WithPointShifts", shiftedCurve.getName());
    assertFalse(ORIGINAL_CURVE.equals(shiftedCurve));
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), shiftedCurve.getCurve(), 1e-9);
    points = Arrays.asList(1.1, 2.1, 3.1, 4.1);
    final double[] newT = new double[] {1, 1.1, 2, 2.1, 3, 3.1, 4, 4.1, 5, 6, 7};
    final double[] newY = new double[] {0.01, INTERPOLATED_CURVE.getYValue(1.1), 0.015, INTERPOLATED_CURVE.getYValue(2.1), 0.017, INTERPOLATED_CURVE.getYValue(3.1), 0.02, INTERPOLATED_CURVE.getYValue(4.1), 0.025, 0.035, 0.05};
    final DoublesCurve expectedCurve = InterpolatedDoublesCurve.from(newT, newY, INTERPOLATOR);
    shiftedCurve = YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, points, shifts, ShiftType.RELATIVE);
    assertEquals("ORIGINAL_WithPointShifts", shiftedCurve.getName());
    assertCurveEquals(expectedCurve, shiftedCurve.getCurve(), 1e-9);
  }

  /**
   * Tests a series of absolute point shifts.
   */
  @Test
  public void testPointAbsoluteShifts() {
    List<Double> points = Arrays.asList(1., 2., 3., 4.);
    final List<Double> shifts = Arrays.asList(2., 3., 4., 5.);
    final double[] y = new double[Y.length];
    System.arraycopy(Y, 0, y, 0, Y.length);
    y[0] += 2;
    y[1] += 3;
    y[2] += 4;
    y[3] += 5;
    YieldCurve shiftedCurve = YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, points, shifts, ShiftType.ABSOLUTE);
    DoublesCurve expectedCurve = InterpolatedDoublesCurve.from(T, y, INTERPOLATOR);
    assertEquals("ORIGINAL_WithPointShifts", shiftedCurve.getName());
    assertCurveEquals(expectedCurve, shiftedCurve.getCurve(), 1e-9);
    shiftedCurve = YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, points, shifts, ShiftType.ABSOLUTE);
    assertEquals("ORIGINAL_WithPointShifts", shiftedCurve.getName());
    assertCurveEquals(expectedCurve, shiftedCurve.getCurve(), 1e-9);
    final List<Double> inverseShifts = Arrays.asList(-2., -3., -4., -5.);
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), YieldCurveUtils.withPointShifts(shiftedCurve, points, inverseShifts, ShiftType.ABSOLUTE).getCurve(), 1e-9);
    points = Arrays.asList(1.1, 2.1, 3.1, 4.1);
    double[] newT = new double[] {1, 1.1, 2, 2.1, 3, 3.1, 4, 4.1, 5, 6, 7};
    double[] newY = new double[] {0.01, INTERPOLATED_CURVE.getYValue(1.1) + 2, 0.015, INTERPOLATED_CURVE.getYValue(2.1) + 3, 0.017, INTERPOLATED_CURVE.getYValue(3.1) + 4, 0.02, INTERPOLATED_CURVE.getYValue(4.1) + 5, 0.025, 0.035, 0.05};
    expectedCurve = InterpolatedDoublesCurve.from(newT, newY, INTERPOLATOR);
    shiftedCurve = YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, points, shifts, ShiftType.ABSOLUTE);
    assertCurveEquals(expectedCurve, shiftedCurve.getCurve(), 1e-9);
    newT = new double[] {1, 1.1, 2, 2.1, 3, 3.1, 4, 4.1, 5, 6, 7};
    newY = new double[] {0.01, INTERPOLATED_CURVE.getYValue(1.1), 0.015, INTERPOLATED_CURVE.getYValue(2.1), 0.017, INTERPOLATED_CURVE.getYValue(3.1), 0.02, INTERPOLATED_CURVE.getYValue(4.1), 0.025, 0.035, 0.05};
    expectedCurve = InterpolatedDoublesCurve.from(newT, newY, INTERPOLATOR);
    assertCurveEquals(expectedCurve, YieldCurveUtils.withPointShifts(shiftedCurve, points, inverseShifts, ShiftType.ABSOLUTE).getCurve(), 1e-9);
  }

  /**
   * Tests a series of relative point shifts.
   */
  @Test
  public void testPointRelativeShifts() {
    List<Double> points = Arrays.asList(1., 2., 3., 4.);
    final List<Double> shifts = Arrays.asList(2., 3., 4., 5.);
    final double[] y = new double[Y.length];
    System.arraycopy(Y, 0, y, 0, Y.length);
    y[0] *= 3;
    y[1] *= 4;
    y[2] *= 5;
    y[3] *= 6;
    YieldCurve shiftedCurve = YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, points, shifts, ShiftType.RELATIVE);
    DoublesCurve expectedCurve = InterpolatedDoublesCurve.from(T, y, INTERPOLATOR);
    assertEquals("ORIGINAL_WithPointShifts", shiftedCurve.getName());
    assertCurveEquals(expectedCurve, shiftedCurve.getCurve(), 1e-9);
    shiftedCurve = YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, points, shifts, ShiftType.RELATIVE);
    assertEquals("ORIGINAL_WithPointShifts", shiftedCurve.getName());
    assertCurveEquals(expectedCurve, shiftedCurve.getCurve(), 1e-9);
    final List<Double> inverseShifts = Arrays.asList(-2. / 3, -0.75, -0.8, -5 / 6.);
    assertCurveEquals(ORIGINAL_CURVE.getCurve(), YieldCurveUtils.withPointShifts(shiftedCurve, points, inverseShifts, ShiftType.RELATIVE).getCurve(), 1e-9);
    points = Arrays.asList(1.1, 2.1, 3.1, 4.1);
    double[] newT = new double[] {1, 1.1, 2, 2.1, 3, 3.1, 4, 4.1, 5, 6, 7};
    double[] newY = new double[] {0.01, INTERPOLATED_CURVE.getYValue(1.1) * 3, 0.015, INTERPOLATED_CURVE.getYValue(2.1) * 4, 0.017, INTERPOLATED_CURVE.getYValue(3.1) * 5, 0.02, INTERPOLATED_CURVE.getYValue(4.1) * 6, 0.025, 0.035, 0.05};
    expectedCurve = InterpolatedDoublesCurve.from(newT, newY, INTERPOLATOR);
    shiftedCurve = YieldCurveUtils.withPointShifts(ORIGINAL_CURVE, points, shifts, ShiftType.RELATIVE);
    assertCurveEquals(expectedCurve, shiftedCurve.getCurve(), 1e-9);
    newT = new double[] {1, 1.1, 2, 2.1, 3, 3.1, 4, 4.1, 5, 6, 7};
    newY = new double[] {0.01, INTERPOLATED_CURVE.getYValue(1.1), 0.015, INTERPOLATED_CURVE.getYValue(2.1), 0.017, INTERPOLATED_CURVE.getYValue(3.1), 0.02, INTERPOLATED_CURVE.getYValue(4.1), 0.025, 0.035, 0.05};
    expectedCurve = InterpolatedDoublesCurve.from(newT, newY, INTERPOLATOR);
    assertCurveEquals(expectedCurve, YieldCurveUtils.withPointShifts(shiftedCurve, points, inverseShifts, ShiftType.RELATIVE).getCurve(), 1e-9);
  }

  /**
   * Tests that two curves are equal to within an absolute tolerance by sampling.
   * @param expected The expected curve
   * @param actual The actual curve
   * @param eps The absolute result tolerance
   */
  private static void assertCurveEquals(final DoublesCurve expected, final DoublesCurve actual, final double eps) {
    for (double t = -1; t <= 10; t += 0.001) {
      assertEquals("t = " + t, expected.getYValue(t), actual.getYValue(t), eps);
    }
  }

  /**
   * Tests that two curves are equal to within an absolute tolerance by sampling.
   * @param expected The expected curve
   * @param actual The actual curve
   * @param startTime The time at which to start comparing the curves
   * @param endTime The time at which to end comparing the curves
   * @param delta The time step
   * @param eps The absolute result tolerance
   */
  private static void assertCurveEquals(final DoublesCurve expected, final DoublesCurve actual, final double startTime,
      final double endTime, final double delta, final double eps) {
    for (double t = -1; t <= 10; t += 0.001) {
      assertEquals("t = " + t, expected.getYValue(t), actual.getYValue(t), eps);
    }
  }
}
