/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

import com.opengamma.math.surface.Surface;

/**
 * 
 */
public class RichardsonExstapolationFiniteDifference implements ConvectionDiffusionPDESolver {

  @SuppressWarnings("unused")
  private final ConvectionDiffusionPDESolver _baseSolver;

  public RichardsonExstapolationFiniteDifference(final ConvectionDiffusionPDESolver baseSolver) {
    Validate.notNull(baseSolver, "null baseSolver");
    _baseSolver = baseSolver;

  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary) {
    return null;
  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final int tSteps, final int xSteps, final double tMax, final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary,
      final Surface<Double, Double, Double> freeBoundary) {
    return null;
  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary) {
    return null;
  }

  @Override
  public PDEResults1D solve(final ConvectionDiffusionPDEDataBundle pdeData, final PDEGrid1D grid, final BoundaryCondition lowerBoundary, final BoundaryCondition upperBoundary,
      final Surface<Double, Double, Double> freeBoundary) {
    // PDEGrid1D doubleTimeStep = new PDEGrid1D(grid., null)
    // res1 = _bas
    //
    return null;
  }

}
