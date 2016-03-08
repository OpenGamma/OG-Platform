/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.function;

import java.io.Serializable;

import com.opengamma.analytics.math.interpolation.PiecewisePolynomialResult2D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class PiecewisePolynomialFunction2D implements Serializable {

  /**
   * Default constructor
   */
  public PiecewisePolynomialFunction2D() {

  }

  /**
   * 
   * @param pp PiecewisePolynomialResult2D
   * @param x0Key 
   * @param x1Key 
   * @return Value of piecewise polynomial function in 2D at (x0Key, x1Key)
   */
  public double evaluate(final PiecewisePolynomialResult2D pp, final double x0Key, final double x1Key) {
    ArgumentChecker.notNull(pp, "pp");

    ArgumentChecker.isFalse(Double.isNaN(x0Key), "x0Key containing NaN");
    ArgumentChecker.isFalse(Double.isInfinite(x0Key), "x0Key containing Infinity");
    ArgumentChecker.isFalse(Double.isNaN(x1Key), "x1Key containing NaN");
    ArgumentChecker.isFalse(Double.isInfinite(x1Key), "x1Key containing Infinity");

    final double[] knots0 = pp.getKnots0().getData();
    final double[] knots1 = pp.getKnots1().getData();
    final int nKnots0 = knots0.length;
    final int nKnots1 = knots1.length;

    int ind0 = 0;
    int ind1 = 0;

    for (int k = 1; k < nKnots0 - 1; ++k) {
      if (x0Key >= knots0[k]) {
        ind0 = k;
      }
    }

    for (int i = 1; i < nKnots1 - 1; ++i) {
      if (x1Key >= knots1[i]) {
        ind1 = i;
      }
    }
    final double res = getValue(pp.getCoefs()[ind0][ind1], x0Key, x1Key, knots0[ind0], knots1[ind1]);

    ArgumentChecker.isFalse(Double.isInfinite(res), "Too large input");
    ArgumentChecker.isFalse(Double.isNaN(res), "Too large input");

    return res;
  }

  /**
   * @param pp PiecewisePolynomialResult2D
   * @param x0Keys 
   * @param x1Keys 
   * @return Values of piecewise polynomial function in 2D at (x0Keys_i, x1Keys_j)
   */
  public DoubleMatrix2D evaluate(final PiecewisePolynomialResult2D pp, final double[] x0Keys, final double[] x1Keys) {
    ArgumentChecker.notNull(pp, "pp");
    ArgumentChecker.notNull(x0Keys, "x0Keys");
    ArgumentChecker.notNull(x1Keys, "x1Keys");

    final int n0Keys = x0Keys.length;
    final int n1Keys = x1Keys.length;

    for (int i = 0; i < n0Keys; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(x0Keys[i]), "x0Keys containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(x0Keys[i]), "x0Keys containing Infinity");
    }
    for (int i = 0; i < n1Keys; ++i) {
      ArgumentChecker.isFalse(Double.isNaN(x1Keys[i]), "x1Keys containing NaN");
      ArgumentChecker.isFalse(Double.isInfinite(x1Keys[i]), "x1Keys containing Infinity");
    }

    final double[] knots0 = pp.getKnots0().getData();
    final double[] knots1 = pp.getKnots1().getData();
    final int nKnots0 = knots0.length;
    final int nKnots1 = knots1.length;

    double[][] res = new double[n0Keys][n1Keys];

    for (int i = 0; i < n0Keys; ++i) {
      for (int j = 0; j < n1Keys; ++j) {
        int ind0 = 0;
        int ind1 = 0;

        for (int k = 1; k < nKnots0 - 1; ++k) {
          if (x0Keys[i] >= knots0[k]) {
            ind0 = k;
          }
        }
        for (int k = 1; k < nKnots1 - 1; ++k) {
          if (x1Keys[j] >= knots1[k]) {
            ind1 = k;
          }
        }
        res[i][j] = getValue(pp.getCoefs()[ind0][ind1], x0Keys[i], x1Keys[j], knots0[ind0], knots1[ind1]);
        ArgumentChecker.isFalse(Double.isInfinite(res[i][j]), "Too large input");
        ArgumentChecker.isFalse(Double.isNaN(res[i][j]), "Too large input");
      }
    }

    return new DoubleMatrix2D(res);
  }

  /** 
   * @param pp PiecewisePolynomialResult2D
   * @param x0Key 
   * @param x1Key 
   * @return Value of first derivative of two-dimensional piecewise polynomial function with respect to x0 at (x0Keys_i, x1Keys_j)
   */
  public double differentiateX0(final PiecewisePolynomialResult2D pp, final double x0Key, final double x1Key) {
    ArgumentChecker.notNull(pp, "pp");
    final int order0 = pp.getOrder()[0];
    final int order1 = pp.getOrder()[1];
    ArgumentChecker.isFalse(order0 < 2, "polynomial degree of x0 < 1");

    final DoubleMatrix1D knots0 = pp.getKnots0();
    final DoubleMatrix1D knots1 = pp.getKnots1();
    final int nKnots0 = knots0.getNumberOfElements();
    final int nKnots1 = knots1.getNumberOfElements();
    DoubleMatrix2D[][] coefs = pp.getCoefs();

    DoubleMatrix2D[][] res = new DoubleMatrix2D[nKnots0][nKnots1];

    for (int i = 0; i < nKnots0 - 1; ++i) {
      for (int j = 0; j < nKnots1 - 1; ++j) {
        double[][] matTmp = new double[order0 - 1][order1];
        for (int k = 0; k < order0 - 1; ++k) {
          for (int l = 0; l < order1; ++l) {
            matTmp[k][l] = coefs[i][j].getData()[k][l] * (order0 - k - 1);
          }
        }
        res[i][j] = new DoubleMatrix2D(matTmp);
      }
    }

    PiecewisePolynomialResult2D ppDiff = new PiecewisePolynomialResult2D(knots0, knots1, res, new int[] {order0 - 1, order1 });

    return evaluate(ppDiff, x0Key, x1Key);
  }

  /** 
   * @param pp PiecewisePolynomialResult2D
   * @param x0Key 
   * @param x1Key 
   * @return Value of first derivative of two-dimensional piecewise polynomial function with respect to x1 at (x0Keys_i, x1Keys_j)
   */
  public double differentiateX1(final PiecewisePolynomialResult2D pp, final double x0Key, final double x1Key) {
    ArgumentChecker.notNull(pp, "pp");
    final int order0 = pp.getOrder()[0];
    final int order1 = pp.getOrder()[1];
    ArgumentChecker.isFalse(order1 < 2, "polynomial degree of x1 < 1");

    final DoubleMatrix1D knots0 = pp.getKnots0();
    final DoubleMatrix1D knots1 = pp.getKnots1();
    final int nKnots0 = knots0.getNumberOfElements();
    final int nKnots1 = knots1.getNumberOfElements();
    DoubleMatrix2D[][] coefs = pp.getCoefs();

    DoubleMatrix2D[][] res = new DoubleMatrix2D[nKnots0][nKnots1];

    for (int i = 0; i < nKnots0 - 1; ++i) {
      for (int j = 0; j < nKnots1 - 1; ++j) {
        double[][] matTmp = new double[order0][order1 - 1];
        for (int k = 0; k < order0; ++k) {
          for (int l = 0; l < order1 - 1; ++l) {
            matTmp[k][l] = coefs[i][j].getData()[k][l] * (order1 - l - 1);
          }
        }
        res[i][j] = new DoubleMatrix2D(matTmp);
      }
    }

    PiecewisePolynomialResult2D ppDiff = new PiecewisePolynomialResult2D(knots0, knots1, res, new int[] {order0, order1 - 1 });

    return evaluate(ppDiff, x0Key, x1Key);
  }

  /** 
   * @param pp PiecewisePolynomialResult2D
   * @param x0Keys 
   * @param x1Keys 
   * @return Values of first derivative of two-dimensional piecewise polynomial function with respect to x0 at (x0Keys_i, x1Keys_j)
   */
  public DoubleMatrix2D differentiateX0(final PiecewisePolynomialResult2D pp, final double[] x0Keys, final double[] x1Keys) {
    ArgumentChecker.notNull(pp, "pp");
    final int order0 = pp.getOrder()[0];
    final int order1 = pp.getOrder()[1];
    ArgumentChecker.isFalse(order0 < 2, "polynomial degree of x0 < 1");

    final DoubleMatrix1D knots0 = pp.getKnots0();
    final DoubleMatrix1D knots1 = pp.getKnots1();
    final int nKnots0 = knots0.getNumberOfElements();
    final int nKnots1 = knots1.getNumberOfElements();
    DoubleMatrix2D[][] coefs = pp.getCoefs();

    DoubleMatrix2D[][] res = new DoubleMatrix2D[nKnots0][nKnots1];

    for (int i = 0; i < nKnots0 - 1; ++i) {
      for (int j = 0; j < nKnots1 - 1; ++j) {
        double[][] matTmp = new double[order0 - 1][order1];
        for (int k = 0; k < order0 - 1; ++k) {
          for (int l = 0; l < order1; ++l) {
            matTmp[k][l] = coefs[i][j].getData()[k][l] * (order0 - k - 1);
          }
        }
        res[i][j] = new DoubleMatrix2D(matTmp);
      }
    }

    PiecewisePolynomialResult2D ppDiff = new PiecewisePolynomialResult2D(knots0, knots1, res, new int[] {order0 - 1, order1 });

    return evaluate(ppDiff, x0Keys, x1Keys);
  }

  /** 
   * @param pp PiecewisePolynomialResult2D
   * @param x0Keys 
   * @param x1Keys 
   * @return Values of first derivative of two-dimensional piecewise polynomial function with respect to x1 at (x0Keys_i, x1Keys_j)
   */
  public DoubleMatrix2D differentiateX1(final PiecewisePolynomialResult2D pp, final double[] x0Keys, final double[] x1Keys) {
    ArgumentChecker.notNull(pp, "pp");
    final int order0 = pp.getOrder()[0];
    final int order1 = pp.getOrder()[1];
    ArgumentChecker.isFalse(order1 < 2, "polynomial degree of x1 < 1");

    final DoubleMatrix1D knots0 = pp.getKnots0();
    final DoubleMatrix1D knots1 = pp.getKnots1();
    final int nKnots0 = knots0.getNumberOfElements();
    final int nKnots1 = knots1.getNumberOfElements();
    DoubleMatrix2D[][] coefs = pp.getCoefs();

    DoubleMatrix2D[][] res = new DoubleMatrix2D[nKnots0][nKnots1];

    for (int i = 0; i < nKnots0 - 1; ++i) {
      for (int j = 0; j < nKnots1 - 1; ++j) {
        double[][] matTmp = new double[order0][order1 - 1];
        for (int k = 0; k < order0; ++k) {
          for (int l = 0; l < order1 - 1; ++l) {
            matTmp[k][l] = coefs[i][j].getData()[k][l] * (order1 - l - 1);
          }
        }
        res[i][j] = new DoubleMatrix2D(matTmp);
      }
    }

    PiecewisePolynomialResult2D ppDiff = new PiecewisePolynomialResult2D(knots0, knots1, res, new int[] {order0, order1 - 1 });

    return evaluate(ppDiff, x0Keys, x1Keys);
  }

  /** 
   * @param pp PiecewisePolynomialResult2D
   * @param x0Key 
   * @param x1Key 
   * @return Value of cross derivative of two-dimensional piecewise polynomial function at (x0Keys_i, x1Keys_j)
   */
  public double differentiateCross(final PiecewisePolynomialResult2D pp, final double x0Key, final double x1Key) {
    ArgumentChecker.notNull(pp, "pp");
    final int order0 = pp.getOrder()[0];
    final int order1 = pp.getOrder()[1];
    ArgumentChecker.isFalse(order0 < 2, "polynomial degree of x0 < 1");
    ArgumentChecker.isFalse(order1 < 2, "polynomial degree of x1 < 1");

    final DoubleMatrix1D knots0 = pp.getKnots0();
    final DoubleMatrix1D knots1 = pp.getKnots1();
    final int nKnots0 = knots0.getNumberOfElements();
    final int nKnots1 = knots1.getNumberOfElements();
    DoubleMatrix2D[][] coefs = pp.getCoefs();

    DoubleMatrix2D[][] res = new DoubleMatrix2D[nKnots0][nKnots1];

    for (int i = 0; i < nKnots0 - 1; ++i) {
      for (int j = 0; j < nKnots1 - 1; ++j) {
        double[][] matTmp = new double[order0 - 1][order1 - 1];
        for (int k = 0; k < order0 - 1; ++k) {
          for (int l = 0; l < order1 - 1; ++l) {
            matTmp[k][l] = coefs[i][j].getData()[k][l] * (order1 - l - 1) * (order0 - k - 1);
          }
        }
        res[i][j] = new DoubleMatrix2D(matTmp);
      }
    }

    PiecewisePolynomialResult2D ppDiff = new PiecewisePolynomialResult2D(knots0, knots1, res, new int[] {order0 - 1, order1 - 1 });

    return evaluate(ppDiff, x0Key, x1Key);
  }

  /** 
   * @param pp PiecewisePolynomialResult2D
   * @param x0Key 
   * @param x1Key 
   * @return Value of second derivative of two-dimensional piecewise polynomial function with respect to x0 at (x0Keys_i, x1Keys_j)
   */
  public double differentiateTwiceX0(final PiecewisePolynomialResult2D pp, final double x0Key, final double x1Key) {
    ArgumentChecker.notNull(pp, "pp");
    final int order0 = pp.getOrder()[0];
    final int order1 = pp.getOrder()[1];
    ArgumentChecker.isFalse(order0 < 3, "polynomial degree of x0 < 2");

    final DoubleMatrix1D knots0 = pp.getKnots0();
    final DoubleMatrix1D knots1 = pp.getKnots1();
    final int nKnots0 = knots0.getNumberOfElements();
    final int nKnots1 = knots1.getNumberOfElements();
    DoubleMatrix2D[][] coefs = pp.getCoefs();

    DoubleMatrix2D[][] res = new DoubleMatrix2D[nKnots0][nKnots1];

    for (int i = 0; i < nKnots0 - 1; ++i) {
      for (int j = 0; j < nKnots1 - 1; ++j) {
        double[][] matTmp = new double[order0 - 2][order1];
        for (int k = 0; k < order0 - 2; ++k) {
          for (int l = 0; l < order1; ++l) {
            matTmp[k][l] = coefs[i][j].getData()[k][l] * (order0 - k - 1) * (order0 - k - 2);
          }
        }
        res[i][j] = new DoubleMatrix2D(matTmp);
      }
    }

    PiecewisePolynomialResult2D ppDiff = new PiecewisePolynomialResult2D(knots0, knots1, res, new int[] {order0 - 2, order1 });

    return evaluate(ppDiff, x0Key, x1Key);
  }

  /** 
   * @param pp PiecewisePolynomialResult2D
   * @param x0Key 
   * @param x1Key 
   * @return Value of second derivative of two-dimensional piecewise polynomial function with respect to x1 at (x0Keys_i, x1Keys_j)
   */
  public double differentiateTwiceX1(final PiecewisePolynomialResult2D pp, final double x0Key, final double x1Key) {
    ArgumentChecker.notNull(pp, "pp");
    final int order0 = pp.getOrder()[0];
    final int order1 = pp.getOrder()[1];
    ArgumentChecker.isFalse(order1 < 3, "polynomial degree of x1 < 2");

    final DoubleMatrix1D knots0 = pp.getKnots0();
    final DoubleMatrix1D knots1 = pp.getKnots1();
    final int nKnots0 = knots0.getNumberOfElements();
    final int nKnots1 = knots1.getNumberOfElements();
    DoubleMatrix2D[][] coefs = pp.getCoefs();

    DoubleMatrix2D[][] res = new DoubleMatrix2D[nKnots0][nKnots1];

    for (int i = 0; i < nKnots0 - 1; ++i) {
      for (int j = 0; j < nKnots1 - 1; ++j) {
        double[][] matTmp = new double[order0][order1 - 2];
        for (int k = 0; k < order0; ++k) {
          for (int l = 0; l < order1 - 2; ++l) {
            matTmp[k][l] = coefs[i][j].getData()[k][l] * (order1 - l - 1) * (order1 - l - 2);
          }
        }
        res[i][j] = new DoubleMatrix2D(matTmp);
      }
    }

    PiecewisePolynomialResult2D ppDiff = new PiecewisePolynomialResult2D(knots0, knots1, res, new int[] {order0, order1 - 2 });

    return evaluate(ppDiff, x0Key, x1Key);
  }

  /** 
   * @param pp PiecewisePolynomialResult2D
   * @param x0Keys 
   * @param x1Keys 
   * @return Values of cross derivative of two-dimensional piecewise polynomial function at (x0Keys_i, x1Keys_j)
   */
  public DoubleMatrix2D differentiateCross(final PiecewisePolynomialResult2D pp, final double[] x0Keys, final double[] x1Keys) {
    ArgumentChecker.notNull(pp, "pp");
    final int order0 = pp.getOrder()[0];
    final int order1 = pp.getOrder()[1];
    ArgumentChecker.isFalse(order0 < 2, "polynomial degree of x0 < 1");
    ArgumentChecker.isFalse(order1 < 2, "polynomial degree of x1 < 1");

    final DoubleMatrix1D knots0 = pp.getKnots0();
    final DoubleMatrix1D knots1 = pp.getKnots1();
    final int nKnots0 = knots0.getNumberOfElements();
    final int nKnots1 = knots1.getNumberOfElements();
    DoubleMatrix2D[][] coefs = pp.getCoefs();

    DoubleMatrix2D[][] res = new DoubleMatrix2D[nKnots0][nKnots1];

    for (int i = 0; i < nKnots0 - 1; ++i) {
      for (int j = 0; j < nKnots1 - 1; ++j) {
        double[][] matTmp = new double[order0 - 1][order1 - 1];
        for (int k = 0; k < order0 - 1; ++k) {
          for (int l = 0; l < order1 - 1; ++l) {
            matTmp[k][l] = coefs[i][j].getData()[k][l] * (order1 - l - 1) * (order0 - k - 1);
          }
        }
        res[i][j] = new DoubleMatrix2D(matTmp);
      }
    }

    PiecewisePolynomialResult2D ppDiff = new PiecewisePolynomialResult2D(knots0, knots1, res, new int[] {order0 - 1, order1 - 1 });

    return evaluate(ppDiff, x0Keys, x1Keys);
  }

  /** 
   * @param pp PiecewisePolynomialResult2D
   * @param x0Keys 
   * @param x1Keys 
   * @return Values of second derivative of two-dimensional piecewise polynomial function with respect to x0 at (x0Keys_i, x1Keys_j)
   */
  public DoubleMatrix2D differentiateTwiceX0(final PiecewisePolynomialResult2D pp, final double[] x0Keys, final double[] x1Keys) {
    ArgumentChecker.notNull(pp, "pp");
    final int order0 = pp.getOrder()[0];
    final int order1 = pp.getOrder()[1];
    ArgumentChecker.isFalse(order0 < 3, "polynomial degree of x0 < 2");

    final DoubleMatrix1D knots0 = pp.getKnots0();
    final DoubleMatrix1D knots1 = pp.getKnots1();
    final int nKnots0 = knots0.getNumberOfElements();
    final int nKnots1 = knots1.getNumberOfElements();
    DoubleMatrix2D[][] coefs = pp.getCoefs();

    DoubleMatrix2D[][] res = new DoubleMatrix2D[nKnots0][nKnots1];

    for (int i = 0; i < nKnots0 - 1; ++i) {
      for (int j = 0; j < nKnots1 - 1; ++j) {
        double[][] matTmp = new double[order0 - 2][order1];
        for (int k = 0; k < order0 - 2; ++k) {
          for (int l = 0; l < order1; ++l) {
            matTmp[k][l] = coefs[i][j].getData()[k][l] * (order0 - k - 1) * (order0 - k - 2);
          }
        }
        res[i][j] = new DoubleMatrix2D(matTmp);
      }
    }

    PiecewisePolynomialResult2D ppDiff = new PiecewisePolynomialResult2D(knots0, knots1, res, new int[] {order0 - 2, order1 });

    return evaluate(ppDiff, x0Keys, x1Keys);
  }

  /** 
   * @param pp PiecewisePolynomialResult2D
   * @param x0Keys 
   * @param x1Keys 
   * @return Values of second derivative of two-dimensional piecewise polynomial function with respect to x1 at (x0Keys_i, x1Keys_j)
   */
  public DoubleMatrix2D differentiateTwiceX1(final PiecewisePolynomialResult2D pp, final double[] x0Keys, final double[] x1Keys) {
    ArgumentChecker.notNull(pp, "pp");
    final int order0 = pp.getOrder()[0];
    final int order1 = pp.getOrder()[1];
    ArgumentChecker.isFalse(order1 < 3, "polynomial degree of x1 < 2");

    final DoubleMatrix1D knots0 = pp.getKnots0();
    final DoubleMatrix1D knots1 = pp.getKnots1();
    final int nKnots0 = knots0.getNumberOfElements();
    final int nKnots1 = knots1.getNumberOfElements();
    DoubleMatrix2D[][] coefs = pp.getCoefs();

    DoubleMatrix2D[][] res = new DoubleMatrix2D[nKnots0][nKnots1];

    for (int i = 0; i < nKnots0 - 1; ++i) {
      for (int j = 0; j < nKnots1 - 1; ++j) {
        double[][] matTmp = new double[order0][order1 - 2];
        for (int k = 0; k < order0; ++k) {
          for (int l = 0; l < order1 - 2; ++l) {
            matTmp[k][l] = coefs[i][j].getData()[k][l] * (order1 - l - 1) * (order1 - l - 2);
          }
        }
        res[i][j] = new DoubleMatrix2D(matTmp);
      }
    }

    PiecewisePolynomialResult2D ppDiff = new PiecewisePolynomialResult2D(knots0, knots1, res, new int[] {order0, order1 - 2 });

    return evaluate(ppDiff, x0Keys, x1Keys);
  }

  /**
   * @param coefMat 
   * @param x0 
   * @param x1 
   * @param leftKnot0 
   * @param leftKnot1 
   * @return sum_{i=0}^{order0-1} sum_{j=0}^{order1-1} coefMat_{ij} (x0-leftKnots0)^{order0-1-i} (x1-leftKnots1)^{order0-1-j}
   */
  private double getValue(final DoubleMatrix2D coefMat, final double x0, final double x1, final double leftKnot0, final double leftKnot1) {

    final int order0 = coefMat.getNumberOfRows();
    final int order1 = coefMat.getNumberOfColumns();
    final double x0Mod = x0 - leftKnot0;
    final double x1Mod = x1 - leftKnot1;
    double res = 0.;

    for (int i = 0; i < order0; ++i) {
      for (int j = 0; j < order1; ++j) {
        res += coefMat.getData()[order0 - i - 1][order1 - j - 1] * Math.pow(x0Mod, i) * Math.pow(x1Mod, j);
      }
    }

    return res;
  }
}
