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
 * 
 */
public class LUDecompositionResultCommons implements LUDecompositionResult {

  private final org.apache.commons.math.linear.LUDecomposition _lu;

  public LUDecompositionResultCommons(final org.apache.commons.math.linear.LUDecomposition lu) {
    _lu = lu;
  }
  
  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.LowerUpperDecompositionResult#getDeterminant()
   */
  @Override
  public double getDeterminant() {
   return _lu.getDeterminant();
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.LowerUpperDecompositionResult#getL()
   */
  @Override
  public DoubleMatrix2D getL() {
    return CommonsMathWrapper.wrap(_lu.getL());
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.LowerUpperDecompositionResult#getP()
   */
  @Override
  public DoubleMatrix2D getP() {
    return CommonsMathWrapper.wrap(_lu.getP());
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.LowerUpperDecompositionResult#getPivot()
   */
  @Override
  public int[] getPivot() {
  
    return _lu.getPivot();
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.LowerUpperDecompositionResult#getU()
   */
  @Override
  public DoubleMatrix2D getU() {
    return CommonsMathWrapper.wrap(_lu.getU());
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.DecompositionResult#Solve(com.opengamma.math.matrix.DoubleMatrix1D)
   */
  @Override
  public DoubleMatrix1D solve(DoubleMatrix1D b) {
    return CommonsMathWrapper.wrap(_lu.getSolver().solve(CommonsMathWrapper.wrap(b)));
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.DecompositionResult#solve(double[])
   */
  @Override
  public double[] solve(double[] b) {
    return _lu.getSolver().solve(b);
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.linearalgebra.DecompositionResult#solve(com.opengamma.math.matrix.DoubleMatrix2D)
   */
  @Override
  public DoubleMatrix2D solve(DoubleMatrix2D b) {
    return CommonsMathWrapper.wrap(_lu.getSolver().solve(CommonsMathWrapper.wrap(b)));
  }

}
