/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import java.util.HashMap;
import java.util.Map;

/**
 * Contains methods for performing shifts on {@link Cube} without needing to know the exact type of the cube.
 */
public class CubeShiftFunctionFactory {
  /** Additive shift function for {@link ConstantDoublesCube} */
  public static final ConstantCubeAdditiveShiftFunction CONSTANT_ADDITIVE = new ConstantCubeAdditiveShiftFunction();
  /** Multiplicative shift function for {@link ConstantDoublesCube} */
  public static final ConstantCubeMultiplicativeShiftFunction CONSTANT_MULTIPLICATIVE = new ConstantCubeMultiplicativeShiftFunction();
  /** Additive shift function for {@link FunctionalDoublesCube} */
  public static final FunctionalCubeAdditiveShiftFunction FUNCTIONAL_ADDITIVE = new FunctionalCubeAdditiveShiftFunction();
  /** Multiplicative shift function for {@link FunctionalDoublesCube} */
  public static final FunctionalCubeMultiplicativeShiftFunction FUNCTIONAL_MULTIPLICATIVE = new FunctionalCubeMultiplicativeShiftFunction();
  /** Additive shift function for {@link InterpolatedDoublesCube}  */
  public static final InterpolatedCubeAdditiveShiftFunction INTERPOLATED_ADDITIVE = new InterpolatedCubeAdditiveShiftFunction();
  /** Multiplicative shift function for {@link InterpolatedDoublesCube} */
  public static final InterpolatedCubeMultiplicativeShiftFunction INTERPOLATED_MULTIPLICATIVE = new InterpolatedCubeMultiplicativeShiftFunction();
  /** Additive shift function for {@link InterpolatedFromSurfacesDoublesCube} */
  public static final InterpolatedFromSurfacesCubeAdditiveShiftFunction INTERPOLATED_FROM_CURVES_ADDITIVE = new InterpolatedFromSurfacesCubeAdditiveShiftFunction();
  /** Additive shift function for {@link NodalDoublesCube} */
  public static final NodalCubeAdditiveShiftFunction NODAL_ADDITIVE = new NodalCubeAdditiveShiftFunction();
  /** Multiplicative shift function for {@link NodalDoublesCube} */
  public static final NodalCubeMultiplicativeShiftFunction NODAL_MULTIPLICATIVE = new NodalCubeMultiplicativeShiftFunction();
  private static final Map<Class<?>, CubeShiftFunction<?>> s_instances = new HashMap<>();

  static {
    s_instances.put(ConstantCubeAdditiveShiftFunction.class, CONSTANT_ADDITIVE);
    s_instances.put(ConstantCubeMultiplicativeShiftFunction.class, CONSTANT_MULTIPLICATIVE);
    s_instances.put(FunctionalCubeAdditiveShiftFunction.class, FUNCTIONAL_ADDITIVE);
    s_instances.put(FunctionalCubeMultiplicativeShiftFunction.class, FUNCTIONAL_MULTIPLICATIVE);
    s_instances.put(InterpolatedCubeAdditiveShiftFunction.class, INTERPOLATED_ADDITIVE);
    s_instances.put(InterpolatedCubeMultiplicativeShiftFunction.class, INTERPOLATED_MULTIPLICATIVE);
    s_instances.put(InterpolatedFromSurfacesCubeAdditiveShiftFunction.class, INTERPOLATED_FROM_CURVES_ADDITIVE);
    s_instances.put(NodalCubeAdditiveShiftFunction.class, NODAL_ADDITIVE);
    s_instances.put(NodalCubeMultiplicativeShiftFunction.class, NODAL_MULTIPLICATIVE);
  }

  /**
   * Gets the function for a class type.
   * @param clazz The class
   * @return The function
   * @throws IllegalArgumentException If the function is not one of the static instances
   */
  public static CubeShiftFunction<?> getFunction(final Class<?> clazz) {
    final CubeShiftFunction<?> f = s_instances.get(clazz);
    if (f == null) {
      throw new IllegalArgumentException("Could not get function for " + clazz.getName());
    }
    return f;
  }

