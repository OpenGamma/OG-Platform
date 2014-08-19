/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.SABRExtrapolationRightFunction;
import com.opengamma.analytics.financial.model.volatility.BlackFormulaRepository;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test class for {@link BenaimDodgsonKainthExtrapolationFunctionProvider} and {@link ShiftedLogNormalExtrapolationFunctionProvider}
 */
@Test(groups = TestGroup.UNIT)
public class SmileInterpolatorSABRWithExtrapolationTest {

  /**
   * Confirm C2 smoothness
   */
  @Test
  public void BDKSmoothnessGeneralTest() {
    double eps = 1.0e-5;

    double expiry = 1.5;
    double forward = 1.1;
    int nStrikes = 10;
    double[] strikes = new double[nStrikes];
    double[] impliedVols = new double[] {0.97, 0.92, 0.802, 0.745, 0.781, 0.812, 0.8334, 0.878, 0.899, 0.9252 };
    for (int i = 0; i < nStrikes; ++i) {
      strikes[i] = forward * (0.85 + i * 0.05);
    }

    double muLow = strikes[0] * BlackFormulaRepository.dualDelta(forward, strikes[0], expiry, impliedVols[0], false) / BlackFormulaRepository.price(forward, strikes[0], expiry, impliedVols[0], false);
    double muHigh = -strikes[nStrikes - 1] * BlackFormulaRepository.dualDelta(forward, strikes[nStrikes - 1], expiry, impliedVols[nStrikes - 1], true) /
        BlackFormulaRepository.price(forward, strikes[nStrikes - 1], expiry, impliedVols[nStrikes - 1], true);

    SmileExtrapolationFunctionSABRProvider extrapBDK = new BenaimDodgsonKainthExtrapolationFunctionProvider(muLow, muHigh);
    SmileInterpolatorSABRWithExtrapolation interpBDK = new SmileInterpolatorSABRWithExtrapolation(extrapBDK);
    InterpolatedSmileFunction funcBDK = new InterpolatedSmileFunction(interpBDK, forward, strikes, expiry, impliedVols);

    List<SABRFormulaData> modelParams = (new SmileInterpolatorSABR()).getFittedModelParameters(forward, strikes, expiry, impliedVols);
    SABRExtrapolationLeftFunction sabrLeftExtrapolation = new SABRExtrapolationLeftFunction(forward, modelParams.get(0), strikes[0], expiry, muLow, new SABRHaganVolatilityFunction());
    SABRExtrapolationRightFunction sabrRightExtrapolation = new SABRExtrapolationRightFunction(forward, modelParams.get(nStrikes - 3), strikes[nStrikes - 1], expiry, muHigh,
        new SABRHaganVolatilityFunction());

    /*
     * left interpolation
     */
    {
      // Checking underlying extrapolation
      double boundaryValue = sabrLeftExtrapolation.price(new EuropeanVanillaOption(strikes[0], expiry, false));
      double CutoffUp = strikes[0] + eps;
      double CutoffDw = strikes[0] - eps;
      double optionPriceExt = sabrLeftExtrapolation.price(new EuropeanVanillaOption(CutoffDw, expiry, false));
      double optionPriceInt = sabrLeftExtrapolation.price(new EuropeanVanillaOption(CutoffUp, expiry, false));
      assertEquals(boundaryValue, optionPriceExt, eps);
      assertEquals(boundaryValue, optionPriceInt, eps);
      double optionPriceExtDw = sabrLeftExtrapolation.price(new EuropeanVanillaOption(CutoffDw - eps, expiry, false));
      double firstExt = (1.5 * boundaryValue + 0.5 * optionPriceExtDw - 2.0 * optionPriceExt) / eps;
      double optionPriceIntUp = sabrLeftExtrapolation.price(new EuropeanVanillaOption(CutoffUp + eps, expiry, false));
      double firstInt = (2.0 * optionPriceInt - 0.5 * optionPriceIntUp - 1.5 * boundaryValue) / eps;
      assertEquals(firstInt, firstExt, eps);
      double secondExt = (boundaryValue + optionPriceExtDw - 2.0 * optionPriceExt) / eps / eps;
      double secondInt = (optionPriceIntUp + boundaryValue - 2.0 * optionPriceInt) / eps / eps;
      assertEquals(secondInt, secondExt, Math.abs(secondInt) * 1.0e-3);

      // Checking volatility function
      double volInt = funcBDK.getVolatility(CutoffUp);
      double volExt = funcBDK.getVolatility(CutoffDw);
      double volBoundary = funcBDK.getVolatility(strikes[0]);
      assertEquals(volBoundary, volInt, eps);
      assertEquals(volBoundary, volExt, eps);
      double volExtDw = funcBDK.getVolatility(CutoffDw - eps);
      double volFirstExt = (1.5 * volBoundary + 0.5 * volExtDw - 2.0 * volExt) / eps;
      double volIntUp = funcBDK.getVolatility(CutoffUp + eps);
      double volFirstInt = (2.0 * volInt - 0.5 * volIntUp - 1.5 * volBoundary) / eps;
      assertEquals(volFirstInt, volFirstExt, eps);
      double volSecondExt = (volBoundary + volExtDw - 2.0 * volExt) / eps / eps;
      double volSecondInt = (volIntUp + volBoundary - 2.0 * volInt) / eps / eps;
      assertEquals(volSecondInt, volSecondExt, Math.abs(volSecondInt) * 1.0e-3);
    }

    /*
     * right interpolation
     */
    {
      // Checking underlying extrapolation
      double boundaryValue = sabrRightExtrapolation.price(new EuropeanVanillaOption(strikes[nStrikes - 1], expiry, true));
      double CutoffUp = strikes[nStrikes - 1] + eps;
      double CutoffDw = strikes[nStrikes - 1] - eps;
      double optionPriceExt = sabrRightExtrapolation.price(new EuropeanVanillaOption(CutoffUp, expiry, true));
      double optionPriceInt = sabrRightExtrapolation.price(new EuropeanVanillaOption(CutoffDw, expiry, true));
      assertEquals(boundaryValue, optionPriceExt, eps);
      assertEquals(boundaryValue, optionPriceInt, eps);
      double optionPriceExtUp = sabrRightExtrapolation.price(new EuropeanVanillaOption(CutoffUp + eps, expiry, true));
      double firstExt = (2.0 * optionPriceExt - 0.5 * optionPriceExtUp - 1.5 * boundaryValue) / eps;
      double optionPriceIntDw = sabrRightExtrapolation.price(new EuropeanVanillaOption(CutoffDw - eps, expiry, true));
      double firstInt = (-2.0 * optionPriceInt + 1.5 * boundaryValue + 0.5 * optionPriceIntDw) / eps;
      assertEquals(firstInt, firstExt, eps);
      double secondExt = (optionPriceExtUp + boundaryValue - 2.0 * optionPriceExt) / eps / eps;
      double secondInt = (boundaryValue + optionPriceIntDw - 2.0 * optionPriceInt) / eps / eps;
      assertEquals(secondInt, secondExt, Math.abs(secondInt) * 1.0e-3);

      // Checking volatility function
      double volInt = funcBDK.getVolatility(CutoffDw);
      double volExt = funcBDK.getVolatility(CutoffUp);
      double volBoundary = funcBDK.getVolatility(strikes[nStrikes - 1]);
      assertEquals(volBoundary, volInt, eps);
      assertEquals(volBoundary, volExt, eps);
      double volExtUp = funcBDK.getVolatility(CutoffUp + eps);
      double volFirstExt = (2.0 * volExt - 0.5 * volExtUp - 1.5 * volBoundary) / eps;
      double volIntDw = funcBDK.getVolatility(CutoffDw - eps);
      double volFirstInt = (-2.0 * volInt + 1.5 * volBoundary + 0.5 * volIntDw) / eps;
      assertEquals(volFirstInt, volFirstExt, eps);
      double volSecondExt = (volBoundary + volExtUp - 2.0 * volExt) / eps / eps;
      double volSecondInt = (volIntDw + volBoundary - 2.0 * volInt) / eps / eps;
      assertEquals(volSecondInt, volSecondExt, Math.abs(volSecondInt) * 1.0e-3);
    }
  }

