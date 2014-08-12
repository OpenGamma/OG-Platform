/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstripping;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.ArgumentChecker;

/**
 * for the set of $k$ vector functions $f_i: \mathbb{R}^{m_i} \to \mathbb{R}^{n_i} \quad x_i \mapsto f_i(x_i) = y_i$ 
 * this forms the function 
 * $f: \mathbb{R}^{m} \to \mathbb{R}^{n} \quad x_i \mapsto f(x) = y$ where $n = \sum_{i=1}^k n_i$ and  
 * $m = \sum_{i=1}^k m_i$ and $x = (x_1,x_2,\dots,x_k)$ \& $y = (y_1,y_2,\dots,y_k)$
 **/
public class ConcatenatedVectorFunction extends VectorFunction {

  private final int[] _xPartition;
  private final int[] _yPartition;
  private final int _nPartitions;
  private final VectorFunction[] _functions;
  private final int _sizeDom;
  private final int _sizeRange;

  /**
   * Form the concatenated function 
   * @param functions The sub functions 
   */
  public ConcatenatedVectorFunction(final VectorFunction[] functions) {
    ArgumentChecker.noNulls(functions, "functions");
    _functions = functions;
    _nPartitions = functions.length;
    _xPartition = new int[_nPartitions];
    _yPartition = new int[_nPartitions];
    int m = 0;
    int n = 0;
    for (int i = 0; i < _nPartitions; i++) {
      _xPartition[i] = _functions[i].getSizeOfDomain();
      _yPartition[i] = _functions[i].getSizeOfRange();
      m += _xPartition[i];
      n += _yPartition[i];
    }
    _sizeDom = m;
    _sizeRange = n;
  }

  @Override
  public DoubleMatrix2D evaluateJacobian(final DoubleMatrix1D x) {
    final DoubleMatrix1D[] subX = partition(x);
    final DoubleMatrix2D jac = new DoubleMatrix2D(getSizeOfRange(), getSizeOfDomain());

    int pos1 = 0;
    int pos2 = 0;
    for (int i = 0; i < _nPartitions; i++) {
      final DoubleMatrix2D subJac = _functions[i].evaluateJacobian(subX[i]);
      final int nRows = _yPartition[i];
      final int nCols = _xPartition[i];
      if (nCols > 0) {
        for (int r = 0; r < nRows; r++) {
          System.arraycopy(subJac.getData()[r], 0, jac.getData()[pos1++], pos2, nCols);
        }
        pos2 += nCols;
      } else {
        pos1 += nRows;
      }
    }
    return jac;
  }

  @Override
  public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
    final DoubleMatrix1D[] subX = partition(x);
    final DoubleMatrix1D y = new DoubleMatrix1D(getSizeOfRange());
    int pos = 0;
    for (int i = 0; i < _nPartitions; i++) {
      final double[] subY = _functions[i].evaluate(subX[i]).getData();
      final int length = subY.length;
      System.arraycopy(subY, 0, y.getData(), pos, length);
      pos += length;
    }
    return y;
  }

  private DoubleMatrix1D[] partition(final DoubleMatrix1D x) {
    final DoubleMatrix1D[] res = new DoubleMatrix1D[_nPartitions];
    int pos = 0;
    for (int i = 0; i < _nPartitions; i++) {
      final int length = _xPartition[i];
      res[i] = new DoubleMatrix1D(length);
      System.arraycopy(x.getData(), pos, res[i].getData(), 0, length);
      pos += length;
    }
    return res;
  }

  @Override
  public int getSizeOfDomain() {
    return _sizeDom;
  }

  @Override
  public int getSizeOfRange() {
    return _sizeRange;
  }

}
