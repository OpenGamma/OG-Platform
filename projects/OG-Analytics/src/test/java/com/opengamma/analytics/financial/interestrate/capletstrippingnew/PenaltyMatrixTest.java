/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.FunctionUtils;
import com.opengamma.analytics.math.interpolation.PenaltyMatrixGenerator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;

/**
 * 
 */
public class PenaltyMatrixTest {

  private static final MatrixAlgebra MA = new OGMatrixAlgebra();

  public DoubleMatrix2D getDiffMatrix(final double[] strikes, final double[] fixingTimes, final int index) {
    final int nStrikes = strikes.length;
    final int nTimes = fixingTimes.length;
    final int nVols = nStrikes * nTimes;
    final double[][] timeMatrix = new double[nVols][nVols];
    final double[][] strikeMatrix = new double[nVols][nVols];

    for (int i = 0; i < nStrikes; ++i) {
      for (int j = 0; j < nTimes; ++j) {
        if (j != 0 && j != nTimes - 1) {
          final double dtInv = 1.0 / (fixingTimes[j + 1] - fixingTimes[j]);
          final double dtmInv = 1.0 / (fixingTimes[j] - fixingTimes[j - 1]);
          timeMatrix[nTimes * i + j][nTimes * i + j + 1] = dtInv * dtmInv;
          timeMatrix[nTimes * i + j][nTimes * i + j] = -dtmInv * (dtInv + dtmInv);
          timeMatrix[nTimes * i + j][nTimes * i + j - 1] = dtmInv * dtmInv;
        }

        if (i != 0 && i != nStrikes - 1) {
          final double dkInv = 1.0 / (strikes[i + 1] - strikes[i]);
          final double dkmInv = 1.0 / (strikes[i] - strikes[i - 1]);
          strikeMatrix[nTimes * i + j][nTimes * (i + 1) + j] = dkInv * dkmInv;
          strikeMatrix[nTimes * i + j][nTimes * i + j] = -dkmInv * (dkInv + dkmInv);
          strikeMatrix[nTimes * i + j][nTimes * (i - 1) + j] = dkmInv * dkmInv;
        }
      }
    }

    final DoubleMatrix2D penaltyTime = new DoubleMatrix2D(timeMatrix);
    final DoubleMatrix2D penaltyStrike = new DoubleMatrix2D(strikeMatrix);
    if (index == 0) {
      return penaltyStrike;
    }
    return penaltyTime;

  }

  @Test
  public void test() {

    double[] x = new double[] {1, 2, 3.3, 4, 5 };
    final int n = x.length;
    final double[] y = new double[n];
    for (int i = 0; i < n; i++) {
      y[i] = 0.3 + 2 * x[i] + 4 * x[i] * x[i] + 1 / 6. * FunctionUtils.cube(x[i]);
    }
    final DoubleMatrix1D yV = new DoubleMatrix1D(y);

    DoubleMatrix2D d = getDiffMatrix(x, new double[] {10 }, 0);
    System.out.println(d);
    System.out.println(MA.multiply(d, yV));

    System.out.println();

    d = PenaltyMatrixGenerator.getDiffMatrix(x, 2);
    System.out.println(d);
    System.out.println(MA.multiply(d, yV));

    DoubleMatrix2D p = PenaltyMatrixGenerator.getPenaltyMatrix(x, 2);
    System.out.println(MA.getInnerProduct(yV, MA.multiply(p, yV)));

    x = new double[] {0.1, 0.2, 0.33, 0.4, 0.5 };
    p = PenaltyMatrixGenerator.getPenaltyMatrix(x, 2);
    System.out.println(MA.getInnerProduct(yV, MA.multiply(p, yV)));
  };

}
