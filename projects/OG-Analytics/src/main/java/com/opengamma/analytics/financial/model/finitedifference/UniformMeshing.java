/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.finitedifference;

import java.util.Arrays;

import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.NaturalCubicSplineInterpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DCubicSplineDataBundle;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class UniformMeshing extends MeshingFunction {

  private static final Interpolator1D INTERPOLATOR = new NaturalCubicSplineInterpolator1D();
  private final Interpolator1DDataBundle _db;
  private final int[] _fpIndicies;
  private final double[] _fpValues;

  /**
   * Crates a set of points (mesh) equally spaced between 0.0 and 1.0 inclusive 
   * @param nPoints Number of points in the mesh 
   */
  public UniformMeshing(int nPoints) {
    super(nPoints);
    _db = null;
    _fpIndicies = null;
    _fpValues = null;
  }

  /**
   * Crates a set of points (mesh) approximately equally spaced between 0.0 and 1.0 inclusive, such that the specified <em>fixedPoints</em> are included as points 
   * @param nPoints Number of points in the mesh 
   * @param fixedPoints Set of points that must be in the mesh. <b>Note:</b> the mesh can be far from uniform if fixed-points are close together and/or a small 
   * number of points are used
   */
  public UniformMeshing(int nPoints, final double[] fixedPoints) {
    super(nPoints);
    ArgumentChecker.notNull(fixedPoints, "null fixed Points");

    // sort and remove duplicates, preserving order
    _fpValues = FunctionUtils.unique(fixedPoints);
    // remove any fixed points on the boundary
//    int nn = temp.length;
//    if (nn > 0 && temp[0] == 0.0) {
//      temp = Arrays.copyOfRange(temp, 1, nn - 1);
//    }
//    nn = temp.length;
//    if (nn > 0 && temp[nn - 1] == 1.0) {
//      temp = Arrays.copyOfRange(temp, 0, nn - 2);
//    }
//    _fpValues = temp;
    final int m = _fpValues.length;
    if (m == 0) {
      _db = null;
      _fpIndicies = null;
    } else {
      if (_fpValues[0] <= 0.0 || _fpValues[m - 1] >= 1.0) {
        throw new IllegalArgumentException("fixedPoints must be between 0.0 and 1.0 exclusive");
      }
      if (super.getNumberOfPoints() - m < 2) {
        throw new IllegalArgumentException("insufficient points to form grid");
      }

      _fpIndicies = new int[m];
      for (int ii = 0; ii < m; ii++) {
        _fpIndicies[ii] = (int) Math.round((nPoints - 1) * _fpValues[ii]);
      }
      // prevent points sharing index
      if (m != FunctionUtils.unique(_fpIndicies).length) {
        for (int ii = 1; ii < m; ii++) {
          int step = _fpIndicies[ii] - _fpIndicies[ii - 1];
          if (step < 1) {
            _fpIndicies[ii] += 1 - step;
          }
        }
      }

      if (_fpIndicies[0] == 0) {
        _fpIndicies[0] = 1;
        for (int ii = 1; ii < m; ii++) {
          if (_fpIndicies[ii - 1] == _fpIndicies[ii]) {
            _fpIndicies[ii] = _fpIndicies[ii] + 1;
          } else {
            break; // no more work to do
          }
        }
      }

      if (_fpIndicies[m - 1] >= nPoints - 1) {
        _fpIndicies[m - 1] = nPoints - 2;
        for (int ii = 1; ii < m - 1; ii++) {
          int step = _fpIndicies[m - ii] - _fpIndicies[m - ii - 1];
          if (step < 1) {
            _fpIndicies[m - ii - 1] += step - 1;
          } else {
            break;
          }
        }
      }

      final double[] x = new double[m + 2];
      final double[] y = new double[m + 2];
      x[0] = 0.0;
      for (int ii = 0; ii < m; ii++) {
        x[ii + 1] = _fpIndicies[ii];
      }
      x[m + 1] = nPoints - 1;
      y[0] = 0.0;
      System.arraycopy(_fpValues, 0, y, 1, m);
      y[m + 1] = 1.0;
      Interpolator1DDataBundle data = INTERPOLATOR.getDataBundleFromSortedArrays(x, y);
      final double grad = 1.0 / (nPoints - 1);
      _db = new Interpolator1DCubicSplineDataBundle(data, grad, grad);
    }
  }

  protected int getFixedPointIndex(int i) {
    return Arrays.binarySearch(_fpIndicies, i);
  }

  @Override
  public Double evaluate(Integer x) {
    if (x < 0 || x >= getNumberOfPoints()) {
      throw new IllegalArgumentException("index out of range");
    }

    if (x == 0) {
      return 0.0;
    }
    if (x == getNumberOfPoints() - 1) {
      return 1.0;
    }

    if (_db == null) {
      return ((double) x) / (getNumberOfPoints() - 1);
    }

    // avoid interpolator lookup if requested value is a fixed value
    final int index = getFixedPointIndex(x);
    if (index >= 0) {
      return _fpValues[index];
    }

    return INTERPOLATOR.interpolate(_db, (double) x);
  }

}
