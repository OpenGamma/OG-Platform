/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.math.surface;

import org.apache.commons.lang.Validate;

/**
 * Shifts a {@link ConstantDoublesSurface}. Only parallel shifts of the surface are supported.
 */
public class ConstantSurfaceAdditiveShiftFunction implements SurfaceShiftFunction<ConstantDoublesSurface> {

  /**
   * {@inheritDoc}
   */
  @Override
  public ConstantDoublesSurface evaluate(final ConstantDoublesSurface surface, final double shift) {
    Validate.notNull(surface, "surface");
    return evaluate(surface, shift, "PARALLEL_SHIFT_" + surface.getName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConstantDoublesSurface evaluate(final ConstantDoublesSurface surface, final double shift, final String newName) {
    Validate.notNull(surface, "surface");
    final double z = surface.getZValue(0., 0.);
    return ConstantDoublesSurface.from(z + shift, newName);
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
  public ConstantDoublesSurface evaluate(final ConstantDoublesSurface surface, final double x, final double y, final double shift, final String newName) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public ConstantDoublesSurface evaluate(final ConstantDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] shift) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * @return Not supported
   * @throws UnsupportedOperationException
   */
  @Override
  public ConstantDoublesSurface evaluate(final ConstantDoublesSurface surface, final double[] xShift, final double[] yShift, final double[] shift, final String newName) {
    throw new UnsupportedOperationException();
  }

}
