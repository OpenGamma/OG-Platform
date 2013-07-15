/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class RichardsonExtrapolationFiniteDifference implements ConvectionDiffusionPDESolver {

  private final ConvectionDiffusionPDESolver _baseSolver;

  public RichardsonExtrapolationFiniteDifference(final ConvectionDiffusionPDESolver baseSolver) {
    Validate.notNull(baseSolver, "null baseSolver");
    _baseSolver = baseSolver;
  }

  @Override
  public PDEResults1D solve(final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> pdeData) {
    final PDEGrid1D grid = pdeData.getGrid();
    final PDEGrid1D grid2 = grid.withDoubleTimeSteps();
    final PDEResults1D res1 = _baseSolver.solve(pdeData);
    final PDE1DDataBundle<ConvectionDiffusionPDE1DCoefficients> pdeData2 = pdeData.withGrid(grid2);
    final PDEResults1D res2 = _baseSolver.solve(pdeData2);
    final int n = res1.getNumberSpaceNodes();
    if (res1 instanceof PDEFullResults1D) {
      final PDEFullResults1D full1 = (PDEFullResults1D) res1;
      final PDEFullResults1D full2 = (PDEFullResults1D) res2;
      final double[][] data = new double[grid.getNumTimeNodes()][grid.getNumSpaceNodes()];
      for (int j = 0; j < grid.getNumTimeNodes(); j++) {
        for (int i = 0; i < n; i++) {
          data[j][i] = 2 * full2.getFunctionValue(i, 2 * j) - full1.getFunctionValue(i, j);
        }
      }
      return new PDEFullResults1D(grid, data);
    }
    final double[] f = new double[n];
    for (int i = 0; i < n; i++) {
      f[i] = 2 * res2.getFunctionValue(i) - res1.getFunctionValue(i);
    }
    return new PDETerminalResults1D(grid, f);
  }

}
