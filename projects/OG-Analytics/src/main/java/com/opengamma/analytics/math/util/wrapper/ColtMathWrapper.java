/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.util.wrapper;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Utility class for converting OpenGamma mathematical objects into <a href="http://acs.lbl.gov/software/colt/api/index.html">Colt</a> objects and vice versa.
 */
public final class ColtMathWrapper {

  private ColtMathWrapper() {
  }

  /**
   * @param x A Colt 2D matrix of doubles, not null
   * @return An OG 2D matrix
   */
  public static DoubleMatrix2D wrap(final cern.colt.matrix.DoubleMatrix2D x) {
    Validate.notNull(x, "x");
    return new DoubleMatrix2D(x.toArray());
  }

  /**
   * @param x An OG 2D matrix of doubles, not null
   * @return A Colt 2D matrix
   */
  public static cern.colt.matrix.DoubleMatrix2D wrap(final DoubleMatrix2D x) {
    Validate.notNull(x, "x");
    return cern.colt.matrix.DoubleFactory2D.dense.make(x.getData());

  }
}
