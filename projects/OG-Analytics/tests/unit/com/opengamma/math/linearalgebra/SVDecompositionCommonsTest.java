/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.linearalgebra;

import com.opengamma.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.math.matrix.MatrixAlgebra;

/**
 * 
 */
public class SVDecompositionCommonsTest extends SVDecompositionCalculationTestCase {
  private static final MatrixAlgebra ALGEBRA = new CommonsMatrixAlgebra();
  private static final Decomposition<SVDecompositionResult> SVD = new SVDecompositionCommons();

  @Override
  protected MatrixAlgebra getAlgebra() {
    return ALGEBRA;
  }

  @Override
  protected Decomposition<SVDecompositionResult> getSVD() {
    return SVD;
  }
}
