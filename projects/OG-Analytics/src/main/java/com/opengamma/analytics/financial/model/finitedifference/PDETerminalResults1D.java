/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

/**
 * Holds the result of the PDE solver on the final time slice only
 */
public class PDETerminalResults1D implements PDEResults1D {

  private final double[] _f;
  private final PDEGrid1D _grid;

  public PDETerminalResults1D(final PDEGrid1D grid, final double[] finalTimeStep) {
    Validate.isTrue(grid.getNumSpaceNodes() == finalTimeStep.length, "space steps in grid not equal to that in data");
    _f = finalTimeStep;
    _grid = grid;
  }

  @Override
  public double getSpaceValue(final int spaceIndex) {
    return _grid.getSpaceNode(spaceIndex);
  }

  @Override
  public double getFunctionValue(final int spaceIndex) {
    return _f[spaceIndex];
  }

  @Override
  public double getFirstSpatialDerivative(final int spaceIndex) {
    checkSpaceIndex(spaceIndex);
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
      res += coeff[i] * _f[spaceIndex + i + offset];
    }
    return res;
  }

  @Override
  public double getSecondSpatialDerivative(final int spaceIndex) {
    checkSpaceIndex(spaceIndex);
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
      res += coeff[i] * _f[spaceIndex + i + offset];
    }
    return res;
  }

  private void checkSpaceIndex(final int spaceIndex) {
    Validate.isTrue(spaceIndex >= 0 && spaceIndex < _grid.getNumSpaceNodes(), "spaceIndex out of range");
  }

  @Override
  public int getNumberSpaceNodes() {
    return _grid.getNumSpaceNodes();
  }

  @Override
  public PDEGrid1D getGrid() {
    return _grid;
  }

  @Override
  public double[] getTerminalResults() {
    return _f;
  }

}
