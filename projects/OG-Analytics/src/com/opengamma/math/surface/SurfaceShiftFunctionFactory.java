/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains methods for performing shifts on {@link Surface} without needing to know the exact type of the surface.
 */
public class SurfaceShiftFunctionFactory {
  /** Shift function for {@link ConstantDoublesSurface} */
  public static final ConstantSurfaceShiftFunction CONSTANT = new ConstantSurfaceShiftFunction();
  /** Shift function for {@link FunctionalDoublesSurface} */
  public static final FunctionalSurfaceShiftFunction FUNCTIONAL = new FunctionalSurfaceShiftFunction();
  /** Shift function for {@link InterpolatedDoublesSurface}  */
  public static final InterpolatedSurfaceShiftFunction INTERPOLATED = new InterpolatedSurfaceShiftFunction();
  /** Shift function for {@link InterpolatedFromCurvesDoublesSurface} */
  public static final InterpolatedFromCurvesSurfaceShiftFunction INTERPOLATED_FROM_CURVES = new InterpolatedFromCurvesSurfaceShiftFunction();
  /** Shift function for {@link NodalDoublesSurface} */
  public static final NodalSurfaceShiftFunction NODAL = new NodalSurfaceShiftFunction();
  private static final Map<Class<?>, SurfaceShiftFunction<?>> s_instances = new HashMap<Class<?>, SurfaceShiftFunction<?>>();

  static {
    s_instances.put(ConstantSurfaceShiftFunction.class, CONSTANT);
    s_instances.put(FunctionalSurfaceShiftFunction.class, FUNCTIONAL);
    s_instances.put(InterpolatedSurfaceShiftFunction.class, INTERPOLATED);
    s_instances.put(InterpolatedFromCurvesSurfaceShiftFunction.class, INTERPOLATED_FROM_CURVES);
    s_instances.put(NodalSurfaceShiftFunction.class, NODAL);
  }

  /**
   * Gets the function for a class type.
   * @param clazz The class
   * @return The function
   * @throws IllegalArgumentException If the function is not one of the static instances
   */
  public static SurfaceShiftFunction<?> getFunction(final Class<?> clazz) {
    final SurfaceShiftFunction<?> f = s_instances.get(clazz);
    if (f == null) {
      throw new IllegalArgumentException("Could not get function for " + clazz.getName());
    }
    return f;
  }

  /**
   * For a surface Surface<Double, Double, Double>, return a parallel-shifted surface.
   * @param surface The original surface
   * @param shift The shift
   * @return A shifted surface with automatically-generated name
   * @throws IllegalArgumentException If the surface type is not constant, functional, interpolated, nodal or spread
   */
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

  /**
   * For a surface Surface<Double, Double, Double>, return a surface shifted at one point.
   * @param surface The original surface
   * @param x The <i>x</i> value of the shift
   * @param y The <i>y</i> value of the shift
   * @param shift The shift
   * @return A shifted surface with automatically-generated name
   * @throws IllegalArgumentException If the surface type is not constant, functional, interpolated, nodal or spread
   */
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

  /**
   * For a surface Surface<Double, Double, Double>, return a parallel-shifted surface.
   * @param surface The original surface
   * @param x An array of <i>x</i> values to shift 
   * @param y An array of <i>y</i> values to shift 
   * @param shift The shifts
   * @return A shifted surface with an automatically-generated name
   * @throws IllegalArgumentException If the surface type is not constant, functional, interpolated, nodal or spread
   */
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

  /**
   * For a surface Surface<Double, Double, Double>, return a parallel-shifted surface.
   * @param surface The original surface
   * @param shift The shift
   * @param newName The name of the shifted surface
   * @return A shifted surface 
   * @throws IllegalArgumentException If the surface type is not constant, functional, interpolated, nodal or spread
   */
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

  /**
   * For a surface Surface<Double, Double, Double>, return a surface shifted at one point.
   * @param surface The original surface
   * @param x The <i>x</i> value of the shift
   * @param y The <i>y</i> value of the shift
   * @param shift The shift
   * @param newName The name of the new surface
   * @return A shifted surface
   * @throws IllegalArgumentException If the surface type is not constant, functional, interpolated, nodal or spread
   */
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

  /**
   * For a surface Surface<Double, Double, Double>, return a parallel-shifted surface.
   * @param surface The original surface
   * @param x An array of <i>x</i> values to shift 
   * @param y An array of <i>y</i> values to shift 
   * @param shift The shifts
   * @param newName The name of the shifted surface
   * @return A shifted surface 
   * @throws IllegalArgumentException If the surface type is not constant, functional, interpolated, nodal or spread
   */
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
