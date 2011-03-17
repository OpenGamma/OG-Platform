/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finiteDifference;

import org.apache.commons.lang.Validate;

import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class ValueBoundaryCondition implements BoundaryCondition {
  
  private Function1D<Double, Double> _timeValue;
  private double _level;
  
  public ValueBoundaryCondition(final Function1D<Double, Double> timeValue, double level) {
    Validate.notNull(timeValue, "null timeValue");
    _timeValue = timeValue;
    _level = level;
  }

  @Override
  public double getConstant(PDEDataBundle data, double t) {
    return _timeValue.evaluate(t);
  }

  @Override
  public double[] getLeftMatrixCondition(PDEDataBundle data, double t) {
    return new double[]{1.0};
  }

  @Override
  public double[] getRightMatrixCondition(PDEDataBundle data, double t) {
    return new double[0];
  }

  @Override
  public double getLevel() {
    return _level;
  }

}
