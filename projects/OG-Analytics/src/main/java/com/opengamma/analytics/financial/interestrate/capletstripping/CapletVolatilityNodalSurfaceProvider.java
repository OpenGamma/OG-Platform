/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Arrays;

import com.opengamma.analytics.financial.interestrate.capletstrippingnew.CapletStrippingDirectGlobalWithPenalty;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.surface.NodalObjectsSurface;
import com.opengamma.util.ArgumentChecker;

/**
 * Nodal surface provider for {@link CapletStrippingDirectGlobalWithPenalty}
 */
public class CapletVolatilityNodalSurfaceProvider extends Function1D<DoubleMatrix1D, NodalObjectsSurface<Integer, Integer, Double>> {
  private static final MatrixAlgebra MA = new ColtMatrixAlgebra();
  private final Integer[] _timeIntegerNodes;
  private final Integer[] _strikeIntegerNodes;
  private final double[] _fixingTimes;
  private final double[] _strikes;

  /**
   * Constructor with strikes and fixing times for constituent caplets
   * @param strikes The strike set, sorted in ascending order
   * @param fixingTimes The fixing time set, sorted in ascending order
   */
  public CapletVolatilityNodalSurfaceProvider(final double[] strikes, final double[] fixingTimes) {
    ArgumentChecker.notNull(strikes, "strikes");
    ArgumentChecker.notNull(fixingTimes, "fixingTimes");

    final int nStrikes = strikes.length;
    final int nTimes = fixingTimes.length;
    _strikeIntegerNodes = new Integer[nTimes * nStrikes];
    _timeIntegerNodes = new Integer[nTimes * nStrikes];
    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nTimes; ++j) {
        _strikeIntegerNodes[nTimes * i + j] = i;
        _timeIntegerNodes[nTimes * i + j] = j;
      }
    }
    _strikes = Arrays.copyOf(strikes, nStrikes);
    _fixingTimes = Arrays.copyOf(fixingTimes, nTimes);
  }

  @Override
  public NodalObjectsSurface<Integer, Integer, Double> evaluate(final DoubleMatrix1D x) {
    ArgumentChecker.notNull(x, "x");

    final int len = x.getNumberOfElements();
    final Double[] data = new Double[len];
    for (int i = 0; i < len; ++i) {
      data[i] = x.getEntry(i);
    }
    return new NodalObjectsSurface<>(_strikeIntegerNodes, _timeIntegerNodes, data);
  }

  /**
   * @return total number of nodes on surface
   */
  public int getNumberOfNodes() {
    return _timeIntegerNodes.length;
  }

  /**
   * Access timeIntegerNodes
   * @return timeIntegerNodes
   */
  public Integer[] getTimeIntegerNodes() {
    return _timeIntegerNodes;
  }

  /**
   * Access strikeIntegerNodes
   * @return strikeIntegerNodes
   */
  public Integer[] getStrikeIntegerNodes() {
    return _strikeIntegerNodes;
  }

  /**
   * Access fixingTimes
   * @return fixingTimes
   */
  public double[] getFixingTimes() {
    return _fixingTimes;
  }

  /**
   * Access strikes
   * @return strikes
   */
  public double[] getStrikes() {
    return _strikes;
  }

  /**
   * Compute penalty matrix for a vector {vol(k0,t0), vol(k0,t1), vol(k0,t2), .... , vol(k1,t0), vol(k1,t1), .... , vol(k2,t0), ....}
   * @param smoothStrike The strike smoothing parameter
   * @param smoothTime The time smoothing parameter
   * @return The penalty matrix
   */
  public DoubleMatrix2D getPenaltyMatrix(final double smoothStrike, final double smoothTime) {
    final int nVols = getNumberOfNodes();
    final double[][] timeMatrix = new double[nVols][nVols];
    final double[][] strikeMatrix = new double[nVols][nVols];

    final int nStrikes = _strikes.length;
    final int nTimes = _fixingTimes.length;
    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nTimes; ++j) {
        if (j != 0 && j != nTimes - 1) {
          final double dtInv = 1.0 / (_fixingTimes[j + 1] - _fixingTimes[j]);
          final double dtmInv = 1.0 / (_fixingTimes[j] - _fixingTimes[j - 1]);
          timeMatrix[nTimes * i + j][nTimes * i + j + 1] = smoothTime * dtInv * dtmInv;
          timeMatrix[nTimes * i + j][nTimes * i + j] = -smoothTime * dtmInv * (dtInv + dtmInv);
          timeMatrix[nTimes * i + j][nTimes * i + j - 1] = smoothTime * dtmInv * dtmInv;
        }

        if (i != 0 && i != nStrikes - 1) {
          final double dkInv = 1.0 / (_strikes[i + 1] - _strikes[i]);
          final double dkmInv = 1.0 / (_strikes[i] - _strikes[i - 1]);
          strikeMatrix[nTimes * i + j][nTimes * (i + 1) + j] = smoothStrike * dkInv * dkmInv;
          strikeMatrix[nTimes * i + j][nTimes * i + j] = -smoothStrike * dkmInv * (dkInv + dkmInv);
          strikeMatrix[nTimes * i + j][nTimes * (i - 1) + j] = smoothStrike * dkmInv * dkmInv;
        }
      }
    }

    final DoubleMatrix2D penaltyTime = new DoubleMatrix2D(timeMatrix);
    final DoubleMatrix2D penaltyStrike = new DoubleMatrix2D(strikeMatrix);
    return (DoubleMatrix2D) MA.add(MA.multiply(MA.getTranspose(penaltyTime), penaltyTime), MA.multiply(MA.getTranspose(penaltyStrike), penaltyStrike));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_fixingTimes);
    result = prime * result + Arrays.hashCode(_strikeIntegerNodes);
    result = prime * result + Arrays.hashCode(_strikes);
    result = prime * result + Arrays.hashCode(_timeIntegerNodes);
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
    if (!(obj instanceof CapletVolatilityNodalSurfaceProvider)) {
      return false;
    }
    final CapletVolatilityNodalSurfaceProvider other = (CapletVolatilityNodalSurfaceProvider) obj;
    if (!Arrays.equals(_strikeIntegerNodes, other._strikeIntegerNodes)) {
      return false;
    }
    if (!Arrays.equals(_timeIntegerNodes, other._timeIntegerNodes)) {
      return false;
    }
    if (!Arrays.equals(_strikes, other._strikes)) {
      return false;
    }
    if (!Arrays.equals(_fixingTimes, other._fixingTimes)) {
      return false;
    }
    return true;
  }
}