  /**
   * For a cube Cube<Double, Double, Double, Double>, return a parallel-shifted cube.
   * @param cube The original cube
   * @param shift The shift
   * @param useAdditive true if the shift is additive, false if the shift is multiplicative (i.e. a percentage shift)
   * @return A shifted cube with automatically-generated name
   * @throws IllegalArgumentException If the cube type is not constant, functional, interpolated, nodal or spread
   */
  public static Cube<Double, Double, Double, Double> getShiftedCube(final Cube<Double, Double, Double, Double> cube, final double shift, final boolean useAdditive) {
    if (cube instanceof ConstantDoublesCube) {
      return useAdditive ? CONSTANT_ADDITIVE.evaluate((ConstantDoublesCube) cube, shift) : CONSTANT_MULTIPLICATIVE
          .evaluate((ConstantDoublesCube) cube, shift);
    }
    if (cube instanceof FunctionalDoublesCube) {
      return useAdditive ? FUNCTIONAL_ADDITIVE.evaluate((FunctionalDoublesCube) cube, shift) : FUNCTIONAL_MULTIPLICATIVE.evaluate(
          (FunctionalDoublesCube) cube, shift);
    }
    if (cube instanceof InterpolatedDoublesCube) {
      return useAdditive ? INTERPOLATED_ADDITIVE.evaluate((InterpolatedDoublesCube) cube, shift) : INTERPOLATED_MULTIPLICATIVE.evaluate(
          (InterpolatedDoublesCube) cube, shift);
    }
    if (cube instanceof InterpolatedFromSurfacesDoublesCube && useAdditive) {
      return INTERPOLATED_FROM_CURVES_ADDITIVE.evaluate((InterpolatedFromSurfacesDoublesCube) cube, shift);
    }
    if (cube instanceof NodalDoublesCube) {
      return useAdditive ? NODAL_ADDITIVE.evaluate((NodalDoublesCube) cube, shift) : NODAL_MULTIPLICATIVE.evaluate((NodalDoublesCube) cube, shift);
    }
    throw new IllegalArgumentException("Do not have a cube shift function for cube " + cube.getClass());
  }

  /**
   * For a cube Cube<Double, Double, Double, Double>, return a cube shifted at one point.
   * @param cube The original cube
   * @param x The <i>x</i> value of the shift
   * @param y The <i>y</i> value of the shift
   * @param z The <i>z</i> value of the shift
   * @param shift The shift
   * @param useAdditive true if the shift is additive, false if the shift is multiplicative (i.e. a percentage shift)
   * @return A shifted cube with automatically-generated name
   * @throws IllegalArgumentException If the cube type is not constant, functional, interpolated, nodal or spread
   */
  public static Cube<Double, Double, Double, Double> getShiftedCube(final Cube<Double, Double, Double, Double> cube, final double x, final double y, final double z, final double shift,
      final boolean useAdditive) {
    if (cube instanceof ConstantDoublesCube) {
      throw new UnsupportedOperationException("Cannot shift a single point on a constant curve");
    }
    if (cube instanceof FunctionalDoublesCube) {
      throw new UnsupportedOperationException("Cannot shift a single point on a functional curve");
    }
    if (cube instanceof InterpolatedDoublesCube) {
      return useAdditive ? INTERPOLATED_ADDITIVE.evaluate((InterpolatedDoublesCube) cube, x, y, z, shift) : INTERPOLATED_MULTIPLICATIVE.evaluate(
          (InterpolatedDoublesCube) cube, x, y, z, shift);
    }
    if (cube instanceof InterpolatedFromSurfacesDoublesCube && useAdditive) {
      return INTERPOLATED_FROM_CURVES_ADDITIVE.evaluate((InterpolatedFromSurfacesDoublesCube) cube, x, y, z, shift);
    }
    if (cube instanceof NodalDoublesCube) {
      return useAdditive ? NODAL_ADDITIVE.evaluate((NodalDoublesCube) cube, x, y, z, shift) : NODAL_MULTIPLICATIVE
          .evaluate((NodalDoublesCube) cube, x, y, z, shift);
    }
    throw new IllegalArgumentException("Do not have a cube shift function for cube " + cube.getClass());
  }

