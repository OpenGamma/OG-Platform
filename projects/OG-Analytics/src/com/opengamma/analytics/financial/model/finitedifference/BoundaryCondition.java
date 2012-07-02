/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

/**
 * Represents a boundary condition for a PDE solver that has time dependent characteristics but is fixed at a space level 
 */
public interface BoundaryCondition {

  double[] getLeftMatrixCondition(final ConvectionDiffusionPDE1DStandardCoefficients data, final PDEGrid1D grid, final double t);

  double[] getRightMatrixCondition(final ConvectionDiffusionPDE1DStandardCoefficients data, final PDEGrid1D grid, final double t);

  double getConstant(final ConvectionDiffusionPDE1DStandardCoefficients data, final double t);

  double getLevel();

}
