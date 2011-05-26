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
public class RichardsonExtrapolationFiniteDifference implements ConvectionDiffusionPDESolver {

  private final ConvectionDiffusionPDESolver _baseSolver;

  public RichardsonExtrapolationFiniteDifference(ConvectionDiffusionPDESolver baseSolver) {
    Validate.notNull(baseSolver, "null baseSolver");
    _baseSolver = baseSolver;

  }

  @Override
  public PDEResults1D solve(ConvectionDiffusionPDEDataBundle pdeData, int tSteps, int xSteps, double tMax, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary) {
    PDEGrid1D grid = new PDEGrid1D(tSteps + 1, xSteps + 1, tMax, lowerBoundary.getLevel(), upperBoundary.getLevel());
    return solve(pdeData, grid, lowerBoundary, upperBoundary);
  }

  @Override
  public PDEResults1D solve(ConvectionDiffusionPDEDataBundle pdeData, int tSteps, int xSteps, double tMax, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary,
      Surface<Double, Double, Double> freeBoundary) {
    PDEGrid1D grid = new PDEGrid1D(tSteps + 1, xSteps + 1, tMax, lowerBoundary.getLevel(), upperBoundary.getLevel());
    return solve(pdeData, grid, lowerBoundary, upperBoundary, freeBoundary);
  }

  @Override
  public PDEResults1D solve(ConvectionDiffusionPDEDataBundle pdeData, PDEGrid1D grid, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary) {
    PDEGrid1D grid2 = grid.withDoubleTimeSteps();
    PDEResults1D res1 = _baseSolver.solve(pdeData, grid, lowerBoundary, upperBoundary);
    PDEResults1D res2 = _baseSolver.solve(pdeData, grid2, lowerBoundary, upperBoundary);
    int n = res1.getNumberSpaceNodes();
    if (res1 instanceof PDEFullResults1D) {
      PDEFullResults1D full1 = (PDEFullResults1D) res1;
      PDEFullResults1D full2 = (PDEFullResults1D) res2;
      double[][] data = new double[grid.getNumTimeNodes()][grid.getNumSpaceNodes()];
      for (int j = 0; j < grid.getNumTimeNodes(); j++) {
        for (int i = 0; i < n; i++) {
          data[j][i] = 2 * full2.getFunctionValue(i, 2 * j) - full1.getFunctionValue(i, j);
        }
      }
      return new PDEFullResults1D(grid, data);
    } else {
      double[] f = new double[n];
      for (int i = 0; i < n; i++) {
        f[i] = 2 * res2.getFunctionValue(i) - res1.getFunctionValue(i);
      }
      return new PDETerminalResults1D(grid, f);
    }
  }

  @Override
  public PDEResults1D solve(ConvectionDiffusionPDEDataBundle pdeData, PDEGrid1D grid, BoundaryCondition lowerBoundary, BoundaryCondition upperBoundary, Surface<Double, Double, Double> freeBoundary) {
    PDEGrid1D grid2 = grid.withDoubleTimeSteps();
    PDEResults1D res1 = _baseSolver.solve(pdeData, grid, lowerBoundary, upperBoundary, freeBoundary);
    PDEResults1D res2 = _baseSolver.solve(pdeData, grid2, lowerBoundary, upperBoundary, freeBoundary);
    int n = res1.getNumberSpaceNodes();
    double[] f = new double[n];
    for (int i = 0; i < n; i++) {
      f[i] = 2 * res2.getFunctionValue(i) - res1.getFunctionValue(i);
    }
    return new PDETerminalResults1D(grid, f);
  }

}
