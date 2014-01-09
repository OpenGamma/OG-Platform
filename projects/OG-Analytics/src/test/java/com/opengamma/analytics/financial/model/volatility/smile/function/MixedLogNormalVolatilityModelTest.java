/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.function;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MixedLogNormalVolatilityModelTest {
  private static final RandomEngine RANDOM = new MersenneTwister64(MersenneTwister.DEFAULT_SEED);
  private static final MixedLogNormalModelData LEPTOKURTIC1;
  private static final MixedLogNormalModelData LEPTOKURTIC2;
  private static final MixedLogNormalModelData PLATYKURTIC;
  private static final MixedLogNormalModelData LARGE_SYSTEM;
  private static final double FORWARD = 0.05;
  private static final double T = 0.6;
  private static final MixedLogNormalVolatilityFunction VOL_FUNC = MixedLogNormalVolatilityFunction.getInstance();
  private static final VolatilityFunctionProvider<MixedLogNormalModelData> FD_VOL_FUNC = new VolatilityFunctionProvider<MixedLogNormalModelData>() {

    @SuppressWarnings("synthetic-access")
    @Override
    public Function1D<MixedLogNormalModelData, Double> getVolatilityFunction(final EuropeanVanillaOption option, final double forward) {
      return VOL_FUNC.getVolatilityFunction(option, forward);
    }
  };

  static {
    LEPTOKURTIC1 = new MixedLogNormalModelData(new double[] {0.8, 0.2 }, new double[] {0.2, 0.7 });
    LEPTOKURTIC2 = new MixedLogNormalModelData(new double[] {0.8, 0.2 }, new double[] {0.2, 0.7 }, new double[] {1.1, 0.6 });
    PLATYKURTIC = new MixedLogNormalModelData(new double[] {0.5, 0.5 }, new double[] {0.2, 0.2 }, new double[] {0.5, 1.5 });
    final int n = 5;
    final double[] parms = new double[3 * n - 2];
    parms[0] = 0.2;
    for (int i = 1; i < n; i++) {
      parms[i] = 0.5 * RANDOM.nextDouble();
    }
    for (int i = 0; i < n - 1; i++) {
      parms[n + i] = Math.PI / 2 * RANDOM.nextDouble();
      parms[2 * n - 1 + i] = parms[n + i] * (0.9 + 0.2 * RANDOM.nextDouble());
    }
    LARGE_SYSTEM = new MixedLogNormalModelData(parms);
  }

  @Test(enabled = false)
  public void printTest() {
    for (int i = 0; i < 101; i++) {
      final double k = FORWARD * (0.5 + 2.5 * i / 100.);
      final EuropeanVanillaOption option = new EuropeanVanillaOption(k, T, true);
      final double vol1 = VOL_FUNC.getVolatility(option, FORWARD, LEPTOKURTIC1);
      final double vol2 = VOL_FUNC.getVolatility(option, FORWARD, LEPTOKURTIC2);
      final double vol3 = VOL_FUNC.getVolatility(option, FORWARD, PLATYKURTIC);
      System.out.println(k + "\t" + vol1 + "\t" + vol2 + "\t" + vol3);
    }
  }

  @Test
  public void smileTest() {

    final double shift = 1e-4;
    final EuropeanVanillaOption optionPlus = new EuropeanVanillaOption((1 + shift) * FORWARD, T, true);
    final EuropeanVanillaOption option = new EuropeanVanillaOption(FORWARD, T, true);
    final EuropeanVanillaOption optionMinus = new EuropeanVanillaOption((1 - shift) * FORWARD, T, true);
    final MixedLogNormalModelData[] data = new MixedLogNormalModelData[] {LEPTOKURTIC1, LEPTOKURTIC2, PLATYKURTIC };
    final double[] skew = new double[3];
    final double[] kurt = new double[3];
    for (int i = 0; i < 3; i++) {
      skew[i] = (VOL_FUNC.getVolatility(optionPlus, FORWARD, data[i]) - VOL_FUNC.getVolatility(optionMinus, FORWARD, data[i])) / 2 / shift / FORWARD;
      kurt[i] = (VOL_FUNC.getVolatility(optionPlus, FORWARD, data[i]) + VOL_FUNC.getVolatility(optionMinus, FORWARD, data[i])
          - 2 * VOL_FUNC.getVolatility(option, FORWARD, data[i])) / shift / shift / FORWARD / FORWARD;
    }
    assertEquals("leptokurtic1", 0, skew[0], 1e-6);
    assertTrue("leptokurtic2", skew[1] < 0);
    assertTrue("leptokurtic1", kurt[0] > 0);
    assertTrue("leptokurtic2", kurt[1] > 0);
    assertTrue("platykurtic", kurt[2] < 0);
  }

  @Test
  public void modelAdjointTest() {
    modelAdjointTest(LEPTOKURTIC1);
    modelAdjointTest(LEPTOKURTIC2);
    modelAdjointTest(LARGE_SYSTEM);
  }

  private void modelAdjointTest(final MixedLogNormalModelData data) {
    final double strike = 1.1 * FORWARD;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, T, true);
    final Function1D<MixedLogNormalModelData, double[]> modelAdjointFunc = VOL_FUNC.getModelAdjointFunction(option, FORWARD);
    final Function1D<MixedLogNormalModelData, double[]> fdModelAdjointFunc = FD_VOL_FUNC.getModelAdjointFunction(option, FORWARD);

    final double[] sense = modelAdjointFunc.evaluate(data);
    final double[] fdSense = fdModelAdjointFunc.evaluate(data);
    final int nParms = data.getNumberOfParameters();
    for (int i = 0; i < nParms; i++) {
      assertEquals(" : parameter " + i, fdSense[i], sense[i], 1e-6);
    }

  }

  @Test
  public void volatilityAdjointTest() {
    volatilityAdjointTest(LEPTOKURTIC1);
    volatilityAdjointTest(LEPTOKURTIC2);
    volatilityAdjointTest(LARGE_SYSTEM);
  }

  public void volatilityAdjointTest(final MixedLogNormalModelData data) {
    final double strike = 0.8 * FORWARD;
    final EuropeanVanillaOption option = new EuropeanVanillaOption(strike, T, true);
    final Function1D<MixedLogNormalModelData, double[]> modelAdjointFunc = VOL_FUNC.getVolatilityAdjointFunction(option, FORWARD);
    final Function1D<MixedLogNormalModelData, double[]> fdModelAdjointFunc = FD_VOL_FUNC.getVolatilityAdjointFunction(option, FORWARD);

    final double[] sense = modelAdjointFunc.evaluate(data);
    final double[] fdSense = fdModelAdjointFunc.evaluate(data);
    final int nParms = 3 + data.getNumberOfParameters();
    for (int i = 0; i < nParms; i++) {
      assertEquals("parameter " + i, fdSense[i], sense[i], 1e-6);
    }
  }

}
