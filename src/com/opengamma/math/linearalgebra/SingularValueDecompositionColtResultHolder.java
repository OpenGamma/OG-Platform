/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.util.wrapper.ColtWrapper;

/**
 * 
 */
public class SingularValueDecompositionColtResultHolder implements SingularValueDecompositionResult {

  private final cern.colt.matrix.linalg.SingularValueDecomposition _svd;

  public SingularValueDecompositionColtResultHolder(final cern.colt.matrix.linalg.SingularValueDecomposition svd) {
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
    return _svd.cond();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getNorm()
   */
  @Override
  public double getNorm() {
    return _svd.norm2();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getRank()
   */
  @Override
  public int getRank() {
    return _svd.rank();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getS()
   */
  @Override
  public DoubleMatrix2D getS() {

    return ColtWrapper.wrap(_svd.getS());
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

    return ColtWrapper.wrap(_svd.getU());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getUT()
   */
  @Override
  public DoubleMatrix2D getUT() {

    return ColtWrapper.wrap(_svd.getU()).getTranspose();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getV()
   */
  @Override
  public DoubleMatrix2D getV() {

    return ColtWrapper.wrap(_svd.getV());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.opengamma.math.linearalgebra.SingularValueDecompositionResult#getVT()
   */
  @Override
  public DoubleMatrix2D getVT() {

    return ColtWrapper.wrap(_svd.getV()).getTranspose();
  }

}
