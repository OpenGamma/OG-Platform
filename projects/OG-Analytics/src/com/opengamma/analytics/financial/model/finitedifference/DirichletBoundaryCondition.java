/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Dirichlet boundary condition, i.e. u(A,t) = f(t), where A is the boundary level, and f(t) is some specified function of time
 */
public class DirichletBoundaryCondition implements BoundaryCondition {

  private final Function1D<Double, Double> _timeValue;
  private final double _level;

  /**
   * Dirichlet boundary condition, i.e. u(A,t) = f(t), where A is the boundary level, and f(t) is some specified function of time
   * @param timeValue The value of u at the boundary, i.e. u(A,t) = f(t) 
   * @param level The boundary level (A)
   */
  public DirichletBoundaryCondition(final Function1D<Double, Double> timeValue, double level) {
    Validate.notNull(timeValue, "null timeValue");
    _timeValue = timeValue;
    _level = level;
  }

  /**
   * Special case of Dirichlet boundary condition, i.e. u(A,t) = constant, where A is the boundary level
   * @param fixedValue The constant value at the boundary
   * @param level The boundary level (A)
   */
  public DirichletBoundaryCondition(final double fixedValue, final double level) {
    _timeValue = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(Double x) {
        return fixedValue;
      }
    };
    _level = level;
  }

  @Override
  public double getConstant(final ConvectionDiffusionPDE1DStandardCoefficients data, final double t) {
    return _timeValue.evaluate(t);
  }

  @Override
  public double getLevel() {
    return _level;
  }

  @Override
  public double[] getLeftMatrixCondition(ConvectionDiffusionPDE1DStandardCoefficients data, PDEGrid1D grid, double t) {
    return new double[] {1.0 };
  }

  @Override
  public double[] getRightMatrixCondition(ConvectionDiffusionPDE1DStandardCoefficients data, PDEGrid1D grid, double t) {
    return new double[0];
  }

}
