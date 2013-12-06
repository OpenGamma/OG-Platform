/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.ColtMatrixAlgebra;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class SVDecompositionColtTest extends SVDecompositionCalculationTestCase {
  private static final MatrixAlgebra ALGEBRA = new ColtMatrixAlgebra();
  private static final Decomposition<SVDecompositionResult> SVD = new SVDecompositionColt();

  @Override
  protected MatrixAlgebra getAlgebra() {
    return ALGEBRA;
  }

  @Override
  protected Decomposition<SVDecompositionResult> getSVD() {
    return SVD;
  }

}
