/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import org.apache.commons.lang.Validate;

/**
 * Shifts a {@link ConstantDoublesSurface}. Only constant percentage shifts of the surface are supported.
 */
public class ConstantSurfaceMultiplicativeShiftFunction implements SurfaceShiftFunction<ConstantDoublesSurface> {

  /**
   * {@inheritDoc}
   */
  @Override
  public ConstantDoublesSurface evaluate(final ConstantDoublesSurface surface, final double percentage) {
    Validate.notNull(surface, "surface");
    return evaluate(surface, percentage, "CONSTANT_MULTIPLIER_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConstantDoublesSurface evaluate(final ConstantDoublesSurface surface, final double percentage, final String newName) {
    Validate.notNull(surface, "surface");
    final double z = surface.getZValue(0., 0.);
    return ConstantDoublesSurface.from(z * (1 + percentage), newName);
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public ConstantDoublesSurface evaluate(final ConstantDoublesSurface surface, final double x, final double y, final double shift) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public ConstantDoublesSurface evaluate(final ConstantDoublesSurface surface, final double x, final double y, final double percentage, final String newName) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public ConstantDoublesSurface evaluate(final ConstantDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] percentage) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public ConstantDoublesSurface evaluate(final ConstantDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] percentage, final String newName) {
    throw new UnsupportedOperationException();
  }

}
