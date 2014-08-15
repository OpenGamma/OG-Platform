/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.util.test.TestGroup;

/**
 * Test class for 
 */
@Test(groups = TestGroup.UNIT)
public class SmileInterpolatorSABRWithExtrapolationTest {

  @Test(enabled = false)
  public void test() {
    double expiry = 1.5;
    double forward = 0.011;
    int nStrikes = 10;
    double[] strikes = new double[nStrikes];
    double[] impliedVols = new double[] {1.02, 0.92, 0.802, 0.745, 0.781, 0.812, 0.8334, 0.878, 0.899, 0.9152 };
    for (int i = 0; i < nStrikes; ++i) {
      strikes[i] = forward * (0.85 + i * 0.05);
      System.out.println(strikes[i] + "\t" + impliedVols[i]);
    }

    double muLow = strikes[0] * BlackFormulaRepository.dualDelta(forward, strikes[0], expiry, impliedVols[0], false) / BlackFormulaRepository.price(forward, strikes[0], expiry, impliedVols[0], false);
    double muHigh = -strikes[nStrikes - 1] * BlackFormulaRepository.dualDelta(forward, strikes[nStrikes - 1], expiry, impliedVols[nStrikes - 1], true) /
        BlackFormulaRepository.price(forward, strikes[nStrikes - 1], expiry, impliedVols[nStrikes - 1], true);

    SmileExtrapolationFunctionSABRProvider extrapShift = new ShiftedLogNormalExtrapolationFunctionProvider("Exception");
    SmileInterpolatorSABRWithExtrapolation interpShift = new SmileInterpolatorSABRWithExtrapolation(extrapShift);
    InterpolatedSmileFunction funcShift = new InterpolatedSmileFunction(interpShift, forward, strikes, expiry, impliedVols);

    SmileExtrapolationFunctionSABRProvider extrapBDK = new BenaimDodgsonKainthExtrapolationFunctionProvider(muLow, muHigh);
    SmileInterpolatorSABRWithExtrapolation interpBDK = new SmileInterpolatorSABRWithExtrapolation(extrapBDK);
    InterpolatedSmileFunction funcBDK = new InterpolatedSmileFunction(interpBDK, forward, strikes, expiry, impliedVols);

    int nSamples = 200;
    for (int i = 0; i < nSamples; ++i) {
      double strike = strikes[0] * (0.9 + i * 0.001);
      System.out.println(strike + "\t" + funcShift.getVolatility(strike) + "\t" + funcBDK.getVolatility(strike));
    }

  }

  //  @Test
  //  public void testt() {
  //    double eps = 1.e-6;
  //    for (int i = 0; i < 100; ++i) {
  //      double strike = 190 + i;
  //      double vol = BlackFormulaRepository.impliedVolatility(10, 200, strike, 1.5, true);
  //      System.out.println(strike + "\t" + BlackFormulaRepository.dualDelta(200, strike, 1.5, vol, true) + "\t" +
  //          (BlackFormulaRepository.impliedVolatility(10, 200, strike + eps, 1.5, true) - BlackFormulaRepository.impliedVolatility(10, 200, strike, 1.5, true)) / eps);
  //    }
  //  }

}
