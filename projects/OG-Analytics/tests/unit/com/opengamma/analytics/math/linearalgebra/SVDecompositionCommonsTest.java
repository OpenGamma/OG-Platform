/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import com.opengamma.analytics.math.linearalgebra.Decomposition;
import com.opengamma.analytics.math.linearalgebra.SVDecompositionCommons;
import com.opengamma.analytics.math.linearalgebra.SVDecompositionResult;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;

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
