/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

/**
 * 
 */
public class FixedSecondDerivativeBoundaryCondition implements BoundaryCondition {

  private final double _secondDev;
  private final double _level;

  public FixedSecondDerivativeBoundaryCondition(final double secondDev, final double level) {
    _secondDev = secondDev;
    _level = level;
  }

  @Override
  public double getConstant(PDEDataBundle data, final double t) {
    return _secondDev;
  }

  @Override
  public double[] getLeftMatrixCondition(PDEDataBundle data, final double t) {
    return new double[] {1, -2, 1};
  }

  @Override
  public double[] getRightMatrixCondition(PDEDataBundle data, final double t) {
    return new double[0];
  }

  @Override
  public double getLevel() {
    return _level;
  }

}
