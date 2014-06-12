/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.BlackFunctionData;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.util.test.TestGroup;

/**
 * @deprecated This class tests deprecated functionality
 */
@Deprecated
@Test(groups = TestGroup.UNIT)
public class SABRSurfaceFittingTest {
  private static final SABRNonLinearLeastSquareFitter FITTER = new SABRNonLinearLeastSquareFitter(new SABRHaganVolatilityFunction());

  @Test
  public void doIt() {
    final double[] maturities = new double[] {5, 1, 10, 15, 1, 5 };
    final double[] tenors = new double[] {5, 5, 10, 15, 1, 10 };
    final double[] forwards = new double[] {0.0424, 0.025513, 0.046213, 0.04405, 0.010482, 0.04443 };
    final double[] atmVols = new double[] {0.23845, 0.36995, 0.18745, 0.162, 0.7332, 0.2177 };

    final int n = maturities.length;
    Validate.isTrue(n == tenors.length && n == forwards.length && n == atmVols.length);

    final double[] moneynessSigma = new double[] {-2, -1, -0.5, -0.25, 0, 0.25, 0.5, 1, 2 };
    final double[][] vols = new double[n][];
    vols[0] = new double[] {0, 0.27, 0.253, 0.247, 0.23845, 0.238, 0.236, 0.233, 0.226 };
    vols[1] = new double[] {0.653, 0.442, 0.396, 0.382, 0.36995, 0.367, 0.363, 0.363, 0.36 };
    vols[2] = new double[] {0.25, 0.214, 0.2, 0.194, 0.18745, 0.186, 0.183, 0.179, 0.171 };
    vols[3] = new double[] {0.224, 0.19, 0.175, 0.17, 0.162, 0.161, 0.158, 0.154, 0.15 };
    vols[4] = new double[] {0, 0, 0.847, 0.776, 0.7332, 0.718, 0.707, 0.702, 0.701 };
    vols[5] = new double[] {0.284, 0.247, 0.231, 0.225, 0.2177, 0.217, 0.213, 0.209, 0.207 };

    final double[] alpha = new double[n];
    final double[] beta = new double[n];
    final double[] nu = new double[n];
    final double[] rho = new double[n];

    final double[] start = new double[] {0.3, 0.9, 0.3, 0.0 };

    for (int i = 0; i < n; i++) {
      int m = 0;
      for (int j = 0; j < vols[i].length; j++) {
        if (vols[i][j] > 0.0) {
          m++;
        }
      }
      final EuropeanVanillaOption[] options = new EuropeanVanillaOption[m];
      final BlackFunctionData[] data = new BlackFunctionData[m];
      final double[] errors = new double[m];
      int p = 0;
      for (int j = 0; j < vols[i].length; j++) {
        if (vols[i][j] > 0.0) {
          options[p] = new EuropeanVanillaOption(forwards[i] * Math.exp(atmVols[i] * Math.sqrt(maturities[i]) * moneynessSigma[j]), maturities[i], true);
          data[p] = new BlackFunctionData(forwards[i], 1, vols[i][j]);
          errors[p] = 0.001;
          p++;
        }
      }
      final LeastSquareResultsWithTransform result = FITTER.getFitResult(options, data, errors, start, new BitSet(4));
      final DoubleMatrix1D params = result.getModelParameters();
      alpha[i] = params.getEntry(0);
      beta[i] = params.getEntry(1);
      nu[i] = params.getEntry(2);
      rho[i] = params.getEntry(3);

      // System.out.print(alpha[i] + "\t" + beta[i] + "\t" + nu[i] + "\t" + rho[i] + "\t");
      // for (int j = 0; j < m; j++) {
      // System.out.print("\t" + strikes[j]);
      // }
      // System.out.print("\n");
      // System.out.print("\t\t\t\t");
      // for (int j = 0; j < m; j++) {
      // double sabrVol = sabr.impliedVolatility(forwards[i], alpha[i], beta[i], nu[i], rho[i], strikes[j], maturities[i]);
      // System.out.print("\t" + sabrVol);
      // }
      // System.out.print("\n");
    }

  }
}
