/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import java.util.Arrays;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.surface.NodalObjectsSurface;

/**
 * 
 */
public class CapletNodalSurfaceProvider extends Function1D<DoubleMatrix1D, NodalObjectsSurface> {

  private static final MatrixAlgebra MA = new ColtMatrixAlgebra();
  private final Integer[] _timeIntegerNodes;
  private final Integer[] _strikeIntegerNodes;

  private final double[] _fixingTimes;
  private final double[] _strikes;

  public CapletNodalSurfaceProvider(final double[] strikes, final double[] fixingTimes) {
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
  public NodalObjectsSurface evaluate(final DoubleMatrix1D x) {
    final int len = x.getNumberOfElements();
    final Double[] data = new Double[len];
    for (int i = 0; i < len; ++i) {
      data[i] = x.getEntry(i);
    }
    NodalObjectsSurface sur = new NodalObjectsSurface(_strikeIntegerNodes, _timeIntegerNodes, data);
    return new NodalObjectsSurface(_strikeIntegerNodes, _timeIntegerNodes, data);
  }

  public int getNumberOfNodes() {
    return _timeIntegerNodes.length;
  }

  public double[] getFixingTimes() {
    return _fixingTimes;
  }

  public double[] getStrikes() {
    return _strikes;
  }

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

    DoubleMatrix2D penaltyTime = new DoubleMatrix2D(timeMatrix);
    DoubleMatrix2D penaltyStrike = new DoubleMatrix2D(strikeMatrix);
    return (DoubleMatrix2D) MA.add(MA.multiply(MA.getTranspose(penaltyTime), penaltyTime), MA.multiply(MA.getTranspose(penaltyStrike), penaltyStrike));
    //    return (DoubleMatrix2D) MA.multiply(MA.getTranspose(penaltyStrike), penaltyStrike);
  }

  //  private static final double TIME_TOL = 0.01 / 365.0;
  //  private static final double STRIKE_TOL = 1.0e-8;
  //  //  private final double [] _strikes;
  //  //  private final double [] _endTimes;
  //
  //  
  //  
  //  
  //  
  //  private final List<CapletGridPoint> _gridPoints;
  //
  //  public CapletGridProvider(final List<CapFloor> caps) {
  //    ArgumentChecker.notNull(caps, "caps");
  //    _gridPoints = new ArrayList<>();
  //    final List<Double> strikeList = new ArrayList<>();
  //    final List<Double> timeList = new ArrayList<>();
  //
  //    for (final CapFloor cap : caps) {
  //      final int nCaplets = cap.getNumberOfPayments();
  //      for (int i = 0; i < nCaplets; ++i) {
  //        final CapFloorIbor caplet = cap.getNthPayment(i);
  //        final CapletGridPoint grid = new CapletGridPoint(caplet);
  //        if (!_gridPoints.contains(grid)) {
  //          _gridPoints.add(grid);
  //        }
  //      }
  //    }
  //  }
  //
  //  private class CapletGridPoint {
  //
  //    private final double[] _strikeAndTime;
  //    private Double _capletVol;
  //
  //    public CapletGridPoint(final CapFloorIbor caplet) {
  //      _strikeAndTime = new double[2];
  //      _strikeAndTime[0] = caplet.getStrike();
  //      _strikeAndTime[1] = caplet.getFixingTime();
  //      _capletVol = Double.NaN;
  //    }
  //
  //    public void setVol(final double vol) {
  //      _capletVol = vol;
  //    }
  //
  //    public Double getVol() {
  //      return _capletVol;
  //    }
  //
  //    public boolean checkSameGridPoint(final CapletGridPoint gridPoint1, final CapletGridPoint gridPoint2) {
  //      if (Math.abs(gridPoint1._strikeAndTime[0] - gridPoint2._strikeAndTime[0]) < TIME_TOL && Math.abs(gridPoint1._strikeAndTime[1] - gridPoint2._strikeAndTime[1]) < STRIKE_TOL) {
  //        return true;
  //      }
  //      return false;
  //    }
  //
  //    @Override
  //    public int hashCode() {
  //      final int prime = 31;
  //      int result = 1;
  //      result = prime * result + getOuterType().hashCode();
  //      result = prime * result + ((_capletVol == null) ? 0 : _capletVol.hashCode());
  //      result = prime * result + Arrays.hashCode(_strikeAndTime);
  //      return result;
  //    }
  //
  //    @Override
  //    public boolean equals(Object obj) {
  //      if (this == obj) {
  //        return true;
  //      }
  //      if (obj == null) {
  //        return false;
  //      }
  //      if (!(obj instanceof CapletGridPoint)) {
  //        return false;
  //      }
  //      CapletGridPoint other = (CapletGridPoint) obj;
  //      if (!getOuterType().equals(other.getOuterType())) {
  //        return false;
  //      }
  //      if (_capletVol == null) {
  //        if (other._capletVol != null) {
  //          return false;
  //        }
  //        //      } else if (!_capletVol.equals(other._capletVol)) {
  //        //        return false;
  //      } else if (checkSameGridPoint(this, other)) {
  //        return true;
  //      }
  //      //      if (!Arrays.equals(_strikeAndTime, other._strikeAndTime)) {
  //      //        return false;
  //      //      }
  //      return true;
  //    }
  //
  //    private CapletGridProvider getOuterType() {
  //      return CapletGridProvider.this;
  //    }
  //
  //  }

}
