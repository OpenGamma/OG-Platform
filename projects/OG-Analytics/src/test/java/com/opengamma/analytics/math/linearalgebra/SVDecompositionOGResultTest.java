/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.maths.datacontainers.matrix.OGRealDenseMatrix;
import com.opengamma.maths.helpers.FuzzyEquals;
import com.opengamma.maths.materialisers.Materialisers;
import com.opengamma.maths.nodes.SVD;
import com.opengamma.util.test.TestGroup;

/**
 * Test of OG SVD impl.
 */
@Test(groups = TestGroup.UNIT)
public class SVDecompositionOGResultTest {
  static final double CONDITION = 7.4896869586610810;
  static final double NORM = 2.8608058531117035;
  static final int RANK = 5;

  // This is a Wilkinson matrix order 5.
  static final OGRealDenseMatrix M = new OGRealDenseMatrix(new double[][] { { 2.0000000000000000, 1.0000000000000000, 0.0000000000000000, 0.0000000000000000, 0.0000000000000000 },
    { 1.0000000000000000, 1.0000000000000000, 1.0000000000000000, 0.0000000000000000, 0.0000000000000000 },
    { 0.0000000000000000, 1.0000000000000000, 0.0000000000000000, 1.0000000000000000, 0.0000000000000000 },
    { 0.0000000000000000, 0.0000000000000000, 1.0000000000000000, 1.0000000000000000, 1.0000000000000000 },
    { 0.0000000000000000, 0.0000000000000000, 0.0000000000000000, 1.0000000000000000, 2.0000000000000000 } });

  static final OGRealDenseMatrix S = new OGRealDenseMatrix(new double[][] { { 2.8608058531117040, 0.0000000000000000, 0.0000000000000000, 0.0000000000000000, 0.0000000000000000 },
    { 0.0000000000000000, 2.6180339887498945, 0.0000000000000000, 0.0000000000000000, 0.0000000000000000 },
    { 0.0000000000000000, 0.0000000000000000, 1.2541016883650529, 0.0000000000000000, 0.0000000000000000 },
    { 0.0000000000000000, 0.0000000000000000, 0.0000000000000000, 1.1149075414767557, 0.0000000000000000 },
    { 0.0000000000000000, 0.0000000000000000, 0.0000000000000000, 0.0000000000000000, 0.3819660112501048 } });
  static final OGRealDenseMatrix U = new OGRealDenseMatrix(new double[][] { { -0.5100363100784391, 0.6015009550075459, -0.4699592805488332, -0.1378449746185653, 0.3717480344601844 },
    { -0.4390422410150158, 0.3717480344601849, 0.3505418338985494, 0.4293743509940411, -0.6015009550075456 },
    { -0.3069360617655821, 0.0000000000000001, 0.5590325523850365, -0.7702420784154201, 0.0000000000000002 },
    { -0.4390422410150167, -0.3717480344601846, 0.3505418338985498, 0.4293743509940411, 0.6015009550075456 },
    { -0.5100363100784399, -0.6015009550075459, -0.4699592805488334, -0.1378449746185655, -0.3717480344601846 } }
      );
  static final OGRealDenseMatrix UT = new OGRealDenseMatrix(new double[][] { { -0.5100363100784391, -0.4390422410150158, -0.3069360617655821, -0.4390422410150167, -0.5100363100784399 },
    { 0.6015009550075459, 0.3717480344601849, 0.0000000000000001, -0.3717480344601846, -0.6015009550075459 },
    { -0.4699592805488332, 0.3505418338985494, 0.5590325523850365, 0.3505418338985498, -0.4699592805488334 },
    { -0.1378449746185653, 0.4293743509940411, -0.7702420784154201, 0.4293743509940411, -0.1378449746185655 },
    { 0.3717480344601844, -0.6015009550075456, 0.0000000000000002, 0.6015009550075456, -0.3717480344601846 } });

  static final OGRealDenseMatrix V = new OGRealDenseMatrix(new double[][] { { -0.5100363100784391, 0.6015009550075459, -0.4699592805488336, 0.1378449746185654, 0.3717480344601845 },
    { -0.4390422410150159, 0.3717480344601848, 0.3505418338985494, -0.4293743509940412, -0.6015009550075452 },
    { -0.3069360617655819, 0.0000000000000002, 0.5590325523850366, 0.7702420784154198, -0.0000000000000001 },
    { -0.4390422410150164, -0.3717480344601841, 0.3505418338985500, -0.4293743509940410, 0.6015009550075456 },
    { -0.5100363100784396, -0.6015009550075456, -0.4699592805488336, 0.1378449746185653, -0.3717480344601843 } });

