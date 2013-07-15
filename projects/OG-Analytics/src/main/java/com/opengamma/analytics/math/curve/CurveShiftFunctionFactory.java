/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.curve;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains methods for performing shifts on {@link Curve} without needing to know the exact type of the curve.
 */
public class CurveShiftFunctionFactory {
  /** Shift function for {@link ConstantDoublesCurve} */
  public static final ConstantCurveShiftFunction CONSTANT = new ConstantCurveShiftFunction();
  /** Shift function for {@link FunctionalDoublesCurve} */
  public static final FunctionalCurveShiftFunction FUNCTIONAL = new FunctionalCurveShiftFunction();
  /** Shift function for {@link InterpolatedDoublesCurve} */
  public static final InterpolatedCurveShiftFunction INTERPOLATED = new InterpolatedCurveShiftFunction();
  /** Shift function for {@link SpreadDoublesCurve} */
  public static final SpreadCurveShiftFunction SPREAD = new SpreadCurveShiftFunction();
  private static final Map<Class<?>, CurveShiftFunction<?>> s_instances = new HashMap<>();

  static {
    s_instances.put(ConstantCurveShiftFunction.class, CONSTANT);
    s_instances.put(FunctionalCurveShiftFunction.class, FUNCTIONAL);
    s_instances.put(InterpolatedCurveShiftFunction.class, INTERPOLATED);
    s_instances.put(SpreadCurveShiftFunction.class, SPREAD);
  }

  /**
   * Gets the function for a class type.
   * @param clazz The class
   * @return The function
   * @throws IllegalArgumentException If the function is not one of the static instances
   */
  public static CurveShiftFunction<?> getFunction(final Class<?> clazz) {
    final CurveShiftFunction<?> f = s_instances.get(clazz);
    if (f == null) {
      throw new IllegalArgumentException("Could not get function for " + clazz.getName());
    }
    return f;
  }

  /**
   * For a curve Curve<Double, Double>, return a parallel-shifted curve.
   * @param curve The original curve
   * @param shift The shift
   * @return A shifted curve with automatically-generated name
   * @throws IllegalArgumentException If the curve type is not constant, functional, interpolated, nodal or spread
   */
  public static DoublesCurve getShiftedCurve(final Curve<Double, Double> curve, final double shift) {
    if (curve instanceof ConstantDoublesCurve) {
      return CONSTANT.evaluate((ConstantDoublesCurve) curve, shift);
    }
    if (curve instanceof FunctionalDoublesCurve) {
      return FUNCTIONAL.evaluate((FunctionalDoublesCurve) curve, shift);
    }
    if (curve instanceof InterpolatedDoublesCurve) {
      return INTERPOLATED.evaluate((InterpolatedDoublesCurve) curve, shift);
    }
    if (curve instanceof SpreadDoublesCurve) {
      return SPREAD.evaluate((SpreadDoublesCurve) curve, shift);
    }
    throw new IllegalArgumentException("Do not have a curve shift function for curve " + curve.getClass());
  }

  /**
   * For a curve Curve<Double, Double>, return a curve shifted at one point.
   * @param curve The original curve
   * @param x The <i>x</i> value of the shift
   * @param shift The shift
   * @return A shifted curve with automatically-generated name
   * @throws IllegalArgumentException If the curve type is not constant, functional, interpolated, nodal or spread
   */
  public static DoublesCurve getShiftedCurve(final Curve<Double, Double> curve, final double x, final double shift) {
    if (curve instanceof ConstantDoublesCurve) {
      throw new UnsupportedOperationException("Cannot shift a single point on a constant curve");
    }
    if (curve instanceof FunctionalDoublesCurve) {
      throw new UnsupportedOperationException("Cannot shift a single point on a functional curve");
    }
    if (curve instanceof InterpolatedDoublesCurve) {
      return INTERPOLATED.evaluate((InterpolatedDoublesCurve) curve, x, shift);
    }
    if (curve instanceof SpreadDoublesCurve) {
      throw new UnsupportedOperationException("Cannot shift a single point on a spread curve");
    }
    throw new IllegalArgumentException("Do not have a curve shift function for curve " + curve.getClass());
  }

