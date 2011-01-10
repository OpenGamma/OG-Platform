/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.curve;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class CurveShiftFunctionFactory {
  /** */
  public static final ConstantCurveShiftFunction CONSTANT = new ConstantCurveShiftFunction();
  /** */
  public static final FunctionalCurveShiftFunction FUNCTIONAL = new FunctionalCurveShiftFunction();
  /** */
  public static final InterpolatedCurveShiftFunction INTERPOLATED = new InterpolatedCurveShiftFunction();
  /** */
  public static final NodalCurveShiftFunction NODAL = new NodalCurveShiftFunction();
  /** */
  public static final SpreadCurveShiftFunction SPREAD = new SpreadCurveShiftFunction();
  private static final Map<Class<?>, CurveShiftFunction<?>> s_instances = new HashMap<Class<?>, CurveShiftFunction<?>>();

  static {
    s_instances.put(ConstantCurveShiftFunction.class, CONSTANT);
    s_instances.put(FunctionalCurveShiftFunction.class, FUNCTIONAL);
    s_instances.put(InterpolatedCurveShiftFunction.class, INTERPOLATED);
    s_instances.put(NodalCurveShiftFunction.class, NODAL);
    s_instances.put(SpreadCurveShiftFunction.class, SPREAD);
  }

  public static CurveShiftFunction<?> getFunction(final Class<?> clazz) {
    final CurveShiftFunction<?> f = s_instances.get(clazz);
    if (f == null) {
      throw new IllegalArgumentException("Could not get function for " + clazz.getName());
    }
    return f;
  }

  public static Curve<Double, Double> getShiftedCurve(final Curve<Double, Double> curve, final double shift) {
    if (curve instanceof ConstantDoublesCurve) {
      return CONSTANT.evaluate((ConstantDoublesCurve) curve, shift);
    }
    if (curve instanceof FunctionalDoublesCurve) {
      return FUNCTIONAL.evaluate((FunctionalDoublesCurve) curve, shift);
    }
    if (curve instanceof InterpolatedDoublesCurve) {
      return INTERPOLATED.evaluate((InterpolatedDoublesCurve) curve, shift);
    }
    if (curve instanceof NodalDoublesCurve) {
      return NODAL.evaluate((NodalDoublesCurve) curve, shift);
    }
    if (curve instanceof SpreadDoublesCurve) {
      return SPREAD.evaluate((SpreadDoublesCurve) curve, shift);
    }
    throw new IllegalArgumentException("Do not have a curve shift function for curve " + curve.getClass());
  }

  public static Curve<Double, Double> getShiftedCurve(final Curve<Double, Double> curve, final double x, final double shift) {
    if (curve instanceof ConstantDoublesCurve) {
      return CONSTANT.evaluate((ConstantDoublesCurve) curve, x, shift);
    }
    if (curve instanceof FunctionalDoublesCurve) {
      return FUNCTIONAL.evaluate((FunctionalDoublesCurve) curve, x, shift);
    }
    if (curve instanceof InterpolatedDoublesCurve) {
      return INTERPOLATED.evaluate((InterpolatedDoublesCurve) curve, x, shift);
    }
    if (curve instanceof NodalDoublesCurve) {
      return NODAL.evaluate((NodalDoublesCurve) curve, x, shift);
    }
    if (curve instanceof SpreadDoublesCurve) {
      return SPREAD.evaluate((SpreadDoublesCurve) curve, x, shift);
    }
    throw new IllegalArgumentException("Do not have a curve shift function for curve " + curve.getClass());
  }

  public static Curve<Double, Double> getShiftedCurve(final Curve<Double, Double> curve, final double[] x, final double[] y) {
    if (curve instanceof ConstantDoublesCurve) {
      return CONSTANT.evaluate((ConstantDoublesCurve) curve, x, y);
    }
    if (curve instanceof FunctionalDoublesCurve) {
      return FUNCTIONAL.evaluate((FunctionalDoublesCurve) curve, x, y);
    }
    if (curve instanceof InterpolatedDoublesCurve) {
      return INTERPOLATED.evaluate((InterpolatedDoublesCurve) curve, x, y);
    }
    if (curve instanceof NodalDoublesCurve) {
      return NODAL.evaluate((NodalDoublesCurve) curve, x, y);
    }
    if (curve instanceof SpreadDoublesCurve) {
      return SPREAD.evaluate((SpreadDoublesCurve) curve, x, y);
    }
    throw new IllegalArgumentException("Do not have a curve shift function for curve " + curve.getClass());
  }

  public static Curve<Double, Double> getShiftedCurve(final Curve<Double, Double> curve, final double shift, final String newName) {
    if (curve instanceof ConstantDoublesCurve) {
      return CONSTANT.evaluate((ConstantDoublesCurve) curve, shift, newName);
    }
    if (curve instanceof FunctionalDoublesCurve) {
      return FUNCTIONAL.evaluate((FunctionalDoublesCurve) curve, shift, newName);
    }
    if (curve instanceof InterpolatedDoublesCurve) {
      return INTERPOLATED.evaluate((InterpolatedDoublesCurve) curve, shift, newName);
    }
    if (curve instanceof NodalDoublesCurve) {
      return NODAL.evaluate((NodalDoublesCurve) curve, shift, newName);
    }
    if (curve instanceof SpreadDoublesCurve) {
      return SPREAD.evaluate((SpreadDoublesCurve) curve, shift, newName);
    }
    throw new IllegalArgumentException("Do not have a curve shift function for curve " + curve.getClass());
  }

  public static Curve<Double, Double> getShiftedCurve(final Curve<Double, Double> curve, final double x, final double shift, final String newName) {
    if (curve instanceof ConstantDoublesCurve) {
      return CONSTANT.evaluate((ConstantDoublesCurve) curve, x, shift, newName);
    }
    if (curve instanceof FunctionalDoublesCurve) {
      return FUNCTIONAL.evaluate((FunctionalDoublesCurve) curve, x, shift, newName);
    }
    if (curve instanceof InterpolatedDoublesCurve) {
      return INTERPOLATED.evaluate((InterpolatedDoublesCurve) curve, x, shift, newName);
    }
    if (curve instanceof NodalDoublesCurve) {
      return NODAL.evaluate((NodalDoublesCurve) curve, x, shift, newName);
    }
    if (curve instanceof SpreadDoublesCurve) {
      return SPREAD.evaluate((SpreadDoublesCurve) curve, x, shift, newName);
    }
    throw new IllegalArgumentException("Do not have a curve shift function for curve " + curve.getClass());
  }

  public static Curve<Double, Double> getShiftedCurve(final Curve<Double, Double> curve, final double[] x, final double[] y, final String newName) {
    if (curve instanceof ConstantDoublesCurve) {
      return CONSTANT.evaluate((ConstantDoublesCurve) curve, x, y, newName);
    }
    if (curve instanceof FunctionalDoublesCurve) {
      return FUNCTIONAL.evaluate((FunctionalDoublesCurve) curve, x, y, newName);
    }
    if (curve instanceof InterpolatedDoublesCurve) {
      return INTERPOLATED.evaluate((InterpolatedDoublesCurve) curve, x, y, newName);
    }
    if (curve instanceof NodalDoublesCurve) {
      return NODAL.evaluate((NodalDoublesCurve) curve, x, y, newName);
    }
    if (curve instanceof SpreadDoublesCurve) {
      return SPREAD.evaluate((SpreadDoublesCurve) curve, x, y, newName);
    }
    throw new IllegalArgumentException("Do not have a curve shift function for curve " + curve.getClass());
  }
}
