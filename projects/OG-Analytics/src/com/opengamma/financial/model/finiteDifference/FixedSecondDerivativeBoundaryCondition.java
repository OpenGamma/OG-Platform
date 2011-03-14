/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
  public double getConstant(final PDEDataBundle data, final double t) {
    return _secondDev;
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
