/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.cube;

import org.apache.commons.lang.Validate;

/**
 * Shifts a {@link ConstantDoublesCube}. Only constant percentage shifts of the cube are supported.
 */
public class ConstantCubeMultiplicativeShiftFunction implements CubeShiftFunction<ConstantDoublesCube> {

  /**
   * {@inheritDoc}
   */
  @Override
  public ConstantDoublesCube evaluate(final ConstantDoublesCube cube, final double percentage) {
    Validate.notNull(cube, "cube");
    return evaluate(cube, percentage, "CONSTANT_MULTIPLIER_" + cube.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConstantDoublesCube evaluate(final ConstantDoublesCube cube, final double percentage, final String newName) {
    Validate.notNull(cube, "cube");
    final double z = cube.getValue(0., 0., 0.);
    return ConstantDoublesCube.from(z * (1 + percentage), newName);
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public ConstantDoublesCube evaluate(final ConstantDoublesCube cube, final double x, final double y, final double z, final double shift) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public ConstantDoublesCube evaluate(final ConstantDoublesCube cube, final double x, final double y, final double z, final double percentage, final String newName) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public ConstantDoublesCube evaluate(final ConstantDoublesCube cube, final double[] xShift, final double[] yShift, final double[] zShift, final double[] percentage) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public ConstantDoublesCube evaluate(final ConstantDoublesCube cube, final double[] xShift, final double[] yShift, final double[] zShift, final double[] percentage, final String newName) {
    throw new UnsupportedOperationException();
  }

}
