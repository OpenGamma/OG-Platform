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
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRBerestyckiVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganAlternativeVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRPaulotVolatilityFunction;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test class for {@link SmileInterpolatorSABRWithExtrapolation} and its underlying class, 
 * {@link SmileExtrapolationFunctionSABRProvider}
 */
@Test(groups = TestGroup.UNIT)
public class SmileInterpolatorSABRWithExtrapolationTest {

  /**
   * Test interpolation part, essentially consistent with the super class (where extrapolation is absent) 
   * iff local fitting is applied. 
   * If global fit is used, resulting interpolation is tested with larger tolerance
   */
  @Test
  public void interpolationTest() {
    double eps = 1.0e-14;

    double expiry = 1.2;
    double forward = 1.7;
    int nStrikes = 11;
    double[] strikes = new double[nStrikes];
    double[] impliedVols = new double[] {2.17, 1.92, 1.702, 1.545, 1.281, 0.912, 0.9934, 1.0878, 1.1499, 1.2032, 1.242 };
    for (int i = 0; i < nStrikes; ++i) {
      strikes[i] = forward * (0.85 + i * 0.05);
    }

    WeightingFunction weight = LinearWeightingFunction.getInstance();
    int seed = 4729;
    double beta = 0.95;

    ShiftedLogNormalExtrapolationFunctionProvider extapQuiet = new ShiftedLogNormalExtrapolationFunctionProvider(
        "Quiet");
    SmileInterpolatorSABRWithExtrapolation interpQuiet = new SmileInterpolatorSABRWithExtrapolation(seed,
        new SABRHaganVolatilityFunction(), beta, weight, extapQuiet);
    InterpolatedSmileFunction funcQuiet = new InterpolatedSmileFunction(interpQuiet, forward, strikes, expiry,
        impliedVols);
    SmileInterpolatorSABR sabr = new SmileInterpolatorSABR(seed, new SABRHaganVolatilityFunction(), beta, weight);
    Function1D<Double, Double> volFunc = sabr.getVolatilityFunction(forward, strikes, expiry, impliedVols);

    int nKeys = 20;
    for (int i = 0; i < nKeys + 1; ++i) {
      Double key = strikes[0] + (strikes[nStrikes - 1] - strikes[0]) * i / nKeys;
      assertEquals(volFunc.evaluate(key), funcQuiet.getVolatility(key), eps);
    }

    SmileInterpolatorSABRWithExtrapolation interpGlobal1 = new SmileInterpolatorSABRWithExtrapolation(
        new SABRPaulotVolatilityFunction(), extapQuiet);
    SmileInterpolatorSABRWithExtrapolation interpGlobal2 = new SmileInterpolatorSABRWithExtrapolation(
        new SABRBerestyckiVolatilityFunction(), extapQuiet);
    SmileInterpolatorSABRWithExtrapolation interpGlobal3 = new SmileInterpolatorSABRWithExtrapolation(
        new SABRHaganAlternativeVolatilityFunction(), extapQuiet);
    InterpolatedSmileFunction funcGlobal1 = new InterpolatedSmileFunction(interpGlobal1, forward, strikes, expiry,
        impliedVols);
    InterpolatedSmileFunction funcGlobal2 = new InterpolatedSmileFunction(interpGlobal2, forward, strikes, expiry,
        impliedVols);
    InterpolatedSmileFunction funcGlobal3 = new InterpolatedSmileFunction(interpGlobal3, forward, strikes, expiry,
        impliedVols);
    for (int i = 0; i < nKeys + 1; ++i) {
      Double key = strikes[0] + (strikes[nStrikes - 1] - strikes[0]) * i / nKeys;
      double ref = funcQuiet.getVolatility(key);
      assertEquals(ref, funcGlobal1.getVolatility(key), 1.5 * ref * 1.0e-1);
      assertEquals(ref, funcGlobal2.getVolatility(key), ref * 1.0e-1);
      assertEquals(ref, funcGlobal3.getVolatility(key), ref * 1.0e-1);
    }

  }

