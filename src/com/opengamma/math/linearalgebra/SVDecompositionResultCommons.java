/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.util.wrapper.CommonsMathWrapper;

/**
 * Wrapper for results of Commons implementation of SVD
 */
public class SVDecompositionResultCommons implements SVDecompositionResult {

  private final org.apache.commons.math.linear.SingularValueDecomposition _svd;

  public SVDecompositionResultCommons(final org.apache.commons.math.linear.SingularValueDecomposition svd) {
    _svd = svd;
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.opengamma.math.linearalgebra.SingularValueDecompositionResult#
   * getConditionNumber()
   */
  @Override
  public double getConditionNumber() {
    return _svd.getConditionNumber();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getNorm()
   */
  @Override
  public double getNorm() {
    return _svd.getNorm();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getRank()
   */
  @Override
  public int getRank() {
    return _svd.getRank();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getS()
   */
  @Override
  public DoubleMatrix2D getS() {

    return CommonsMathWrapper.wrap(_svd.getS());
  }

  /*
   * (non-Javadoc)
   * 
   * @seecom.opengamma.math.linearalgebra.SingularValueDecompositionResult#
   * getSingularValues()
   */
  @Override
  public double[] getSingularValues() {
    return _svd.getSingularValues();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getU()
   */
  @Override
  public DoubleMatrix2D getU() {

    return CommonsMathWrapper.wrap(_svd.getU());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getUT()
   */
  @Override
  public DoubleMatrix2D getUT() {

    return CommonsMathWrapper.wrap(_svd.getUT());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getV()
   */
  @Override
  public DoubleMatrix2D getV() {

    return CommonsMathWrapper.wrap(_svd.getV());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getVT()
   */
  @Override
  public DoubleMatrix2D getVT() {

    return CommonsMathWrapper.wrap(_svd.getVT());
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.SingularValueDecompositionResult#Solve(com.opengamma.math.matrix.DoubleMatrix1D)
   */
  @Override
  public DoubleMatrix1D solve(DoubleMatrix1D b) {
    return CommonsMathWrapper.wrap(_svd.getSolver().solve(CommonsMathWrapper.wrap(b)));
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.DecompositionResult#solve(double[])
   */
  @Override
  public double[] solve(double[] b) {
    return _svd.getSolver().solve(b);
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.DecompositionResult#solve(com.opengamma.math.matrix.DoubleMatrix2D)
   */
  @Override
  public DoubleMatrix2D solve(DoubleMatrix2D b) {
    return CommonsMathWrapper.wrap(_svd.getSolver().solve(CommonsMathWrapper.wrap(b)));
  }

}
