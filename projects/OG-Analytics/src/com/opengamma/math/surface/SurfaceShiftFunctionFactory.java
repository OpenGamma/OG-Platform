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
  /** Additive shift function for {@link ConstantDoublesSurface} */
  public static final ConstantSurfaceAdditiveShiftFunction CONSTANT_ADDITIVE = new ConstantSurfaceAdditiveShiftFunction();
  /** Multiplicative shift function for {@link ConstantDoublesSurface} */
  public static final ConstantSurfaceMultiplicativeShiftFunction CONSTANT_MULTIPLICATIVE = new ConstantSurfaceMultiplicativeShiftFunction();
  /** Additive shift function for {@link FunctionalDoublesSurface} */
  public static final FunctionalSurfaceAdditiveShiftFunction FUNCTIONAL_ADDITIVE = new FunctionalSurfaceAdditiveShiftFunction();
  /** Multiplicative shift function for {@link FunctionalDoublesSurface} */
  public static final FunctionalSurfaceMultiplicativeShiftFunction FUNCTIONAL_MULTIPLICATIVE = new FunctionalSurfaceMultiplicativeShiftFunction();
  /** Additive shift function for {@link InterpolatedDoublesSurface}  */
  public static final InterpolatedSurfaceAdditiveShiftFunction INTERPOLATED_ADDITIVE = new InterpolatedSurfaceAdditiveShiftFunction();
  /** Multiplicative shift function for {@link InterpolatedDoublesSurface} */
  public static final InterpolatedSurfaceMultiplicativeShiftFunction INTERPOLATED_MULTIPLICATIVE = new InterpolatedSurfaceMultiplicativeShiftFunction();
  /** Additive shift function for {@link InterpolatedFromCurvesDoublesSurface} */
  public static final InterpolatedFromCurvesSurfaceAdditiveShiftFunction INTERPOLATED_FROM_CURVES_ADDITIVE = new InterpolatedFromCurvesSurfaceAdditiveShiftFunction();
  /** Additive shift function for {@link NodalDoublesSurface} */
  public static final NodalSurfaceAdditiveShiftFunction NODAL_ADDITIVE = new NodalSurfaceAdditiveShiftFunction();
  /** Multiplicative shift function for {@link NodalDoublesSurface} */
  public static final NodalSurfaceMultiplicativeShiftFunction NODAL_MULTIPLICATIVE = new NodalSurfaceMultiplicativeShiftFunction();
  private static final Map<Class<?>, SurfaceShiftFunction<?>> s_instances = new HashMap<Class<?>, SurfaceShiftFunction<?>>();

  static {
    s_instances.put(ConstantSurfaceAdditiveShiftFunction.class, CONSTANT_ADDITIVE);
    s_instances.put(ConstantSurfaceMultiplicativeShiftFunction.class, CONSTANT_MULTIPLICATIVE);
    s_instances.put(FunctionalSurfaceAdditiveShiftFunction.class, FUNCTIONAL_ADDITIVE);
    s_instances.put(FunctionalSurfaceMultiplicativeShiftFunction.class, FUNCTIONAL_MULTIPLICATIVE);
    s_instances.put(InterpolatedSurfaceAdditiveShiftFunction.class, INTERPOLATED_ADDITIVE);
    s_instances.put(InterpolatedSurfaceMultiplicativeShiftFunction.class, INTERPOLATED_MULTIPLICATIVE);
    s_instances.put(InterpolatedFromCurvesSurfaceAdditiveShiftFunction.class, INTERPOLATED_FROM_CURVES_ADDITIVE);
    s_instances.put(NodalSurfaceAdditiveShiftFunction.class, NODAL_ADDITIVE);
    s_instances.put(NodalSurfaceMultiplicativeShiftFunction.class, NODAL_MULTIPLICATIVE);
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
   * @param useAdditive true if the shift is additive, false if the shift is multiplicative (i.e. a percentage shift)
   * @return A shifted surface with automatically-generated name
   * @throws IllegalArgumentException If the surface type is not constant, functional, interpolated, nodal or spread
   */
  public static Surface<Double, Double, Double> getShiftedSurface(final Surface<Double, Double, Double> surface, final double shift, final boolean useAdditive) {
    if (surface instanceof ConstantDoublesSurface) {
      return useAdditive ? CONSTANT_ADDITIVE.evaluate((ConstantDoublesSurface) surface, shift) : CONSTANT_MULTIPLICATIVE.evaluate((ConstantDoublesSurface) surface, shift);
    }
    if (surface instanceof FunctionalDoublesSurface) {
      return useAdditive ? FUNCTIONAL_ADDITIVE.evaluate((FunctionalDoublesSurface) surface, shift) : FUNCTIONAL_MULTIPLICATIVE.evaluate((FunctionalDoublesSurface) surface, shift);
    }
    if (surface instanceof InterpolatedDoublesSurface) {
      return useAdditive ? INTERPOLATED_ADDITIVE.evaluate((InterpolatedDoublesSurface) surface, shift) : INTERPOLATED_MULTIPLICATIVE.evaluate((InterpolatedDoublesSurface) surface, shift);
    }
    if (surface instanceof InterpolatedFromCurvesDoublesSurface && useAdditive) {
      return INTERPOLATED_FROM_CURVES_ADDITIVE.evaluate((InterpolatedFromCurvesDoublesSurface) surface, shift); 
    }
    if (surface instanceof NodalDoublesSurface) {
      return useAdditive ? NODAL_ADDITIVE.evaluate((NodalDoublesSurface) surface, shift) : NODAL_MULTIPLICATIVE.evaluate((NodalDoublesSurface) surface, shift);
    }
    throw new IllegalArgumentException("Do not have a surface shift function for surface " + surface.getClass());
  }

  /**
   * For a surface Surface<Double, Double, Double>, return a surface shifted at one point.
   * @param surface The original surface
   * @param x The <i>x</i> value of the shift
   * @param y The <i>y</i> value of the shift
   * @param shift The shift
   * @param useAdditive true if the shift is additive, false if the shift is multiplicative (i.e. a percentage shift)
   * @return A shifted surface with automatically-generated name
   * @throws IllegalArgumentException If the surface type is not constant, functional, interpolated, nodal or spread
   */
  public static Surface<Double, Double, Double> getShiftedSurface(final Surface<Double, Double, Double> surface, final double x, final double y, final double shift,
      final boolean useAdditive) {
    if (surface instanceof ConstantDoublesSurface) {
      return useAdditive ? CONSTANT_ADDITIVE.evaluate((ConstantDoublesSurface) surface, x, y, shift) : CONSTANT_MULTIPLICATIVE.evaluate((ConstantDoublesSurface) surface, x, y, shift);
    }
    if (surface instanceof FunctionalDoublesSurface) {
      return useAdditive ? FUNCTIONAL_ADDITIVE.evaluate((FunctionalDoublesSurface) surface, x, y, shift) : FUNCTIONAL_MULTIPLICATIVE.evaluate((FunctionalDoublesSurface) surface, x, y, shift);
    }
    if (surface instanceof InterpolatedDoublesSurface) {
      return useAdditive ? INTERPOLATED_ADDITIVE.evaluate((InterpolatedDoublesSurface) surface, x, y, shift) : INTERPOLATED_MULTIPLICATIVE.evaluate((InterpolatedDoublesSurface) surface, x, y, shift);
    }
    if (surface instanceof InterpolatedFromCurvesDoublesSurface && useAdditive) {
      return INTERPOLATED_FROM_CURVES_ADDITIVE.evaluate((InterpolatedFromCurvesDoublesSurface) surface, x, y, shift); 
    }
    if (surface instanceof NodalDoublesSurface) {
      return useAdditive ? NODAL_ADDITIVE.evaluate((NodalDoublesSurface) surface, x, y, shift) : NODAL_MULTIPLICATIVE.evaluate((NodalDoublesSurface) surface, x, y, shift);
    }
    throw new IllegalArgumentException("Do not have a surface shift function for surface " + surface.getClass());
  }

  /**
   * For a surface Surface<Double, Double, Double>, return a parallel-shifted surface.
   * @param surface The original surface
   * @param x An array of <i>x</i> values to shift 
   * @param y An array of <i>y</i> values to shift 
   * @param shift The shifts
   * @param useAdditive true if the shift is additive, false if the shift is multiplicative (i.e. a percentage shift)
   * @return A shifted surface with an automatically-generated name
   * @throws IllegalArgumentException If the surface type is not constant, functional, interpolated, nodal or spread
   */
  public static Surface<Double, Double, Double> getShiftedSurface(final Surface<Double, Double, Double> surface, final double[] x, final double[] y, final double[] shift,
      final boolean useAdditive) {
    if (surface instanceof ConstantDoublesSurface) {
      return useAdditive ? CONSTANT_ADDITIVE.evaluate((ConstantDoublesSurface) surface, x, y, shift) : CONSTANT_MULTIPLICATIVE.evaluate((ConstantDoublesSurface) surface, x, y, shift);
    }
    if (surface instanceof FunctionalDoublesSurface) {
      return useAdditive ? FUNCTIONAL_ADDITIVE.evaluate((FunctionalDoublesSurface) surface, x, y, shift) : FUNCTIONAL_MULTIPLICATIVE.evaluate((FunctionalDoublesSurface) surface, x, y, shift);
    }
    if (surface instanceof InterpolatedDoublesSurface) {
      return useAdditive ? INTERPOLATED_ADDITIVE.evaluate((InterpolatedDoublesSurface) surface, x, y, shift) : INTERPOLATED_MULTIPLICATIVE.evaluate((InterpolatedDoublesSurface) surface, x, y, shift);
    }
    if (surface instanceof InterpolatedFromCurvesDoublesSurface && useAdditive) {
      return INTERPOLATED_FROM_CURVES_ADDITIVE.evaluate((InterpolatedFromCurvesDoublesSurface) surface, x, y, shift); 
    }
    if (surface instanceof NodalDoublesSurface) {
      return useAdditive ? NODAL_ADDITIVE.evaluate((NodalDoublesSurface) surface, x, y, shift) : NODAL_MULTIPLICATIVE.evaluate((NodalDoublesSurface) surface, x, y, shift);
    }
    throw new IllegalArgumentException("Do not have a surface shift function for surface " + surface.getClass());
  }

  /**
   * For a surface Surface<Double, Double, Double>, return a parallel-shifted surface.
   * @param surface The original surface
   * @param shift The shift
   * @param newName The name of the shifted surface
   * @param useAdditive true if the shift is additive, false if the shift is multiplicative (i.e. a percentage shift)
   * @return A shifted surface 
   * @throws IllegalArgumentException If the surface type is not constant, functional, interpolated, nodal or spread
   */
  public static Surface<Double, Double, Double> getShiftedSurface(final Surface<Double, Double, Double> surface, final double shift, final String newName, final boolean useAdditive) {
    if (surface instanceof ConstantDoublesSurface) {
      return useAdditive ? CONSTANT_ADDITIVE.evaluate((ConstantDoublesSurface) surface, shift, newName) : CONSTANT_MULTIPLICATIVE.evaluate((ConstantDoublesSurface) surface, shift, newName);
    }
    if (surface instanceof FunctionalDoublesSurface) {
      return useAdditive ? FUNCTIONAL_ADDITIVE.evaluate((FunctionalDoublesSurface) surface, shift, newName) : FUNCTIONAL_MULTIPLICATIVE.evaluate((FunctionalDoublesSurface) surface, shift, newName);
    }
    if (surface instanceof InterpolatedDoublesSurface) {
      return useAdditive ? INTERPOLATED_ADDITIVE.evaluate((InterpolatedDoublesSurface) surface, shift, newName) :
        INTERPOLATED_MULTIPLICATIVE.evaluate((InterpolatedDoublesSurface) surface, shift, newName);
    }
    if (surface instanceof InterpolatedFromCurvesDoublesSurface && useAdditive) {
      return INTERPOLATED_FROM_CURVES_ADDITIVE.evaluate((InterpolatedFromCurvesDoublesSurface) surface, shift, newName);
    }
    if (surface instanceof NodalDoublesSurface) {
      return useAdditive ? NODAL_ADDITIVE.evaluate((NodalDoublesSurface) surface, shift, newName) : 
        NODAL_MULTIPLICATIVE.evaluate((NodalDoublesSurface) surface, shift, newName);
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
   * @param useAdditive true if the shift is additive, false if the shift is multiplicative (i.e. a percentage shift)
   * @return A shifted surface
   * @throws IllegalArgumentException If the surface type is not constant, functional, interpolated, nodal or spread
   */
  public static Surface<Double, Double, Double> getShiftedSurface(final Surface<Double, Double, Double> surface, final double x, final double y, final double shift, final String newName,
      final boolean useAdditive) {
    if (surface instanceof ConstantDoublesSurface) {
      return useAdditive ? CONSTANT_ADDITIVE.evaluate((ConstantDoublesSurface) surface, x, y, shift, newName) : 
        CONSTANT_MULTIPLICATIVE.evaluate((ConstantDoublesSurface) surface, x, y, shift, newName);
    }
    if (surface instanceof FunctionalDoublesSurface) {
      return useAdditive ? FUNCTIONAL_ADDITIVE.evaluate((FunctionalDoublesSurface) surface, x, y, shift, newName) :
        FUNCTIONAL_MULTIPLICATIVE.evaluate((FunctionalDoublesSurface) surface, x, y, shift, newName);
    }
    if (surface instanceof InterpolatedDoublesSurface) {
      return useAdditive ? INTERPOLATED_ADDITIVE.evaluate((InterpolatedDoublesSurface) surface, x, y, shift, newName) :
        INTERPOLATED_MULTIPLICATIVE.evaluate((InterpolatedDoublesSurface) surface, x, y, shift, newName);
    }
    if (surface instanceof InterpolatedFromCurvesDoublesSurface && useAdditive) {
      return INTERPOLATED_FROM_CURVES_ADDITIVE.evaluate((InterpolatedFromCurvesDoublesSurface) surface, x, y, shift, newName); 
    }
    if (surface instanceof NodalDoublesSurface) {
      return useAdditive ? NODAL_ADDITIVE.evaluate((NodalDoublesSurface) surface, x, y, shift, newName) : 
        NODAL_MULTIPLICATIVE.evaluate((NodalDoublesSurface) surface, x, y, shift, newName);
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
   * @param useAdditive true if the shift is additive, false if the shift is multiplicative (i.e. a percentage shift)
   * @return A shifted surface 
   * @throws IllegalArgumentException If the surface type is not constant, functional, interpolated, nodal or spread
   */
  public static Surface<Double, Double, Double> getShiftedSurface(final Surface<Double, Double, Double> surface, final double[] x, final double[] y, final double[] shift, final String newName,
      final boolean useAdditive) {
    if (surface instanceof ConstantDoublesSurface) {
      return useAdditive ? CONSTANT_ADDITIVE.evaluate((ConstantDoublesSurface) surface, x, y, shift, newName) : 
        CONSTANT_MULTIPLICATIVE.evaluate((ConstantDoublesSurface) surface, x, y, shift, newName);
    }
    if (surface instanceof FunctionalDoublesSurface) {
      return useAdditive ? FUNCTIONAL_ADDITIVE.evaluate((FunctionalDoublesSurface) surface, x, y, shift, newName) :
        FUNCTIONAL_MULTIPLICATIVE.evaluate((FunctionalDoublesSurface) surface, x, y, shift, newName);
    }
    if (surface instanceof InterpolatedDoublesSurface) {
      return useAdditive ? INTERPOLATED_ADDITIVE.evaluate((InterpolatedDoublesSurface) surface, x, y, shift, newName) :
        INTERPOLATED_MULTIPLICATIVE.evaluate((InterpolatedDoublesSurface) surface, x, y, shift, newName);
    }
    if (surface instanceof InterpolatedFromCurvesDoublesSurface && useAdditive) {
      return INTERPOLATED_FROM_CURVES_ADDITIVE.evaluate((InterpolatedFromCurvesDoublesSurface) surface, x, y, shift, newName);
    }
    if (surface instanceof NodalDoublesSurface) {
      return useAdditive ? NODAL_ADDITIVE.evaluate((NodalDoublesSurface) surface, x, y, shift, newName) : 
        NODAL_MULTIPLICATIVE.evaluate((NodalDoublesSurface) surface, x, y, shift, newName);
    }
    throw new IllegalArgumentException("Do not have a surface shift function for surface " + surface.getClass());
  }
}
