/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.model.finitedifference;

import java.util.Arrays;

import org.apache.commons.lang.Validate;

/**
 * 
 */
public class PDEGrid1D {

  private final int _nSpaceNodes;
  private final double[] _tNodes;
  private final double[] _xNodes;
  private final double[] _dt;
  private final double[] _dx;
  private final double[][] _x1st;
  private final double[][] _x1stFwd;
  private final double[][] _x1stBkd;
  private final double[][] _x2nd;

  public PDEGrid1D(final int numTimeNodes, final int numSpaceNodes, final double tMax, final double xMin, final double xMax) {

    Validate.isTrue(numTimeNodes > 1, "need at least 2 time nodes");
    Validate.isTrue(numSpaceNodes > 2, "need at least 3 space nodes");
    Validate.isTrue(tMax > 0, "need tMax > 0");
    Validate.isTrue(xMax > xMin, "need xMax > xMin");

    _nSpaceNodes = numSpaceNodes;
    _tNodes = new double[numTimeNodes];
    _xNodes = new double[numSpaceNodes];
    _dt = new double[numTimeNodes - 1];
    _dx = new double[numSpaceNodes - 1];

    final double dt = tMax / (numTimeNodes - 1);
    _tNodes[numTimeNodes - 1] = tMax;
    for (int i = 0; i < numTimeNodes - 1; i++) {
      _tNodes[i] = i * dt;
      _dt[i] = dt;
    }

    final double dx = (xMax - xMin) / (numSpaceNodes - 1);
    _xNodes[numSpaceNodes - 1] = xMax;
    for (int i = 0; i < numSpaceNodes - 1; i++) {
      _xNodes[i] = xMin + i * dx;
      _dx[i] = dx;
    }

    _x1st = new double[numSpaceNodes - 2][3];
    _x2nd = new double[numSpaceNodes - 2][3];
    for (int i = 0; i < numSpaceNodes - 2; i++) {
      _x1st[i][0] = -1. / 2. / dx;
      _x1st[i][1] = 0.0;
      _x1st[i][2] = 1. / 2. / dx;
      _x2nd[i][0] = 1. / dx / dx;
      _x2nd[i][1] = -2. / dx / dx;
      _x2nd[i][2] = 1. / dx / dx;
    }

    _x1stFwd = new double[numSpaceNodes - 1][2];
    _x1stBkd = new double[numSpaceNodes - 1][2];
    for (int i = 0; i < numSpaceNodes - 1; i++) {
      _x1stFwd[i][0] = -1 / dx;
      _x1stFwd[i][1] = 1 / dx;
      _x1stBkd[i][0] = -1 / dx;
      _x1stBkd[i][1] = 1 / dx;
    }
  }

  public PDEGrid1D(final MeshingFunction timeMesh, MeshingFunction spaceMesh) {
    this(timeMesh.getPoints(), spaceMesh.getPoints());
  }