  /**
   * 
   */
  @Test
  public void hashCodeAndEqualsTest() {
    WeightingFunction weight = LinearWeightingFunction.getInstance();
    int seed = 4729;
    double beta = 0.95;
    ShiftedLogNormalExtrapolationFunctionProvider extapQuiet = new ShiftedLogNormalExtrapolationFunctionProvider(
        "Quiet");
    ShiftedLogNormalExtrapolationFunctionProvider extapFlat = new ShiftedLogNormalExtrapolationFunctionProvider("Flat");
    SmileInterpolatorSABRWithExtrapolation interpQuiet1 = new SmileInterpolatorSABRWithExtrapolation(seed,
        new SABRHaganVolatilityFunction(), beta, weight, extapQuiet);
    SmileInterpolatorSABRWithExtrapolation interpQuiet2 = new SmileInterpolatorSABRWithExtrapolation(seed,
        new SABRHaganVolatilityFunction(), beta * 0.9, weight, extapQuiet);
    SmileInterpolatorSABRWithExtrapolation interpQuiet3 = interpQuiet1;
    SmileInterpolatorSABRWithExtrapolation interpQuiet4 = new SmileInterpolatorSABRWithExtrapolation(seed,
        new SABRHaganVolatilityFunction(), beta, weight, extapQuiet);
    SmileInterpolatorSABRWithExtrapolation interpFlat = new SmileInterpolatorSABRWithExtrapolation(seed,
        new SABRHaganVolatilityFunction(), beta, weight, extapFlat);

    assertTrue(interpQuiet1.equals(interpQuiet1));

    assertTrue(interpQuiet1.hashCode() == interpQuiet3.hashCode());
    assertTrue(interpQuiet1.equals(interpQuiet3));
    assertTrue(interpQuiet3.equals(interpQuiet1));

    assertFalse(interpQuiet1.equals(interpQuiet2));
    assertFalse(interpQuiet1.hashCode() == interpQuiet2.hashCode());

    assertFalse(interpQuiet1.equals(interpFlat));
    assertFalse(interpQuiet1.hashCode() == interpFlat.hashCode());

    assertTrue(interpQuiet1.hashCode() == interpQuiet4.hashCode());
    assertTrue(interpQuiet1.equals(interpQuiet4));
    assertTrue(interpQuiet4.equals(interpQuiet1));

    assertFalse(interpQuiet1.equals(null));
    assertFalse(interpQuiet1.equals(new SmileInterpolatorSABR()));
  }

  /**
   * Quiet and flat expected behavior for shifted lognormal model extrapolation
   */
  @Test
  public void SLNQuietAndFlatTest() {
    double eps = 1.0e-6;

    double expiry = 1.5;
    double forward = 1.1;
    int nStrikes = 10;
    double[] strikes = new double[nStrikes];
    double[] impliedVols = new double[] {1.17, 0.92, 0.802, 0.745, 0.781, 0.812, 0.8334, 0.878, 0.899, 0.9352 };
    for (int i = 0; i < nStrikes; ++i) {
      strikes[i] = forward * (0.85 + i * 0.05);
    }
    ShiftedLogNormalExtrapolationFunctionProvider extapFlat = new ShiftedLogNormalExtrapolationFunctionProvider("Flat");
    SmileInterpolatorSABRWithExtrapolation interpFlat = new SmileInterpolatorSABRWithExtrapolation(extapFlat);
    InterpolatedSmileFunction funcFlat = new InterpolatedSmileFunction(interpFlat, forward, strikes, expiry,
        impliedVols);
    double[] keys = new double[] {0.1 * strikes[0], 0.36 * strikes[0], 0.99 * strikes[0] };
    for (int i = 0; i < keys.length; ++i) {
      assertEquals(impliedVols[0], funcFlat.getVolatility(keys[i]), 1.e-8);
    }
    keys = new double[] {1.1 * strikes[nStrikes - 1], 2.312 * strikes[nStrikes - 1], 12.99 * strikes[nStrikes - 1] };
    for (int i = 0; i < keys.length; ++i) {
      assertEquals(impliedVols[nStrikes - 1], funcFlat.getVolatility(keys[i]), 1.e-8);
    }

    ShiftedLogNormalExtrapolationFunctionProvider extapQuiet = new ShiftedLogNormalExtrapolationFunctionProvider(
        "Quiet");
    SmileInterpolatorSABRWithExtrapolation interpQuiet = new SmileInterpolatorSABRWithExtrapolation(extapQuiet);
    InterpolatedSmileFunction funcQuiet = new InterpolatedSmileFunction(interpQuiet, forward, strikes, expiry,
        impliedVols);
    /*
     * Only C0 continuity is kept
     */
    {
      double CutoffUp = strikes[0] + eps;
      double CutoffDw = strikes[0] - eps;
      double volInt = funcQuiet.getVolatility(CutoffUp);
      double volExt = funcQuiet.getVolatility(CutoffDw);
      double volBoundary = funcQuiet.getVolatility(strikes[0]);
      assertEquals(volBoundary, volInt, eps * 10.0);
      assertEquals(volBoundary, volExt, eps * 10.0);
    }
    {
      double CutoffUp = strikes[nStrikes - 1] + eps;
      double CutoffDw = strikes[nStrikes - 1] - eps;
      double volInt = funcQuiet.getVolatility(CutoffDw);
      double volExt = funcQuiet.getVolatility(CutoffUp);
      double volBoundary = funcQuiet.getVolatility(strikes[nStrikes - 1]);
      assertEquals(volBoundary, volInt, eps * 10.0);
      assertEquals(volBoundary, volExt, eps * 10.0);
    }
  }

