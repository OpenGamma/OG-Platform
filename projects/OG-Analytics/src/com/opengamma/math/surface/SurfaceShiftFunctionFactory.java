/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 */
public class SurfaceShiftFunctionFactory {
  /** */
  public static final ConstantSurfaceShiftFunction CONSTANT = new ConstantSurfaceShiftFunction();
  /** */
  public static final FunctionalSurfaceShiftFunction FUNCTIONAL = new FunctionalSurfaceShiftFunction();
  /** */
  public static final InterpolatedSurfaceShiftFunction INTERPOLATED = new InterpolatedSurfaceShiftFunction();
  /** */
  public static final InterpolatedFromCurvesSurfaceShiftFunction INTERPOLATED_FROM_CURVES = new InterpolatedFromCurvesSurfaceShiftFunction();
  /** */
  public static final NodalSurfaceShiftFunction NODAL = new NodalSurfaceShiftFunction();
  private static final Map<Class<?>, SurfaceShiftFunction<?>> s_instances = new HashMap<Class<?>, SurfaceShiftFunction<?>>();

  static {
    s_instances.put(ConstantSurfaceShiftFunction.class, CONSTANT);
    s_instances.put(FunctionalSurfaceShiftFunction.class, FUNCTIONAL);
    s_instances.put(InterpolatedSurfaceShiftFunction.class, INTERPOLATED);
    s_instances.put(InterpolatedFromCurvesSurfaceShiftFunction.class, INTERPOLATED_FROM_CURVES);
    s_instances.put(NodalSurfaceShiftFunction.class, NODAL);
  }

  public static SurfaceShiftFunction<?> getFunction(final Class<?> clazz) {
    final SurfaceShiftFunction<?> f = s_instances.get(clazz);
    if (f == null) {
      throw new IllegalArgumentException("Could not get function for " + clazz.getName());
    }
    return f;
  }

  public static Surface<Double, Double, Double> getShiftedSurface(final Surface<Double, Double, Double> surface, final double shift) {
    if (surface instanceof ConstantDoublesSurface) {
      return CONSTANT.evaluate((ConstantDoublesSurface) surface, shift);
    }
    if (surface instanceof FunctionalDoublesSurface) {
      return FUNCTIONAL.evaluate((FunctionalDoublesSurface) surface, shift);
    }
    if (surface instanceof InterpolatedDoublesSurface) {
      return INTERPOLATED.evaluate((InterpolatedDoublesSurface) surface, shift);
    }
    if (surface instanceof InterpolatedFromCurvesDoublesSurface) {
      return INTERPOLATED_FROM_CURVES.evaluate((InterpolatedFromCurvesDoublesSurface) surface, shift);
    }
    if (surface instanceof NodalDoublesSurface) {
      return NODAL.evaluate((NodalDoublesSurface) surface, shift);
    }
    throw new IllegalArgumentException("Do not have a surface shift function for surface " + surface.getClass());
  }

  public static Surface<Double, Double, Double> getShiftedSurface(final Surface<Double, Double, Double> surface, final double x, final double y, final double shift) {
    if (surface instanceof ConstantDoublesSurface) {
      return CONSTANT.evaluate((ConstantDoublesSurface) surface, x, y, shift);
    }
    if (surface instanceof FunctionalDoublesSurface) {
      return FUNCTIONAL.evaluate((FunctionalDoublesSurface) surface, x, y, shift);
    }
    if (surface instanceof InterpolatedDoublesSurface) {
      return INTERPOLATED.evaluate((InterpolatedDoublesSurface) surface, x, y, shift);
    }
    if (surface instanceof InterpolatedFromCurvesDoublesSurface) {
      return INTERPOLATED_FROM_CURVES.evaluate((InterpolatedFromCurvesDoublesSurface) surface, x, y, shift);
    }
    if (surface instanceof NodalDoublesSurface) {
      return NODAL.evaluate((NodalDoublesSurface) surface, x, y, shift);
    }
    throw new IllegalArgumentException("Do not have a surface shift function for surface " + surface.getClass());
  }

