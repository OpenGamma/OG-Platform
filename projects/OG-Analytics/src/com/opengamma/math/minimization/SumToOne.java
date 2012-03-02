/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.math.minimization;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class SumToOne {
  private static final double TOL = 1e-9;
  private static final Map<Integer, int[][]> SETS = new HashMap<Integer, int[][]>();

  private int[][] _set;
  private int _n;

  public SumToOne(final int n) {
    _set = getSet(n);
    _n = n;
  }

  public double[] transform(final double[] fitParms) {
    ArgumentChecker.isTrue(fitParms.length == _n - 1, "length of fitParms is {}, but must be {} ", fitParms.length, _n - 1);
    double[] s2 = new double[_n - 1];
    double[] c2 = new double[_n - 1];
    for (int j = 0; j < _n - 1; j++) {
      double temp = Math.sin(fitParms[j]);
      temp *= temp;
      s2[j] = temp;
      c2[j] = 1.0 - temp;
    }

    double[] res = new double[_n];
    for (int i = 0; i < _n; i++) {
      double prod = 1.0;
      for (int j = 0; j < _n - 1; j++) {
        if (_set[i][j] == 1) {
          prod *= s2[j];
        } else if (_set[i][j] == -1) {
          prod *= c2[j];
        }
      }
      res[i] = prod;
    }
    return res;
  }

  public DoubleMatrix1D transform(final DoubleMatrix1D fitParms) {
    return new DoubleMatrix1D(transform(fitParms.getData()));
  }

  public double[] inverseTransform(final double[] modelParms) {
    ArgumentChecker.isTrue(modelParms.length == _n, "length of modelParms is {}, but must be {} ", modelParms.length, _n);

    double[] res = new double[_n - 1];
    double[] cum = new double[_n + 1];

    double sum = 0.0;
    for (int i = 0; i < _n; i++) {
      sum += modelParms[i];
      cum[i + 1] = sum;
    }
    ArgumentChecker.isTrue(Math.abs(sum - 1.0) < TOL, "sum of elements is {}. Must be 1.0", sum);

    cal(cum, 1.0, 0, _n, 0, res);

    for (int i = 0; i < _n - 1; i++) {
      res[i] = Math.asin(Math.sqrt(res[i]));
    }
    return res;
  }

  public DoubleMatrix1D inverseTransform(final DoubleMatrix1D modelParms) {
    return new DoubleMatrix1D(inverseTransform(modelParms.getData()));
  }

  public double[][] jacobian(final double[] fitParms) {
    ArgumentChecker.isTrue(fitParms.length == _n - 1, "length of fitParms is {}, but must be {} ", fitParms.length, _n - 1);
    double[] sin = new double[_n - 1];
    double[] cos = new double[_n - 1];
    for (int j = 0; j < _n - 1; j++) {
      sin[j] = Math.sin(fitParms[j]);
      cos[j] = Math.cos(fitParms[j]);
    }

    double[] a = new double[_n];
    for (int i = 0; i < _n; i++) {
      double prod = 1.0;
      for (int j = 0; j < _n - 1; j++) {
        if (_set[i][j] == 1) {
          prod *= sin[j];
        } else if (_set[i][j] == -1) {
          prod *= cos[j];
        }
      }
      a[i] = 2 * prod * prod;
    }

    double[][] res = new double[_n][_n - 1];
    for (int i = 0; i < _n; i++) {
      for (int j = 0; j < _n - 1; j++) {
        if (_set[i][j] == 1 && a[i] != 0.0) {
          res[i][j] = a[i] * cos[j] / sin[j];
        } else if (_set[i][j] == -1 && a[i] != 0.0) {
          res[i][j] = -a[i] * sin[j] / cos[j];
        }
      }
    }
    return res;
  }

  public DoubleMatrix2D jacobian(final DoubleMatrix1D fitParms) {
    return new DoubleMatrix2D(jacobian(fitParms.getData()));
  }

  private void cal(final double[] cum, final double factor, final int d, final int n, final int p1, final double[] res) {
    if (n == 1) {
      return;
    }
    int n1 = n / 2;
    int n2 = n - n1;
    double s = (cum[p1 + n1] - cum[p1]) * factor;
    double c = 1 - s;
    res[d] = s;
    cal(cum, factor / s, d + 1, n1, p1, res);
    cal(cum, factor / c, d + n1, n2, p1 + n1, res);
  }

  protected static int[][] getSet(int n) {
    ArgumentChecker.isTrue(n > 1, "need n>1");
    if (SETS.containsKey(n)) {
      return SETS.get(n);
    }
    int[][] res = new int[n][];
    switch (n) {
      case 2:
        res[0] = new int[] {1 };
        res[1] = new int[] {-1 };
        break;
      case 3:
        res[0] = new int[] {1, 0 };
        res[1] = new int[] {-1, 1 };
        res[2] = new int[] {-1, -1 };
        break;
      case 4:
        res[0] = new int[] {1, 1, 0 };
        res[1] = new int[] {1, -1, 0 };
        res[2] = new int[] {-1, 0, 1 };
        res[3] = new int[] {-1, 0, -1 };
        break;
      default:
        int n1 = n / 2;
        int n2 = n - n1;
        int[][] set1 = getSet(n1);
        int[][] set2 = (n1 == n2 ? set1 : getSet(n2));
        res = new int[n][n - 1];

        for (int i = 0; i < n1; i++) {
          res[i][0] = 1;
          System.arraycopy(set1[i], 0, res[i], 1, n1 - 1);
        }
        for (int i = 0; i < n2; i++) {
          res[i + n1][0] = -1;
          System.arraycopy(set2[i], 0, res[i + n1], n1, n2 - 1);
        }
    }
    SETS.put(n, res);
    return res;
  }

}
