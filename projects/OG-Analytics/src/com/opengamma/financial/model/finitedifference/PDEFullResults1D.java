/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class PDEFullResults1D implements PDEResults1D {

  private final double[][] _f;
  private final PDEGrid1D _grid;
  private final PDETerminalResults1D _terminalResults;

  public PDEFullResults1D(final PDEGrid1D grid, final double[][] fullSolverData) {
    Validate.isTrue(grid.getNumTimeNodes() == fullSolverData.length, "time steps in grid not equal to that in data");
    Validate.isTrue(grid.getNumSpaceNodes() == fullSolverData[0].length, "space steps in grid not equal to that in data");
    _grid = grid;
    _f = fullSolverData;
    _terminalResults = new PDETerminalResults1D(grid, fullSolverData[grid.getNumTimeNodes() - 1]);
  }

  //  // penultimate
  //  public PDEFullResults1D(final PDEGrid1D grid, final double[] penultimateTimeStep, final double[] finalTimeStep) {
  //    Validate.isTrue(grid.getNumSpaceNodes() == penultimateTimeStep.length, "space steps in grid not equal to that in data");
  //    Validate.isTrue(grid.getNumSpaceNodes() == finalTimeStep.length, "space steps in grid not equal to that in data");
  //    _f = new double[2][];
  //    _f[0] = finalTimeStep;
  //    _f[1] = penultimateTimeStep;
  //    _grid = grid;
  //    _fullDataSet = false;
  //  }

  @Override
  public double getFunctionValue(final int index) {
    return _terminalResults.getFunctionValue(index);
  }

  @Override
  public double getSpaceValue(int spaceIndex) {
    return _grid.getSpaceNode(spaceIndex);
  }

  @Override
  public double getFirstSpatialDerivative(int spaceIndex) {
    return _terminalResults.getFirstSpatialDerivative(spaceIndex);
  }

  @Override
  public double getSecondSpatialDerivative(int spaceIndex) {
    return _terminalResults.getSecondSpatialDerivative(spaceIndex);
  }

  @Override
  public int getNumberSpaceNodes() {
    return _grid.getNumSpaceNodes();
  }

  public double getFunctionValue(final int spaceIndex, final int timeIndex) {
    return _f[timeIndex][spaceIndex];
  }

  public int getNumberTimeNodes() {
    return _grid.getNumTimeNodes();
  }

  public double getTimeValue(int timeIndex) {
    return _grid.getTimeNode(timeIndex);
  }

  @Override
  public PDEGrid1D getGrid() {
    return _grid;
  }

}
