/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.matrix.ColtMatrixAlgebra;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class SVDecompositionCommonsTest extends SVDecompositionCalculationTestCase {
  private static final MatrixAlgebra ALGEBRA = new ColtMatrixAlgebra();
  private static final SVDecomposition SVD = new SVDecompositionColt();

  @Override
  protected MatrixAlgebra getAlgebra() {
    return ALGEBRA;
  }

  @Override
  protected SVDecomposition getSVD() {
    return SVD;
  }
}