  /**
   * For a curve Curve<Double, Double>, return a parallel-shifted curve.
   * @param curve The original curve
   * @param x An array of <i>x</i> values to shift
   * @param y The shifts
   * @return A shifted curve with automatically-generated name
   * @throws IllegalArgumentException If the curve type is not constant, functional, interpolated, nodal or spread
   */
  public static DoublesCurve getShiftedCurve(final Curve<Double, Double> curve, final double[] x, final double[] y) {
    if (curve instanceof ConstantDoublesCurve) {
      throw new UnsupportedOperationException("Cannot shift a single point on a constant curve");
    }
    if (curve instanceof FunctionalDoublesCurve) {
      throw new UnsupportedOperationException("Cannot shift a single point on a functional curve");
    }
    if (curve instanceof InterpolatedDoublesCurve) {
      return INTERPOLATED.evaluate((InterpolatedDoublesCurve) curve, x, y);
    }
    if (curve instanceof SpreadDoublesCurve) {
      throw new UnsupportedOperationException("Cannot shift a single point on a spread curve");
    }
    throw new IllegalArgumentException("Do not have a curve shift function for curve " + curve.getClass());
  }

  /**
   * For a curve Curve<Double, Double>, return a parallel-shifted curve.
   * @param curve The original curve
   * @param shift The shift
   * @param newName The name of the shifted curve
   * @return A shifted curve
   * @throws IllegalArgumentException If the curve type is not constant, functional, interpolated, nodal or spread
   */
  public static DoublesCurve getShiftedCurve(final Curve<Double, Double> curve, final double shift, final String newName) {
    if (curve instanceof ConstantDoublesCurve) {
      return CONSTANT.evaluate((ConstantDoublesCurve) curve, shift, newName);
    }
    if (curve instanceof FunctionalDoublesCurve) {
      return FUNCTIONAL.evaluate((FunctionalDoublesCurve) curve, shift, newName);
    }
    if (curve instanceof InterpolatedDoublesCurve) {
      return INTERPOLATED.evaluate((InterpolatedDoublesCurve) curve, shift, newName);
    }
    if (curve instanceof SpreadDoublesCurve) {
      return SPREAD.evaluate((SpreadDoublesCurve) curve, shift, newName);
    }
    throw new IllegalArgumentException("Do not have a curve shift function for curve " + curve.getClass());
  }

  /**
   * For a curve Curve<Double, Double>, return a curve shifted at one point.
   * @param curve The original curve
   * @param x The <i>x</i> value of the shift
   * @param shift The shift
   * @param newName The name of the shifted curve
   * @return A shifted curve
   * @throws IllegalArgumentException If the curve type is not constant, functional, interpolated, nodal or spread
   */
  public static DoublesCurve getShiftedCurve(final Curve<Double, Double> curve, final double x, final double shift, final String newName) {
    if (curve instanceof ConstantDoublesCurve) {
      throw new UnsupportedOperationException("Cannot shift a single point on a constant curve");
    }
    if (curve instanceof FunctionalDoublesCurve) {
      throw new UnsupportedOperationException("Cannot shift a single point on a functional curve");
    }
    if (curve instanceof InterpolatedDoublesCurve) {
      return INTERPOLATED.evaluate((InterpolatedDoublesCurve) curve, x, shift, newName);
    }
    if (curve instanceof SpreadDoublesCurve) {
      throw new UnsupportedOperationException("Cannot shift a single point on a spread curve");
    }
    throw new IllegalArgumentException("Do not have a curve shift function for curve " + curve.getClass());
  }

  /**
   * For a curve Curve<Double, Double>, return a parallel-shifted curve.
   * @param curve The original curve
   * @param x An array of <i>x</i> values to shift
   * @param y The shifts
   * @param newName The name of the shifted curve
   * @return A shifted curve
   * @throws IllegalArgumentException If the curve type is not constant, functional, interpolated, nodal or spread
   */
  public static DoublesCurve getShiftedCurve(final Curve<Double, Double> curve, final double[] x, final double[] y, final String newName) {
    if (curve instanceof ConstantDoublesCurve) {
      throw new UnsupportedOperationException("Cannot shift a single point on a constant curve");
    }
    if (curve instanceof FunctionalDoublesCurve) {
      throw new UnsupportedOperationException("Cannot shift a single point on a functional curve");
    }
    if (curve instanceof InterpolatedDoublesCurve) {
      return INTERPOLATED.evaluate((InterpolatedDoublesCurve) curve, x, y, newName);
    }
    if (curve instanceof SpreadDoublesCurve) {
      throw new UnsupportedOperationException("Cannot shift a single point on a spread curve");
    }
    throw new IllegalArgumentException("Do not have a curve shift function for curve " + curve.getClass());
  }
}