  public static Surface<Double, Double, Double> getShiftedSurface(final Surface<Double, Double, Double> surface, final double[] x, final double[] y, final double[] shift) {
    if (surface instanceof ConstantDoublesSurface) {
      return CONSTANT.evaluate((ConstantDoublesSurface) surface, x, y, shift);
    }
    if (surface instanceof FunctionalDoublesSurface) {
      return FUNCTIONAL.evaluate((FunctionalDoublesSurface) surface, x, y, shift);
    }
    if (surface instanceof InterpolatedDoublesSurface) {
      return INTERPOLATED.evaluate((InterpolatedDoublesSurface) surface, x, y, shift);
    }
    if (surface instanceof InterpolatedFromCurvesDoublesSurface) {
      return INTERPOLATED_FROM_CURVES.evaluate((InterpolatedFromCurvesDoublesSurface) surface, x, y, shift);
    }
    if (surface instanceof NodalDoublesSurface) {
      return NODAL.evaluate((NodalDoublesSurface) surface, x, y, shift);
    }
    throw new IllegalArgumentException("Do not have a surface shift function for surface " + surface.getClass());
  }

  public static Surface<Double, Double, Double> getShiftedSurface(final Surface<Double, Double, Double> surface, final double shift, final String newName) {
    if (surface instanceof ConstantDoublesSurface) {
      return CONSTANT.evaluate((ConstantDoublesSurface) surface, shift, newName);
    }
    if (surface instanceof FunctionalDoublesSurface) {
      return FUNCTIONAL.evaluate((FunctionalDoublesSurface) surface, shift, newName);
    }
    if (surface instanceof InterpolatedDoublesSurface) {
      return INTERPOLATED.evaluate((InterpolatedDoublesSurface) surface, shift, newName);
    }
    if (surface instanceof InterpolatedFromCurvesDoublesSurface) {
      return INTERPOLATED_FROM_CURVES.evaluate((InterpolatedFromCurvesDoublesSurface) surface, shift, newName);
    }
    if (surface instanceof NodalDoublesSurface) {
      return NODAL.evaluate((NodalDoublesSurface) surface, shift, newName);
    }
    throw new IllegalArgumentException("Do not have a surface shift function for surface " + surface.getClass());
  }

  public static Surface<Double, Double, Double> getShiftedSurface(final Surface<Double, Double, Double> surface, final double x, final double y, final double shift, final String newName) {
    if (surface instanceof ConstantDoublesSurface) {
      return CONSTANT.evaluate((ConstantDoublesSurface) surface, x, y, shift, newName);
    }
    if (surface instanceof FunctionalDoublesSurface) {
      return FUNCTIONAL.evaluate((FunctionalDoublesSurface) surface, x, y, shift, newName);
    }
    if (surface instanceof InterpolatedDoublesSurface) {
      return INTERPOLATED.evaluate((InterpolatedDoublesSurface) surface, x, y, shift, newName);
    }
    if (surface instanceof InterpolatedFromCurvesDoublesSurface) {
      return INTERPOLATED_FROM_CURVES.evaluate((InterpolatedFromCurvesDoublesSurface) surface, x, y, shift, newName);
    }
    if (surface instanceof NodalDoublesSurface) {
      return NODAL.evaluate((NodalDoublesSurface) surface, x, y, shift, newName);
    }
    throw new IllegalArgumentException("Do not have a surface shift function for surface " + surface.getClass());
  }

  public static Surface<Double, Double, Double> getShiftedSurface(final Surface<Double, Double, Double> surface, final double[] x, final double[] y, final double[] shift, final String newName) {
    if (surface instanceof ConstantDoublesSurface) {
      return CONSTANT.evaluate((ConstantDoublesSurface) surface, x, y, shift, newName);
    }
    if (surface instanceof FunctionalDoublesSurface) {
      return FUNCTIONAL.evaluate((FunctionalDoublesSurface) surface, x, y, shift, newName);
    }
    if (surface instanceof InterpolatedDoublesSurface) {
      return INTERPOLATED.evaluate((InterpolatedDoublesSurface) surface, x, y, shift, newName);
    }
    if (surface instanceof InterpolatedFromCurvesDoublesSurface) {
      return INTERPOLATED_FROM_CURVES.evaluate((InterpolatedFromCurvesDoublesSurface) surface, x, y, shift, newName);
    }
    if (surface instanceof NodalDoublesSurface) {
      return NODAL.evaluate((NodalDoublesSurface) surface, x, y, shift, newName);
    }
    throw new IllegalArgumentException("Do not have a surface shift function for surface " + surface.getClass());
  }
}