  /**
   * For a cube Cube<Double, Double, Double, Double>, return a parallel-shifted cube.
   * @param cube The original cube
   * @param x An array of <i>x</i> values to shift
   * @param y An array of <i>y</i> values to shift
   * @param z An array of <i>z</i> values to shift
   * @param shift The shifts
   * @param useAdditive true if the shift is additive, false if the shift is multiplicative (i.e. a percentage shift)
   * @return A shifted cube with an automatically-generated name
   * @throws IllegalArgumentException If the cube type is not constant, functional, interpolated, nodal or spread
   */
  public static Cube<Double, Double, Double, Double> getShiftedCube(final Cube<Double, Double, Double, Double> cube, final double[] x, final double[] y, final double[] z,
      final double[] shift, final boolean useAdditive) {
    if (cube instanceof ConstantDoublesCube) {
      throw new UnsupportedOperationException("Cannot parallel shift a constant curve");
    }
    if (cube instanceof FunctionalDoublesCube) {
      throw new UnsupportedOperationException("Cannot parallel shift a functional curve");
    }
    if (cube instanceof InterpolatedDoublesCube) {
      return useAdditive ? INTERPOLATED_ADDITIVE.evaluate((InterpolatedDoublesCube) cube, x, y, z, shift) : INTERPOLATED_MULTIPLICATIVE.evaluate(
          (InterpolatedDoublesCube) cube, x, y, z, shift);
    }
    if (cube instanceof InterpolatedFromSurfacesDoublesCube && useAdditive) {
      return INTERPOLATED_FROM_CURVES_ADDITIVE.evaluate((InterpolatedFromSurfacesDoublesCube) cube, x, y, z, shift);
    }
    if (cube instanceof NodalDoublesCube) {
      return useAdditive ? NODAL_ADDITIVE.evaluate((NodalDoublesCube) cube, x, y, z, shift) : NODAL_MULTIPLICATIVE
          .evaluate((NodalDoublesCube) cube, x, y, z, shift);
    }
    throw new IllegalArgumentException("Do not have a cube shift function for cube " + cube.getClass());
  }

  /**
   * For a cube Cube<Double, Double, Double, Double>, return a parallel-shifted cube.
   * @param cube The original cube
   * @param shift The shift
   * @param newName The name of the shifted cube
   * @param useAdditive true if the shift is additive, false if the shift is multiplicative (i.e. a percentage shift)
   * @return A shifted cube
   * @throws IllegalArgumentException If the cube type is not constant, functional, interpolated, nodal or spread
   */
  public static Cube<Double, Double, Double, Double> getShiftedCube(final Cube<Double, Double, Double, Double> cube, final double shift, final String newName,
      final boolean useAdditive) {
    if (cube instanceof ConstantDoublesCube) {
      return useAdditive ? CONSTANT_ADDITIVE.evaluate((ConstantDoublesCube) cube, shift, newName) : CONSTANT_MULTIPLICATIVE.evaluate(
          (ConstantDoublesCube) cube, shift, newName);
    }
    if (cube instanceof FunctionalDoublesCube) {
      return useAdditive ? FUNCTIONAL_ADDITIVE.evaluate((FunctionalDoublesCube) cube, shift, newName) : FUNCTIONAL_MULTIPLICATIVE.evaluate(
          (FunctionalDoublesCube) cube, shift, newName);
    }
    if (cube instanceof InterpolatedDoublesCube) {
      return useAdditive ? INTERPOLATED_ADDITIVE.evaluate((InterpolatedDoublesCube) cube, shift, newName) : INTERPOLATED_MULTIPLICATIVE.evaluate(
          (InterpolatedDoublesCube) cube, shift, newName);
    }
    if (cube instanceof InterpolatedFromSurfacesDoublesCube && useAdditive) {
      return INTERPOLATED_FROM_CURVES_ADDITIVE.evaluate((InterpolatedFromSurfacesDoublesCube) cube, shift, newName);
    }
    if (cube instanceof NodalDoublesCube) {
      return useAdditive ? NODAL_ADDITIVE.evaluate((NodalDoublesCube) cube, shift, newName) : NODAL_MULTIPLICATIVE.evaluate((NodalDoublesCube) cube, shift,
          newName);
    }
    throw new IllegalArgumentException("Do not have a cube shift function for cube " + cube.getClass());
  }

