/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Neumann boundary condition, i.e. du/dx(A,t) = f(t), where A is the boundary level, and f(t) is some specified function of time
 */
public class NeumannBoundaryCondition implements BoundaryCondition {

  private final Function1D<Double, Double> _timeValue;
  private final double _level;
  private final boolean _isLower;

  /**
   * Neumann  boundary condition, i.e. du/dx(A,t) = f(t), where A is the boundary level, and f(t) is some specified function of time
   * @param timeValue The value of u at the boundary, i.e. du/dx(A,t) = f(t) 
   * @param level The boundary level (A)
   * @param isLower True if this represents a lower boundary
   */
  public NeumannBoundaryCondition(final Function1D<Double, Double> timeValue, final double level, final boolean isLower) {
    Validate.notNull(timeValue, "null timeValue");
    _timeValue = timeValue;
    _level = level;
    _isLower = isLower;
  }

  public NeumannBoundaryCondition(final double fixedValue, final double level, final boolean isLower) {
    _timeValue = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        return fixedValue;
      }
    };
    _level = level;
    _isLower = isLower;
  }

  @Override
  public double getConstant(final ConvectionDiffusionPDE1DStandardCoefficients data, final double t) {
    return _timeValue.evaluate(t);
  }

  @Override
  public double[] getLeftMatrixCondition(ConvectionDiffusionPDE1DStandardCoefficients data, PDEGrid1D grid, double t) {
    double[] temp;
    if (_isLower) {
      temp = grid.getFirstDerivativeForwardCoefficients(0);
    } else {
      temp = grid.getFirstDerivativeBackwardCoefficients(grid.getNumSpaceNodes() - 1);
    }
    return temp;
  }

  @Override
  public double getLevel() {
    return _level;
  }

  @Override
  public double[] getRightMatrixCondition(ConvectionDiffusionPDE1DStandardCoefficients data, PDEGrid1D grid, double t) {
    return new double[0];
  }

}