  /**
   * Check C1 smoothness of shifted lognormal model extrapolation
   */
  @Test
  public void SLNSmoothnessTest() {
    double eps = 1.0e-5;

    double expiry = 1.5;
    double forward = 1.1;
    int nStrikes = 10;
    double[] strikes = new double[nStrikes];
    double[] impliedVols = new double[] {0.97, 0.92, 0.802, 0.745, 0.781, 0.812, 0.8334, 0.878, 0.899, 0.9052 };
    for (int i = 0; i < nStrikes; ++i) {
      strikes[i] = forward * (0.85 + i * 0.05);
    }
    ShiftedLogNormalExtrapolationFunctionProvider extapSLN = new ShiftedLogNormalExtrapolationFunctionProvider();
    SmileInterpolatorSABRWithExtrapolation interpSLN = new SmileInterpolatorSABRWithExtrapolation(
        new SABRHaganVolatilityFunction(), extapSLN);
    InterpolatedSmileFunction funcSLN = new InterpolatedSmileFunction(interpSLN, forward, strikes, expiry, impliedVols);

    /*
     * left interpolation
     */
    {
      double CutoffUp = strikes[0] + eps;
      double CutoffDw = strikes[0] - eps;
      // Checking volatility function
      double volInt = funcSLN.getVolatility(CutoffUp);
      double volExt = funcSLN.getVolatility(CutoffDw);
      double volBoundary = funcSLN.getVolatility(strikes[0]);
      assertEquals(volBoundary, volInt, eps);
      assertEquals(volBoundary, volExt, eps);
      double volExtDw = funcSLN.getVolatility(CutoffDw - eps);
      double volFirstExt = (1.5 * volBoundary + 0.5 * volExtDw - 2.0 * volExt) / eps;
      double volIntUp = funcSLN.getVolatility(CutoffUp + eps);
      double volFirstInt = (2.0 * volInt - 0.5 * volIntUp - 1.5 * volBoundary) / eps;
      assertEquals(volFirstInt, volFirstExt, eps);
    }
    /*
     * right interpolation
     */
    {
      double CutoffUp = strikes[nStrikes - 1] + eps;
      double CutoffDw = strikes[nStrikes - 1] - eps;
      // Checking volatility function
      double volInt = funcSLN.getVolatility(CutoffDw);
      double volExt = funcSLN.getVolatility(CutoffUp);
      double volBoundary = funcSLN.getVolatility(strikes[nStrikes - 1]);
      assertEquals(volBoundary, volInt, eps);
      assertEquals(volBoundary, volExt, eps);
      double volExtUp = funcSLN.getVolatility(CutoffUp + eps);
      double volFirstExt = (2.0 * volExt - 0.5 * volExtUp - 1.5 * volBoundary) / eps;
      double volIntDw = funcSLN.getVolatility(CutoffDw - eps);
      double volFirstInt = (-2.0 * volInt + 1.5 * volBoundary + 0.5 * volIntDw) / eps;
      assertEquals(volFirstInt, volFirstExt, eps);
    }
  }

