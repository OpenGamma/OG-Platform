/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.ShiftType;
import com.opengamma.analytics.math.curve.AddCurveSpreadFunction;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.CurveShiftFunctionFactory;
import com.opengamma.analytics.math.curve.CurveSpreadFunction;
import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.MultiplyCurveSpreadFunction;
import com.opengamma.analytics.math.curve.SpreadDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Class containing utility methods for manipulating yield curves.
 */
public class YieldCurveUtils {
  /** Curve spread function that adds two curves */
  private static final CurveSpreadFunction ADD_SPREAD = AddCurveSpreadFunction.getInstance();
  /** Curve spread function that multiplies one curve by another */
  private static final CurveSpreadFunction MULTIPLY_SPREAD = MultiplyCurveSpreadFunction.getInstance();
  /** Suffix for parallel shifts */
  public static final String PARALLEL_SHIFT_NAME = "_WithParallelShift";
  /** Suffix for bucketed shifts */
  public static final String BUCKETED_SHIFT_NAME = "_WithBucketedShifts";
  /** Suffix for point shifts */
  public static final String POINT_SHIFT_NAME = "_WithPointShifts";

  /**
   * Shifts a curve by a constant amount over all tenors. If the {@link ShiftType} is
   * absolute, the shift is added to the curve i.e. a shift of 0.0001 results in all
   * yields on the curve having one basis point added. If it is relative, then all yields on
   * are the curve are multiplied by the shift amount i.e. a relative shift of 0.01 will
   * result in all points on the curve being shifted upwards by 1% of the yield.
   * <p>
   * The original curve is unchanged and a new curve is returned.
   * @param curve The original curve, not null
   * @param shift The shift
   * @param shiftType The shift type, not null
   * @return A new curve with all values shifted by a constant amount
   */
  public static YieldCurve withParallelShift(YieldCurve curve, double shift, ShiftType shiftType) {
    return withParallelShift(curve, shift, shiftType, PARALLEL_SHIFT_NAME);
  }

  /**
   * Shifts a curve by a constant amount over all tenors. If the {@link ShiftType} is
   * absolute, the shift is added to the curve i.e. a shift of 0.0001 results in all
   * yields on the curve having one basis point added. If it is relative, then all yields on
   * are the curve are multiplied by the shift amount i.e. a relative shift of 0.01 will
   * result in all points on the curve being shifted upwards by 1% of the yield.
   * <p>
   * The original curve is unchanged and a new curve is returned.
   * @param curve The original curve, not null
   * @param shift The shift
   * @param shiftType The shift type, not null
   * @param nameSuffix Suffix to add to the name of the shifted curve
   * @return A new curve with all values shifted by a constant amount
   */
  public static YieldCurve withParallelShift(YieldCurve curve, double shift, ShiftType shiftType, String nameSuffix) {
    ArgumentChecker.notNull(curve, "curve");
    ArgumentChecker.notNull(shiftType, "shift type");
    ArgumentChecker.notNull(nameSuffix, "nameSuffix");

    String newName = curve.getName() + nameSuffix;
    DoublesCurve underlyingCurve = curve.getCurve();
    switch (shiftType) {
      case ABSOLUTE:
        return new YieldCurve(
            newName,
            SpreadDoublesCurve.from(ADD_SPREAD, newName, underlyingCurve, ConstantDoublesCurve.from(shift)));
      case RELATIVE:
        return new YieldCurve(
            newName,
            SpreadDoublesCurve.from(MULTIPLY_SPREAD, newName, underlyingCurve, ConstantDoublesCurve.from(1 + shift)));
      default:
        throw new IllegalArgumentException("Cannot handle curve shift type " + shiftType + " for parallel shifts");
    }
  }

  /**
   * Performs bucketed shifts on curves. The buckets need not be continuous; if they are not,
   * then the curve is unchanged between the two times. The shifts include the lower time but
   * exclude the upper, and are applied as a step function (i.e. constant over the bucket).
   * The units of time of the buckets are years.
   * <p>
   * If the {@link ShiftType} is absolute, the shift is added to the curve; a shift of 0.0001
   * from one year to two years results in the curve being shifted upwards by one basis point
   * from the one year point to the two year point. If this shift is relative, the yields are
   * multiplied by the shift amount.
   * <p>
   * The original curve is unchanged and a new curve is returned.
   * @param curve The original curve, not null
   * @param buckets The buckets, not null
   * @param shifts The shifts, not null. There must be one shift per bucket.
   * @param shiftType The shift type, not null
   * @return A new curve with bucketed shifts applied
   */
  public static YieldCurve withBucketedShifts(YieldCurve curve,
                                              List<DoublesPair> buckets,
                                              List<Double> shifts,
                                              ShiftType shiftType) {
    return withBucketedShifts(curve, buckets, shifts, shiftType, BUCKETED_SHIFT_NAME);
  }

