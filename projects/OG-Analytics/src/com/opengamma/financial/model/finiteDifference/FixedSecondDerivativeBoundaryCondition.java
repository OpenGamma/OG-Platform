/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

/**
 * boundary condition, i.e. d^2u/dx^2(A,t) = f(t), where A is the boundary level, and f(t) is some specified function of time
 */
public class FixedSecondDerivativeBoundaryCondition implements BoundaryCondition {

  private final double _secondDev;
  private final double _level;

  public FixedSecondDerivativeBoundaryCondition(final double secondDev, final double level) {
    _secondDev = secondDev;
    _level = level;
  }

  @Override
  public double getConstant(final PDEDataBundle data, final double t, final double dx) {
    return _secondDev * dx * dx;
  }

  @Override
  public double[] getLeftMatrixCondition(final PDEDataBundle data, final double t) {
    return new double[] {1, -2, 1};
  }

  @Override
  public double[] getRightMatrixCondition(final PDEDataBundle data, final double t) {
    return new double[0];
  }

  @Override
  public double getLevel() {
    return _level;
  }

}