  /**
   * Check trivial extrapolation is recovered
   */
  @Test
  public void functionRecoveryBDKExtrapolationTest() {
    double forward = 1.0;
    double expiry = 3.0;
    int nSamples = 4;
    double[] strikes = new double[nSamples];
    double[] vols = new double[nSamples];

    final double mu = 1.0;
    final double a = -1.0;
    final double b = 0.0;
    final double c = 0.0;

    // Expected left extrapolation
    Function1D<Double, Double> left = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double strike) {
        return Math.pow(strike, mu) * Math.exp(a + b * strike + c * strike * strike);
      }
    };
    // Expected right extrapolation
    Function1D<Double, Double> right = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double strike) {
        return Math.pow(strike, -mu) * Math.exp(a + b / strike + c / strike / strike);
      }
    };

    for (int i = 0; i < nSamples; ++i) {
      double strike = forward * (0.75 + 0.05 * i);
      double price = left.evaluate(strike);
      double vol = BlackFormulaRepository.impliedVolatility(price, forward, strike, expiry, false);
      strikes[i] = strike;
      vols[i] = vol;
    }
    SmileExtrapolationFunctionSABRProvider extrapBDK = new BenaimDodgsonKainthExtrapolationFunctionProvider(mu, mu);
    SmileInterpolatorSABRWithExtrapolation interpBDK = new SmileInterpolatorSABRWithExtrapolation(extrapBDK);
    InterpolatedSmileFunction funcBDK = new InterpolatedSmileFunction(interpBDK, forward, strikes, expiry, vols);
    double[] keys = new double[] {forward * 0.1, forward * 0.5, forward * 0.66 };
    for (int i = 0; i < keys.length; ++i) {
      double vol = funcBDK.getVolatility(keys[i]);
      double price = BlackFormulaRepository.price(forward, keys[i], expiry, vol, false);
      assertEquals(left.evaluate(keys[i]), price, 1.e-2);
    }

    for (int i = 0; i < nSamples; ++i) {
      double strike = forward * (1.1 + 0.05 * i);
      double price = right.evaluate(strike);
      double vol = BlackFormulaRepository.impliedVolatility(price, forward, strike, expiry, true);
      strikes[i] = strike;
      vols[i] = vol;
    }
    extrapBDK = new BenaimDodgsonKainthExtrapolationFunctionProvider(mu, mu);
    interpBDK = new SmileInterpolatorSABRWithExtrapolation(extrapBDK);
    funcBDK = new InterpolatedSmileFunction(interpBDK, forward, strikes, expiry, vols);
    keys = new double[] {forward * 1.31, forward * 1.5, forward * 2.61, forward * 15.0 };
    for (int i = 0; i < keys.length; ++i) {
      double vol = funcBDK.getVolatility(keys[i]);
      double price = BlackFormulaRepository.price(forward, keys[i], expiry, vol, true);
      assertEquals(right.evaluate(keys[i]), price, 1.e-2);
    }
  }

  /**
   * 
   */
  @Test
  public void hashCodeAndEqualsErrorBDKTest() {

    BenaimDodgsonKainthExtrapolationFunctionProvider provider1 = new BenaimDodgsonKainthExtrapolationFunctionProvider(2.0, 3.5);
    BenaimDodgsonKainthExtrapolationFunctionProvider provider2 = new BenaimDodgsonKainthExtrapolationFunctionProvider(2.0, 1.5);
    BenaimDodgsonKainthExtrapolationFunctionProvider provider3 = new BenaimDodgsonKainthExtrapolationFunctionProvider(1.0, 3.5);
    ShiftedLogNormalExtrapolationFunctionProvider provider4 = new ShiftedLogNormalExtrapolationFunctionProvider();
    BenaimDodgsonKainthExtrapolationFunctionProvider provider5 = provider1;
    BenaimDodgsonKainthExtrapolationFunctionProvider provider6 = new BenaimDodgsonKainthExtrapolationFunctionProvider(2.0, 3.5);

    assertTrue(provider1.equals(provider1));

    assertTrue(provider1.hashCode() == provider5.hashCode());
    assertTrue(provider1.equals(provider5));
    assertTrue(provider5.equals(provider1));

    assertTrue(provider1.hashCode() == provider6.hashCode());
    assertTrue(provider1.equals(provider6));
    assertTrue(provider6.equals(provider1));

    assertFalse(provider1.equals(provider2));
    assertFalse(provider1.hashCode() == provider2.hashCode());

    assertFalse(provider1.equals(provider3));
    assertFalse(provider1.hashCode() == provider3.hashCode());

    assertFalse(provider1.equals(provider4));
    assertFalse(provider1.hashCode() == provider4.hashCode());

    assertFalse(provider1.equals(null));

    /**
     * Exception expected
     */
    try {
      new BenaimDodgsonKainthExtrapolationFunctionProvider(-2.0, 3.5);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("muLow should be positive", e.getMessage());
    }
    try {
      new BenaimDodgsonKainthExtrapolationFunctionProvider(2.5, -10.0);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("muHigh should be positive", e.getMessage());
    }

    double expiry = 1.5;
    double forward = 1.1;
    int nStrikes = 10;
    double[] strikes = new double[nStrikes];
    double[] impliedVols = new double[] {0.97, 0.92, 0.802, 0.745, 0.781, 0.812, 0.8334, 0.878, 0.899, 0.9252 };
    for (int i = 0; i < nStrikes; ++i) {
      strikes[i] = forward * (0.85 + i * 0.05);
    }
    double muLow = strikes[0] * BlackFormulaRepository.dualDelta(forward, strikes[0], expiry, impliedVols[0], false) / BlackFormulaRepository.price(forward, strikes[0], expiry, impliedVols[0], false);
    double muHigh = -strikes[nStrikes - 1] * BlackFormulaRepository.dualDelta(forward, strikes[nStrikes - 1], expiry, impliedVols[nStrikes - 1], true) /
        BlackFormulaRepository.price(forward, strikes[nStrikes - 1], expiry, impliedVols[nStrikes - 1], true);
    List<SABRFormulaData> modelParams = (new SmileInterpolatorSABR()).getFittedModelParameters(forward, strikes, expiry, impliedVols);
    BenaimDodgsonKainthExtrapolationFunctionProvider provider = new BenaimDodgsonKainthExtrapolationFunctionProvider(muLow, muHigh);

    try {
      provider.getExtrapolationFunction(modelParams.get(0), modelParams.get(nStrikes - 3), new SABRHaganVolatilityFunction(), forward, expiry, -strikes[0], strikes[nStrikes - 1]);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("cutOffStrikeLow should be positive", e.getMessage());
    }
    try {
      provider.getExtrapolationFunction(modelParams.get(0), modelParams.get(nStrikes - 3), new SABRHaganVolatilityFunction(), forward, expiry, strikes[0], -strikes[nStrikes - 1]);
      throw new RuntimeException();
    } catch (final Exception e) {
      assertEquals("cutOffStrikeLow < cutOffStrikeHigh should be satisfied", e.getMessage());
    }
  }

  @Test(enabled = false)
  public void test() {

    double expiry = 1.5;
    double forward = 0.011;
    int nStrikes = 10;
    double[] strikes = new double[nStrikes];
    double[] impliedVols = new double[] {0.97, 0.92, 0.802, 0.745, 0.781, 0.812, 0.8334, 0.878, 0.899, 0.9152 };
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
      double strike = strikes[0] * (0.01 + i * 0.01);
      System.out.println(strike + "\t" + funcShift.getVolatility(strike)
          + "\t" + funcBDK.getVolatility(strike)
          );
    }

    System.out.println();
    System.out.println();
    System.out.println();

    for (int i = 0; i < nSamples; ++i) {
      double strike = strikes[0] * (0.01 + i * 0.01);
      double vol = funcBDK.getVolatility(strike);
      boolean isCall = strike < forward ? false : true;
      System.out.println(strike + "\t" + BlackFormulaRepository.price(forward, strike, expiry, vol, isCall)
          );
    }

    //    List<SABRFormulaData> modelParams = (new SmileInterpolatorSABR()).getFittedModelParameters(forward, strikes, expiry, impliedVols);
    //    SABRExtrapolationRightFunction sabrLeftExtrapolation = new SABRExtrapolationRightFunction(forward, modelParams.get(0), strikes[0], expiry, -muLow, new SABRHaganVolatilityFunction());
    //    for (int i = 0; i < nSamples; ++i) {
    //      double strike = strikes[0] * (0.01 + i * 0.01);
    //      EuropeanVanillaOption option = new EuropeanVanillaOption(strike, expiry, false);
    //      System.out.println(strike + "\t" + sabrLeftExtrapolation.price(option));
    //    }
  }

  @Test
      (enabled = false)
      public void failTest() {
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
