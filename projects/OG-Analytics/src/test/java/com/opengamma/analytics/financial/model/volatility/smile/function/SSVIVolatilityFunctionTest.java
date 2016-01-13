/**
 * Copyright (C) 2016 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.differentiation.FiniteDifferenceType;
import com.opengamma.analytics.math.differentiation.ScalarFieldFirstOrderDifferentiator;
import com.opengamma.analytics.math.differentiation.ValueDerivatives;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link SSVIVolatilityFunction}
 */
@Test(groups = TestGroup.UNIT)
public class SSVIVolatilityFunctionTest {
  
  private static final double VOL_ATM = 0.20;
  private static final double RHO = -0.25;
  private static final double ETA = 0.50;
  private static final double TIME_EXP = 2.5;
  private static final double FORWARD = 0.05;
  private static final int N = 10;
  private static final double[] STRIKES = new double[N];
  static {
    for (int i = 0; i < N; i++) {
      STRIKES[i] = FORWARD - 0.03 + (i * 0.05 / N);
    }
  }

  private static final double TOLERANCE_VOL = 1.0E-10;
  private static final double TOLERANCE_AD = 1.0E-7;

  @Test
  public void volatility() { // Function versus local implementation of formula
    double theta = VOL_ATM * VOL_ATM * TIME_EXP;
    double phi = ETA * Math.sqrt(theta);
    for (int i = 0; i < N; i++) {
      double k = Math.log(STRIKES[i] / FORWARD);
      double w = 0.5 * theta * (1.0d + RHO * phi * k + Math.sqrt(Math.pow(phi * k + RHO, 2) + (1.0d - RHO * RHO)));
      double sigmaExpected = Math.sqrt(w / TIME_EXP);
      double sigmaComputed = SSVIVolatilityFunction.volatility(FORWARD, STRIKES[i], TIME_EXP, VOL_ATM, RHO, ETA);
      assertEquals("Strike: " + STRIKES[i], sigmaExpected, sigmaComputed, TOLERANCE_VOL);
    }
  }

  @Test
  public void derivatives() { // AD v Finite Difference   
    ScalarFieldFirstOrderDifferentiator differentiator =
        new ScalarFieldFirstOrderDifferentiator(FiniteDifferenceType.CENTRAL, 1.0E-5);
    for (int i = 0; i < N; i++) {
      Function1D<DoubleMatrix1D, Double> function = new Function1D<DoubleMatrix1D, Double>() {
        private static final long serialVersionUID = 1L;
        @Override
        public Double evaluate(DoubleMatrix1D x) {
          return SSVIVolatilityFunction.volatility(x.getEntry(0), x.getEntry(1), x.getEntry(2),
              x.getEntry(3), x.getEntry(4), x.getEntry(5));
        }
      };
      Function1D<DoubleMatrix1D, DoubleMatrix1D> d = differentiator.differentiate(function);
      DoubleMatrix1D fd = d.evaluate(new DoubleMatrix1D(FORWARD, STRIKES[i], TIME_EXP, VOL_ATM, RHO, ETA));
      ValueDerivatives ad = 
          SSVIVolatilityFunction.volatilityAdjoint(FORWARD, STRIKES[i], TIME_EXP, VOL_ATM, RHO, ETA);
      for (int j = 0; j < 6; j++) {
        assertEquals("Strike: " + STRIKES[i], fd.getEntry(j), ad.getDerivatives()[j], TOLERANCE_AD);
      }
    }   
  }

}