  public PDEGrid1D(final double[] timeGrid, final double[] spaceGrid) {
    final int tNodes = timeGrid.length;
    final int xNodes = spaceGrid.length;
    Validate.isTrue(tNodes > 1, "need at least 2 time nodes");
    Validate.isTrue(xNodes > 2, "need at least 3 space nodes");

    _nSpaceNodes = xNodes;
    _tNodes = timeGrid;
    _xNodes = spaceGrid;

    _dt = new double[tNodes - 1];
    for (int n = 0; n < tNodes - 1; n++) {
      _dt[n] = timeGrid[n + 1] - timeGrid[n];
      Validate.isTrue(_dt[n] > 0, "time steps must be increasing");
    }

    _dx = new double[xNodes - 1];
    for (int i = 0; i < xNodes - 1; i++) {
      _dx[i] = spaceGrid[i + 1] - spaceGrid[i];
      Validate.isTrue(_dx[i] > 0, "space steps must be increasing");
    }

    _x1st = new double[xNodes - 2][3];
    _x2nd = new double[xNodes - 2][3];
    for (int i = 0; i < xNodes - 2; i++) {
      _x1st[i][0] = -_dx[i + 1] / _dx[i] / (_dx[i] + _dx[i + 1]);
      _x1st[i][1] = (_dx[i + 1] - _dx[i]) / _dx[i] / _dx[i + 1];
      _x1st[i][2] = _dx[i] / _dx[i + 1] / (_dx[i] + _dx[i + 1]);
      _x2nd[i][0] = 2 / _dx[i] / (_dx[i] + _dx[i + 1]);
      _x2nd[i][1] = -2 / _dx[i] / _dx[i + 1];
      _x2nd[i][2] = 2 / _dx[i + 1] / (_dx[i] + _dx[i + 1]);
    }

    _x1stFwd = new double[xNodes - 1][2];
    _x1stBkd = new double[xNodes - 1][2];
    for (int i = 0; i < xNodes - 1; i++) {
      _x1stFwd[i][0] = -1 / _dx[i];
      _x1stFwd[i][1] = 1 / _dx[i];
      _x1stBkd[i][0] = -1 / _dx[i];
      _x1stBkd[i][1] = 1 / _dx[i];
    }

  }

  public int getNumTimeNodes() {
    return _tNodes.length;
  }

  public int getNumSpaceNodes() {
    return _xNodes.length;
  }

  public double getTimeNode(final int n) {
    return _tNodes[n];
  }

  public double getSpaceNode(final int i) {
    return _xNodes[i];
  }

  public double getTimeStep(final int n) {
    return _dt[n];
  }

  public double getSpaceStep(final int i) {
    return _dx[i];
  }

  public double[] getTimeNodes() {
    return _tNodes;
  }

  public double[] getSpaceNodes() {
    return _xNodes;
  }

  public int getLowerBoundIndexForTime(final double time) {
    return getLowerBoundIndex(_tNodes, time);
  }

  public int getLowerBoundIndexForSpace(final double space) {
    return getLowerBoundIndex(_xNodes, space);
  }

  public double[] getFirstDerivativeCoefficients(final int i) {
    Validate.isTrue(i > 0 && i < _nSpaceNodes - 1, "Can't take central difference at first or last node. Use Forward or backwards");
    return _x1st[i - 1];
  }

  public double[] getFirstDerivativeForwardCoefficients(final int i) {
    Validate.isTrue(i < _nSpaceNodes - 1, "Can't take forward difference at last node. Use central or backwards");
    return _x1stFwd[i];
  }

  public double[] getFirstDerivativeBackwardCoefficients(final int i) {
    Validate.isTrue(i > 0, "Can't take backwards difference at first node. Use central or forwards");
    return _x1stBkd[i - 1];
  }

  public double[] getSecondDerivativeCoefficients(final int i) {
    if (i == 0) {
      return _x2nd[0]; // TODO check this is still the best 3-point when the grid is non-uniform
    } else if (i == _nSpaceNodes - 1) {
      return _x2nd[_nSpaceNodes - 3];
    }
    return _x2nd[i - 1];
  }

  public PDEGrid1D withDoubleTimeSteps() {
    double[] timeGrid = new double[_tNodes.length * 2 - 1];
    for (int i = 0; i < _tNodes.length - 1; i++) {
      timeGrid[2 * i] = _tNodes[i];
      timeGrid[2 * i + 1] = (_tNodes[i] + _tNodes[i + 1]) / 2.0;
    }
    timeGrid[2 * (_tNodes.length - 1)] = _tNodes[_tNodes.length - 1];
    return new PDEGrid1D(timeGrid, _xNodes);
  }

  private int getLowerBoundIndex(final double[] array, final double t) {
    final int n = array.length;
    if (t < array[0]) {
      return 0;
    }
    if (t > array[n - 1]) {
      return n - 1;
    }

    int index = Arrays.binarySearch(array, t);
    if (index >= 0) {
      // Fast break out if it's an exact match.
      return index;
    }
    if (index < 0) {
      index = -(index + 1);
      index--;
    }
    return index;
  }

}
