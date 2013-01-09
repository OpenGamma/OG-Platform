/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import com.opengamma.analytics.math.function.Function1D;

/**
 * Represents one half of a coupled 1D PDE system, with a coupling strength lambda of this system to the other  
 */
public class CoupledPDEDataBundle extends PDE1DDataBundle<ConvectionDiffusionPDE1DCoupledCoefficients> {

  //TODO this is not an ideal solution - each instance has a copy of the grid, even though they must be identical 
  //TODO add variable lambda 
  public CoupledPDEDataBundle(final ConvectionDiffusionPDE1DCoupledCoefficients coefficients,
      final Function1D<Double, Double> initialCondition,
      final BoundaryCondition lowerBoundary,
      final BoundaryCondition upperBoundary,
      final PDEGrid1D grid) {
    super(coefficients, initialCondition, lowerBoundary, upperBoundary, grid);
  }

}