  /**
   * Performs bucketed shifts on curves. The buckets need not be continuous; if they are not,
   * then the curve is unchanged between the two times. The shifts include the lower time but
   * exclude the upper, and are applied as a step function (i.e. constant over the bucket).
   * The units of time of the buckets are years.
   * <p>
   * If the {@link ShiftType} is absolute, the shift is added to the curve; a shift of 0.0001
   * from one year to two years results in the curve being shifted upwards by one basis point
   * from the one year point to the two year point. If this shift is relative, the yields are
   * multiplied by the shift amount.
   * <p>
   * The original curve is unchanged and a new curve is returned.
   * @param curve The original curve, not null
   * @param buckets The buckets, not null
   * @param shifts The shifts, not null. There must be one shift per bucket.
   * @param shiftType The shift type, not null
   * @param nameSuffix Suffix to add to the name of the shifted curve
   * @return A new curve with bucketed shifts applied
   */
  public static YieldCurve withBucketedShifts(YieldCurve curve,
                                              List<DoublesPair> buckets,
                                              List<Double> shifts,
                                              ShiftType shiftType,
                                              String nameSuffix) {
    ArgumentChecker.notNull(curve, "curve");
    ArgumentChecker.noNulls(buckets, "buckets");
    ArgumentChecker.noNulls(shifts, "shifts");
    ArgumentChecker.isTrue(buckets.size() == shifts.size(), "must have one shift per bucket");
    ArgumentChecker.notNull(shiftType, "shift type");
    ArgumentChecker.notNull(nameSuffix, "nameSuffix");

    String newName = curve.getName() + nameSuffix;
    DoublesCurve underlyingCurve = curve.getCurve();
    if (buckets.isEmpty()) {
      return new YieldCurve(
          newName,
          SpreadDoublesCurve.from(ADD_SPREAD, newName, underlyingCurve, ConstantDoublesCurve.from(0)));
    }
    List<DoublesPair> stepCurvePoints = new ArrayList<>();
    Iterator<DoublesPair> iterBuckets = buckets.iterator();
    Iterator<Double> iterShifts = shifts.iterator();
    DoublesPair oldPair = iterBuckets.next();
    double shift = iterShifts.next();
    Interpolator1D stepInterpolator =
        CombinedInterpolatorExtrapolatorFactory.getInterpolator(
            Interpolator1DFactory.STEP,
            Interpolator1DFactory.FLAT_EXTRAPOLATOR);
    switch (shiftType) {
      case ABSOLUTE: {
        if (oldPair.getFirstDouble() >= 0 && Double.compare(0, oldPair.getFirstDouble()) != 0) {
          stepCurvePoints.add(DoublesPair.of(0., 0.));
        }
        stepCurvePoints.add(DoublesPair.of(oldPair.getFirstDouble(), shift));
        while (iterBuckets.hasNext()) {
          DoublesPair pair = iterBuckets.next();
          shift = iterShifts.next();
          if (Double.compare(pair.getFirstDouble(), oldPair.getSecondDouble()) != 0) {
            stepCurvePoints.add(DoublesPair.of(oldPair.getSecondDouble(), 0));
            stepCurvePoints.add(DoublesPair.of(pair.getFirstDouble(), shift));
          } else {
            stepCurvePoints.add(DoublesPair.of(oldPair.getSecondDouble(), shift));
          }
          oldPair = pair;
        }
        stepCurvePoints.add(DoublesPair.of(oldPair.getSecondDouble(), 0));
        DoublesCurve spreadCurve = InterpolatedDoublesCurve.from(stepCurvePoints, stepInterpolator);
        return new YieldCurve(newName, SpreadDoublesCurve.from(ADD_SPREAD, newName, underlyingCurve, spreadCurve));
      } case RELATIVE: {
        if (oldPair.getFirstDouble() >= 0 && Double.compare(0, oldPair.getFirstDouble()) != 0) {
          stepCurvePoints.add(DoublesPair.of(0., 1.));
        }
        stepCurvePoints.add(DoublesPair.of(oldPair.getFirstDouble(), 1 + shift));
        while (iterBuckets.hasNext()) {
          DoublesPair pair = iterBuckets.next();
          shift = iterShifts.next();
          if (Double.compare(pair.getFirstDouble(), oldPair.getSecondDouble()) != 0) {
            stepCurvePoints.add(DoublesPair.of(oldPair.getSecondDouble(), 1));
            stepCurvePoints.add(DoublesPair.of(pair.getFirstDouble(), 1 + shift));
          } else {
            stepCurvePoints.add(DoublesPair.of(oldPair.getSecondDouble(), 1 + shift));
          }
          oldPair = pair;
        }
        stepCurvePoints.add(DoublesPair.of(oldPair.getSecondDouble(), 1));
        DoublesCurve spreadCurve = InterpolatedDoublesCurve.from(stepCurvePoints, stepInterpolator);
        return new YieldCurve(newName, SpreadDoublesCurve.from(MULTIPLY_SPREAD, newName, underlyingCurve, spreadCurve));
      } default:
        throw new IllegalArgumentException("Cannot handle curve shift type " + shiftType + " for bucketed shifts");
    }
  }