  static final OGRealDenseMatrix VT = new OGRealDenseMatrix(new double[][] { { -0.5100363100784391, -0.4390422410150159, -0.3069360617655819, -0.4390422410150164, -0.5100363100784396 },
    { 0.6015009550075459, 0.3717480344601848, 0.0000000000000002, -0.3717480344601841, -0.6015009550075456 },
    { -0.4699592805488336, 0.3505418338985494, 0.5590325523850366, 0.3505418338985500, -0.4699592805488336 },
    { 0.1378449746185654, -0.4293743509940412, 0.7702420784154198, -0.4293743509940410, 0.1378449746185653 },
    { 0.3717480344601845, -0.6015009550075452, -0.0000000000000001, 0.6015009550075456, -0.3717480344601843 } });

  static final double[] SINGULAR_VALUES = new double[] { 2.8608058531117040, 2.6180339887498945, 1.2541016883650529, 1.1149075414767557, 0.3819660112501048 };
  static final OGRealDenseMatrix RESULT_2D = new OGRealDenseMatrix(new double[][] { { -0.2500000000000000, -2.5000000000000000 }, { 1.5000000000000000, 15.0000000000000000 },
    { 0.7500000000000000, 7.5000000000000000 }, { 1.5000000000000000, 15.0000000000000000 }, { 1.7499999999999998, 17.5000000000000000 } });
  static final OGRealDenseMatrix RESULT_1D = new OGRealDenseMatrix(new double[][] { { -0.2500000000000000 }, { 1.5000000000000000 }, { 0.7500000000000000 }, { 1.5000000000000000 },
    { 1.7499999999999998 } });
  private static final SVDecompositionResult SVD = new SVDecompositionOGResult(new SVD(M));

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullSVD() {
    new SVDecompositionOGResult(null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullArray() {
    SVD.solve((double[]) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullVector() {
    SVD.solve((DoubleMatrix1D) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMatrix() {
    SVD.solve((DoubleMatrix2D) null);
  }

  @Test
  public void testGetters() {
    assertTrue(FuzzyEquals.SingleValueFuzzyEquals(CONDITION, SVD.getConditionNumber()));
    assertTrue(RANK == SVD.getRank());
    assertTrue(FuzzyEquals.SingleValueFuzzyEquals(NORM, SVD.getNorm()));
    assertTrue(FuzzyEquals.ArrayFuzzyEquals(Materialisers.toDoubleArrayOfArrays(S), SVD.getS().asDoubleAoA()));
    assertTrue(FuzzyEquals.ArrayFuzzyEquals(Materialisers.toDoubleArrayOfArrays(U), SVD.getU().asDoubleAoA()));
    assertTrue(FuzzyEquals.ArrayFuzzyEquals(Materialisers.toDoubleArrayOfArrays(UT), SVD.getUT().asDoubleAoA()));
    assertTrue(FuzzyEquals.ArrayFuzzyEquals(Materialisers.toDoubleArrayOfArrays(V), SVD.getV().asDoubleAoA()));
    assertTrue(FuzzyEquals.ArrayFuzzyEquals(Materialisers.toDoubleArrayOfArrays(VT), SVD.getVT().asDoubleAoA()));
    assertTrue(FuzzyEquals.ArrayFuzzyEquals(SINGULAR_VALUES, SVD.getSingularValues()));
  }

  @Test
  public void testSolvers() {
    assertTrue(FuzzyEquals.ArrayFuzzyEquals(RESULT_1D.getData(), SVD.solve(new DoubleMatrix1D(new double[] { 1, 2, 3, 4, 5 }).asDoubleArray())));
    assertTrue(FuzzyEquals.ArrayFuzzyEquals(RESULT_1D.getData(), SVD.solve(new DoubleMatrix1D(new double[] { 1, 2, 3, 4, 5 })).asDoubleArray()));
    assertTrue(FuzzyEquals.ArrayFuzzyEquals(RESULT_2D.getData(),
        SVD.solve(new DoubleMatrix2D(new double[][] { { 1.0000000000000000, 10.0000000000000000 }, { 2.0000000000000000, 20.0000000000000000 }, { 3.0000000000000000, 30.0000000000000000 },
          { 4.0000000000000000, 40.0000000000000000 }, { 5.0000000000000000, 50.0000000000000000 } })).asDoubleArray(), 1e-14, 1e-14));
  }

  @Test
  public void testRank()
  {
    OGRealDenseMatrix M = new OGRealDenseMatrix(new double[][] { { 1.0000000000000000, 2.0000000000000000, 3.0000000000000000 }, { 4.0000000000000000, 5.0000000000000000, 6.0000000000000000 },
      { 7.0000000000000000, 8.0000000000000000, 9.0000000000000000 }, { 10.0000000000000000, 11.0000000000000000, 12.0000000000000000 } });
    SVDecompositionResult SVD = new SVDecompositionOGResult(new SVD(M));
    assertTrue(SVD.getRank() == 2);
  }
}
