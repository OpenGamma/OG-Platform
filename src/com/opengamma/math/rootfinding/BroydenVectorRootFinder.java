/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.rootfinding;

import com.opengamma.math.function.Function1D;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * 
 */
public class BroydenVectorRootFinder extends VectorRootFinder {

  /* (non-Javadoc)
   * @see com.opengamma.math.rootfinding.MultiDRootFinder#getRoot(com.opengamma.math.function.Function1D, java.lang.Object)
   */
  @Override
  public DoubleMatrix1D getRoot(Function1D<DoubleMatrix1D, DoubleMatrix1D> function, DoubleMatrix1D x) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.opengamma.math.rootfinding.MultiDRootFinder#getRoot(com.opengamma.math.function.Function1D, com.opengamma.math.function.Function1D, java.lang.Object)
   */
  @Override
  public DoubleMatrix1D getRoot(Function1D<DoubleMatrix1D, DoubleMatrix1D> function,
      Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobian, DoubleMatrix1D x) {
    // TODO Auto-generated method stub
    return null;
  }

}