  /**
   * Performs point shifts on curves. The units of time are years.
   * <p>
   * If the {@link ShiftType} is absolute, the shift is added to the curve; a shift of 0.0001
   * results in the curve being shifted upwards by one basis point at the time point. If this
   * shift is relative, the yields are multiplied by the shift amount.
   * <p>
   * The original curve is unchanged and a new curve is returned.
   * <p>
   * This method only works for interpolated yield curves.
   * @param curve The original curve, not null
   * @param t The times, not null
   * @param shifts The shifts, not null. There must be one shift per time.
   * @param shiftType The shift type, not null
   * @return A new curve with point shifts applied
   * @throws IllegalArgumentException if the curve is not an interpolated curve
   */
  public static YieldCurve withPointShifts(YieldCurve curve, List<Double> t, List<Double> shifts, ShiftType shiftType) {
    return withPointShifts(curve, t, shifts, shiftType, POINT_SHIFT_NAME);
  }

  /**
   * Performs point shifts on curves. The units of time are years.
   * <p>
   * If the {@link ShiftType} is absolute, the shift is added to the curve; a shift of 0.0001
   * results in the curve being shifted upwards by one basis point at the time point. If this
   * shift is relative, the yields are multiplied by the shift amount.
   * <p>
   * The original curve is unchanged and a new curve is returned.
   * <p>
   * This method only works for interpolated yield curves.
   * @param curve The original curve, not null
   * @param t The times, not null
   * @param shifts The shifts, not null. There must be one shift per time.
   * @param shiftType The shift type, not null
   * @param nameSuffix Suffix to add to the name of the shifted curve
   * @return A new curve with point shifts applied
   * @throws IllegalArgumentException if the curve is not an interpolated curve
   */
  public static YieldCurve withPointShifts(YieldCurve curve,
                                           List<Double> t,
                                           List<Double> shifts,
                                           ShiftType shiftType,
                                           String nameSuffix) {
    ArgumentChecker.notNull(curve, "curve");
    ArgumentChecker.noNulls(t, "times");
    ArgumentChecker.noNulls(shifts, "shifts");
    ArgumentChecker.notNull(nameSuffix, "nameSuffix");
    ArgumentChecker.isTrue(t.size() == shifts.size(), "must have one shift per point");
    ArgumentChecker.isTrue(
        curve.getCurve() instanceof InterpolatedDoublesCurve,
        "Can only perform points shifts on interpolated curves");
    ArgumentChecker.notNull(shiftType, "shift type");
    String newName = curve.getName() + nameSuffix;
    int n = t.size();
    double[] tArray = ArrayUtils.toPrimitive(t.toArray(new Double[n]));
    double[] shiftArray = ArrayUtils.toPrimitive(shifts.toArray(new Double[n]));
    switch (shiftType) {
      case ABSOLUTE: {
        return new YieldCurve(
            newName,
            CurveShiftFunctionFactory.getShiftedCurve(curve.getCurve(), tArray, shiftArray, newName));
      } case RELATIVE: {
        InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) curve.getCurve();
        return new YieldCurve(newName, getRelativeShiftedCurve(interpolatedCurve, tArray, shiftArray, newName));
      } default:
        throw new IllegalArgumentException("Cannot handle curve shift type " + shiftType + " for point shifts");
    }
  }

  /**
   * Performs relative shifts on a yield curve
   * @param curve The curve
   * @param t The times
   * @param yShift The shifts
   * @param newName The new curve name
   * @return A shifted curve
   */
  //TODO this should be moved into a separate CurveShiftFunction
  private static InterpolatedDoublesCurve getRelativeShiftedCurve(InterpolatedDoublesCurve curve,
                                                                  double[] t,
                                                                  double[] yShift,
                                                                  String newName) {
    if (t.length == 0) {
      return InterpolatedDoublesCurve.from(
          curve.getXDataAsPrimitive(),
          curve.getYDataAsPrimitive(),
          curve.getInterpolator(),
          newName);
    }
    List<Double> newX = new ArrayList<>(Arrays.asList(curve.getXData()));
    List<Double> newY = new ArrayList<>(Arrays.asList(curve.getYData()));
    for (int i = 0; i < t.length; i++) {
      int index = newX.indexOf(t[i]);
      if (index >= 0) {
        newY.set(index, newY.get(index) * (1 + yShift[i]));
      } else {
        newX.add(t[i]);
        newY.add(curve.getYValue(t[i]) * (1 + yShift[i]));
      }
    }
    return InterpolatedDoublesCurve.from(newX, newY, curve.getInterpolator(), newName);
  }
}