  /**
   * For a cube Cube<Double, Double, Double, Double>, return a cube shifted at one point.
   * @param cube The original cube
   * @param x The <i>x</i> value of the shift
   * @param y The <i>y</i> value of the shift
   * @param z The <i>z</i> value of the shift
   * @param shift The shift
   * @param newName The name of the new cube
   * @param useAdditive true if the shift is additive, false if the shift is multiplicative (i.e. a percentage shift)
   * @return A shifted cube
   * @throws IllegalArgumentException If the cube type is not constant, functional, interpolated, nodal or spread
   */
  public static Cube<Double, Double, Double, Double> getShiftedCube(final Cube<Double, Double, Double, Double> cube, final double x, final double y, final double z, final double shift,
      final String newName, final boolean useAdditive) {
    if (cube instanceof ConstantDoublesCube) {
      throw new UnsupportedOperationException("Cannot shift a single point on a constant curve");
    }
    if (cube instanceof FunctionalDoublesCube) {
      throw new UnsupportedOperationException("Cannot shift a single point on a functional curve");
    }
    if (cube instanceof InterpolatedDoublesCube) {
      return useAdditive ? INTERPOLATED_ADDITIVE.evaluate((InterpolatedDoublesCube) cube, x, y, z, shift, newName) : INTERPOLATED_MULTIPLICATIVE.evaluate(
          (InterpolatedDoublesCube) cube, x, y, z, shift, newName);
    }
    if (cube instanceof InterpolatedFromSurfacesDoublesCube && useAdditive) {
      return INTERPOLATED_FROM_CURVES_ADDITIVE.evaluate((InterpolatedFromSurfacesDoublesCube) cube, x, y, z, shift, newName);
    }
    if (cube instanceof NodalDoublesCube) {
      return useAdditive ? NODAL_ADDITIVE.evaluate((NodalDoublesCube) cube, x, y, z, shift, newName) : NODAL_MULTIPLICATIVE.evaluate((NodalDoublesCube) cube, x,
          y, z, shift, newName);
    }
    throw new IllegalArgumentException("Do not have a cube shift function for cube " + cube.getClass());
  }

  /**
   * For a cube Cube<Double, Double, Double, Double>, return a parallel-shifted cube.
   * @param cube The original cube
   * @param x An array of <i>x</i> values to shift
   * @param y An array of <i>y</i> values to shift
   * @param z An array of <i>z</i> values to shift
   * @param shift The shifts
   * @param newName The name of the shifted cube
   * @param useAdditive true if the shift is additive, false if the shift is multiplicative (i.e. a percentage shift)
   * @return A shifted cube
   * @throws IllegalArgumentException If the cube type is not constant, functional, interpolated, nodal or spread
   */
  public static Cube<Double, Double, Double, Double> getShiftedCube(final Cube<Double, Double, Double, Double> cube, final double[] x, final double[] y, final double[] z,
      final double[] shift, final String newName, final boolean useAdditive) {
    if (cube instanceof ConstantDoublesCube) {
      throw new UnsupportedOperationException("Cannot parallel shift a constant curve");
    }
    if (cube instanceof FunctionalDoublesCube) {
      throw new UnsupportedOperationException("Cannot parallel shift a functional curve");
    }
    if (cube instanceof InterpolatedDoublesCube) {
      return useAdditive ? INTERPOLATED_ADDITIVE.evaluate((InterpolatedDoublesCube) cube, x, y, z, shift, newName) : INTERPOLATED_MULTIPLICATIVE.evaluate(
          (InterpolatedDoublesCube) cube, x, y, z, shift, newName);
    }
    if (cube instanceof InterpolatedFromSurfacesDoublesCube && useAdditive) {
      return INTERPOLATED_FROM_CURVES_ADDITIVE.evaluate((InterpolatedFromSurfacesDoublesCube) cube, x, y, z, shift, newName);
    }
    if (cube instanceof NodalDoublesCube) {
      return useAdditive ? NODAL_ADDITIVE.evaluate((NodalDoublesCube) cube, x, y, z, shift, newName) : NODAL_MULTIPLICATIVE.evaluate((NodalDoublesCube) cube, x,
          y, z, shift, newName);
    }
    throw new IllegalArgumentException("Do not have a cube shift function for cube " + cube.getClass());
  }
}