  /**
   * Check extrapolation is recovered for shifted lognormal model extrapolation
   */
  @Test
  public void functionRecoverySLNTest() {
    final double forward = 1.0;
    final double expiry = 3.0;
    int nSamples = 11;
    double[] strikes = new double[nSamples];
    double[] vols = new double[nSamples];

    final double muLeft = 0.4;
    final double thetaLeft = 0.5;
    // Expected left extrapolation
    Function1D<Double, Double> left = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double strike) {
        return ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, strike, expiry, muLeft, thetaLeft);
      }
    };

    final double muRight = -0.3;
    final double thetaRight = 0.5;
    // Expected right extrapolation
    Function1D<Double, Double> right = new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double strike) {
        return ShiftedLogNormalTailExtrapolation.impliedVolatility(forward, strike, expiry, muRight, thetaRight);
      }
    };

    for (int i = 0; i < 5; ++i) {
      double strike = forward * (0.75 + 0.05 * i);
      vols[i] = left.evaluate(strike);
      strikes[i] = strike;
    }
    for (int i = 6; i < nSamples; ++i) {
      double strike = forward * (0.75 + 0.05 * i);
      vols[i] = right.evaluate(strike);
      strikes[i] = strike;
    }
    strikes[5] = forward;
    vols[5] = 0.5 * (vols[4] + vols[6]);
    ShiftedLogNormalExtrapolationFunctionProvider extapSLN = new ShiftedLogNormalExtrapolationFunctionProvider();
    SmileInterpolatorSABRWithExtrapolation interpSLN = new SmileInterpolatorSABRWithExtrapolation(extapSLN);
    InterpolatedSmileFunction funcSLN = new InterpolatedSmileFunction(interpSLN, forward, strikes, expiry, vols);
    double[] keys = new double[] {forward * 0.1, forward * 0.5, forward * 0.66 };
    for (int i = 0; i < keys.length; ++i) {
      assertEquals(left.evaluate(keys[i]), funcSLN.getVolatility(keys[i]), 1.e-2);
    }
    keys = new double[] {forward * 1.31, forward * 1.5, forward * 2.61, forward * 15.0 };
    for (int i = 0; i < keys.length; ++i) {
      assertEquals(right.evaluate(keys[i]), funcSLN.getVolatility(keys[i]), 1.e-2);
    }
  }

  /**
   * Check C2 smoothness of Benaim-Dodgson-Kainth extrapolation
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

    double muLow = strikes[0] * BlackFormulaRepository.dualDelta(forward, strikes[0], expiry, impliedVols[0], false) /
        BlackFormulaRepository.price(forward, strikes[0], expiry, impliedVols[0], false);
    double muHigh = -strikes[nStrikes - 1] *
        BlackFormulaRepository.dualDelta(forward, strikes[nStrikes - 1], expiry, impliedVols[nStrikes - 1], true) /
        BlackFormulaRepository.price(forward, strikes[nStrikes - 1], expiry, impliedVols[nStrikes - 1], true);

    SmileExtrapolationFunctionSABRProvider extrapBDK = new BenaimDodgsonKainthExtrapolationFunctionProvider(muLow,
        muHigh);
    SmileInterpolatorSABRWithExtrapolation interpBDK = new SmileInterpolatorSABRWithExtrapolation(
        new SABRBerestyckiVolatilityFunction(), extrapBDK);
    InterpolatedSmileFunction funcBDK = new InterpolatedSmileFunction(interpBDK, forward, strikes, expiry, impliedVols);

    List<SABRFormulaData> modelParams = (new SmileInterpolatorSABR()).getFittedModelParameters(forward, strikes,
        expiry, impliedVols);
    SABRExtrapolationLeftFunction sabrLeftExtrapolation = new SABRExtrapolationLeftFunction(forward,
        modelParams.get(0), strikes[0], expiry, muLow, new SABRHaganVolatilityFunction());
    SABRExtrapolationRightFunction sabrRightExtrapolation = new SABRExtrapolationRightFunction(forward,
        modelParams.get(nStrikes - 3), strikes[nStrikes - 1], expiry, muHigh,
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
      double boundaryValue = sabrRightExtrapolation
          .price(new EuropeanVanillaOption(strikes[nStrikes - 1], expiry, true));
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
   * Check trivial extrapolation is recovered for Benaim-Dodgson-Kainth extrapolation
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
    SmileInterpolatorSABRWithExtrapolation interpBDK = new SmileInterpolatorSABRWithExtrapolation(
        new SABRBerestyckiVolatilityFunction(), extrapBDK);
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

}
