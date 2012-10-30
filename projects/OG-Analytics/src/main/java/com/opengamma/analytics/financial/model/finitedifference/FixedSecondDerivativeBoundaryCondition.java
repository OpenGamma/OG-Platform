/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

/**
 * boundary condition, i.e. d^2u/dx^2(A,t) = f(t), where A is the boundary level, and f(t) is some specified function of time
 */
public class FixedSecondDerivativeBoundaryCondition implements BoundaryCondition {

  private final double _secondDev;
  private final double _level;
  private final boolean _isLower;

  public FixedSecondDerivativeBoundaryCondition(final double secondDev, final double level, final boolean isLower) {
    _secondDev = secondDev;
    _level = level;
    _isLower = isLower;
  }

  @Override
  public double getConstant(final ConvectionDiffusionPDE1DStandardCoefficients data, final double t) {
    return _secondDev;
  }

  @Override
  public double[] getLeftMatrixCondition(final ConvectionDiffusionPDE1DStandardCoefficients data, PDEGrid1D grid, final double t) {
    double[] temp;
    if (_isLower) {
      temp = grid.getSecondDerivativeCoefficients(0);
    } else {
      temp = grid.getSecondDerivativeCoefficients(grid.getNumSpaceNodes() - 1);
    }
    return temp;
  }

  @Override
  public double[] getRightMatrixCondition(final ConvectionDiffusionPDE1DStandardCoefficients data, PDEGrid1D grid, final double t) {
    return new double[0];
  }

  @Override
  public double getLevel() {
    return _level;
  }

}
