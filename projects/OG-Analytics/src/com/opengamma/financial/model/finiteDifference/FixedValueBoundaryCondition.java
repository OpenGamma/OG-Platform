/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

/**
 * 
 */
public class FixedValueBoundaryCondition implements BoundaryCondition {
  
  private final double _fixedValue;
  private final double  _level;
  
  public FixedValueBoundaryCondition(final double fixedValue, final double level) {
    _fixedValue = fixedValue;
    _level = level;
  }

  @Override
  public double getConstant(PDEDataBundle data, final double t) {
    return _fixedValue;
  }

  @Override
  public double[] getLeftMatrixCondition(PDEDataBundle data,  final double t) {
    return new double[]{1.0};
   
  }

  @Override
  public double[] getRightMatrixCondition(PDEDataBundle data,  final double t) {
    return new double[0];
  }

  @Override
  public double getLevel() {
    return _level;
  }

}
