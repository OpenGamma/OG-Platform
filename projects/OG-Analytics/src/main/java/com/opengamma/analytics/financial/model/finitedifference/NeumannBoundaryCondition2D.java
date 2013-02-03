/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.surface.ConstantDoublesSurface;
import com.opengamma.analytics.math.surface.Surface;

/**
 * 
 */
public class NeumannBoundaryCondition2D implements BoundaryCondition2D {

  private final Surface<Double, Double, Double> _f;
  private final double _level;

  public NeumannBoundaryCondition2D(final Surface<Double, Double, Double> boundaryFirstDeriviative, double boundaryLevel) {
    Validate.notNull(boundaryFirstDeriviative, "boundaryValue ");
    _f = boundaryFirstDeriviative;
    _level = boundaryLevel;
  }

  public NeumannBoundaryCondition2D(final double boundaryFirstDeriviative, double boundaryLevel) {
    _f = ConstantDoublesSurface.from(boundaryFirstDeriviative);
    _level = boundaryLevel;
  }

  @Override
  public double getConstant(double t, double boundaryPosition, double gridSpacing) {
    return _f.getZValue(t, boundaryPosition) * gridSpacing;
  }

  @Override
  public double[] getLeftMatrixCondition(double t, double boundaryPosition) {
    return new double[] {-1, 1 };
  }

  @Override
  public double getLevel() {
    return _level;
  }

  @Override
  public double[] getRightMatrixCondition(double t, double boundaryPosition) {
    return new double[0];
  }

}
