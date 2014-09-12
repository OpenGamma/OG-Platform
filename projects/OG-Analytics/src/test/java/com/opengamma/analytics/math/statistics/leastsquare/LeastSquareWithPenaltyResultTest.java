/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.statistics.leastsquare;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class LeastSquareWithPenaltyResultTest {

  @Test
  public void test() {

    double chi2 = 13.234324;
    double pen = 2.3445;
    int nParms = 12;
    DoubleMatrix1D parms = new DoubleMatrix1D(nParms, 0.5);
    DoubleMatrix2D cov = new DoubleMatrix2D(nParms, nParms);


    LeastSquareWithPenaltyResults res = new LeastSquareWithPenaltyResults(chi2, pen, parms, cov);
    assertEquals(chi2, res.getChiSq());
    assertEquals(pen, res.getPenalty());

    DoubleMatrix2D invJac = new DoubleMatrix2D(nParms, 5);
    res = new LeastSquareWithPenaltyResults(chi2, pen, parms, cov, invJac);
    assertEquals(chi2, res.getChiSq());
    assertEquals(pen, res.getPenalty());
  }


}
