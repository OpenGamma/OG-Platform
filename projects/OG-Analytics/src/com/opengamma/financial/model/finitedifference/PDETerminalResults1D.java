/**
 * Copyright (C) 2009 - 2011 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class PDETerminalResults1D implements PDEResults1D {

  private final double[] _f;
  private final PDEGrid1D _grid;

  public PDETerminalResults1D(PDEGrid1D grid, double[] finalTimeStep) {
    Validate.isTrue(grid.getNumSpaceNodes() == finalTimeStep.length, "space steps in grid not equal to that in data");
    _f = finalTimeStep;
    _grid = grid;
  }

  @Override
  public double getSpaceValue(int spaceIndex) {
    return _grid.getSpaceNode(spaceIndex);
  }

  @Override
  public double getFunctionValue(int spaceIndex) {
    return _f[spaceIndex];
  }

  @Override
  public double getFirstSpatialDerivative(int spaceIndex) {
    checkSpaceIndex(spaceIndex);
    double[] coeff;
    double res = 0;
    int n = _grid.getNumSpaceNodes();
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
  public double getSecondSpatialDerivative(int spaceIndex) {
    checkSpaceIndex(spaceIndex);
    double[] coeff = _grid.getSecondDerivativeCoefficients(spaceIndex);
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

  private void checkSpaceIndex(int spaceIndex) {
    Validate.isTrue(spaceIndex >= 0 && spaceIndex < _grid.getNumSpaceNodes(), "spaceIndex out of range");
  }

  @Override
  public int getNumberSpaceNodes() {
    return _grid.getNumSpaceNodes();
  }

}
