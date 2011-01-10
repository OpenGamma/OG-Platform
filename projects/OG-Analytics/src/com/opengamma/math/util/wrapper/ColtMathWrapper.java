/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.util.wrapper;

import com.opengamma.math.matrix.DoubleMatrix2D;

/**
 * Wrapper for colt classes
 */
public final class ColtMathWrapper {

  private ColtMathWrapper() {
    //Cannot instantiate
  }

  public static DoubleMatrix2D wrap(final cern.colt.matrix.DoubleMatrix2D x) {
    return new DoubleMatrix2D(x.toArray());
  }

  public static cern.colt.matrix.DoubleMatrix2D wrap(final DoubleMatrix2D x) {
    return cern.colt.matrix.DoubleFactory2D.dense.make(x.getData());

  }
}
