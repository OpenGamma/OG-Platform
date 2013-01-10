/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import java.util.Arrays;

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

  @Override
  public double getFunctionValue(final int index) {
    return _terminalResults.getFunctionValue(index);
  }

  @Override
  public double getSpaceValue(final int spaceIndex) {
    return _grid.getSpaceNode(spaceIndex);
  }

  @Override
  public double getFirstSpatialDerivative(final int spaceIndex) {
    return _terminalResults.getFirstSpatialDerivative(spaceIndex);
  }

  public double getFirstSpatialDerivative(final int spaceIndex, final int timeIndex) {
    double[] coeff;
    double res = 0;
    final int n = _grid.getNumSpaceNodes();
    int offset;
    if (spaceIndex == 0) {
      coeff = _grid.getFirstDerivativeForwardCoefficients(spaceIndex);
      offset = 0;
    } else if (spaceIndex == n - 1) {
      coeff = _grid.getFirstDerivativeBackwardCoefficients(spaceIndex);
      offset = -coeff.length + 1;
    } else {
      coeff = _grid.getFirstDerivativeCoefficients(spaceIndex);
      offset = -(coeff.length - 1) / 2;
    }
    for (int i = 0; i < coeff.length; i++) {
      res += coeff[i] * _f[timeIndex][spaceIndex + i + offset];
    }
    return res;
  }

  @Override
  public double getSecondSpatialDerivative(final int spaceIndex) {
    return _terminalResults.getSecondSpatialDerivative(spaceIndex);
  }

  public double getSecondSpatialDerivative(final int spaceIndex, final int timeIndex) {
    final double[] coeff = _grid.getSecondDerivativeCoefficients(spaceIndex);
    double res = 0;
    int offset;
    if (spaceIndex == 0) {
      offset = 0;
    } else if (spaceIndex == _grid.getNumSpaceNodes() - 1) {
      offset = -2;
    } else {
      offset = -1;
    }

    for (int i = 0; i < coeff.length; i++) {
      res += coeff[i] * _f[timeIndex][spaceIndex + i + offset];
    }
    return res;
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

  public double getTimeValue(final int timeIndex) {
    return _grid.getTimeNode(timeIndex);
  }

  @Override
  public PDEGrid1D getGrid() {
    return _grid;
  }

  public double[][] getF() {
    return _f;
  }

  public PDEFullResults1D withGrid(final PDEGrid1D grid) {
    return new PDEFullResults1D(grid, _f);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_f);
    result = prime * result + ((_grid == null) ? 0 : _grid.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PDEFullResults1D other = (PDEFullResults1D) obj;
    if (!Arrays.equals(_f, other._f)) {
      return false;
    }
    if (_grid == null) {
      if (other._grid != null) {
        return false;
      }
    } else if (!_grid.equals(other._grid)) {
      return false;
    }
    return true;
  }

  @Override
  public double[] getTerminalResults() {
    return _terminalResults.getTerminalResults();
  }

}
