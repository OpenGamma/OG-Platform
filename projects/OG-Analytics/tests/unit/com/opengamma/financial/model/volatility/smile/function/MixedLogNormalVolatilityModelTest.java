/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.function;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class MixedLogNormalVolatilityModelTest {

  private static final MixedLogNormalModelData LEPTOKURTIC1;
  private static final MixedLogNormalModelData LEPTOKURTIC2;
  private static final MixedLogNormalModelData PLATYKURTIC;
  private static final double FORWARD = 0.05;
  private static final double T = 0.6;
  private static final MixedLogNormalVolatilityFunction VOL_FUNC = new MixedLogNormalVolatilityFunction();
  private static final VolatilityFunctionProvider<MixedLogNormalModelData> FD_VOL_FUNC = new VolatilityFunctionProvider<MixedLogNormalModelData>() {

    @Override
    public Function1D<MixedLogNormalModelData, Double> getVolatilityFunction(EuropeanVanillaOption option, double forward) {
      return VOL_FUNC.getVolatilityFunction(option, forward);
    }
  };

  static {
    LEPTOKURTIC1 = new MixedLogNormalModelData(new double[] {0.8, 0.2 }, new double[] {0.2, 0.7 });
    LEPTOKURTIC2 = new MixedLogNormalModelData(new double[] {0.8, 0.2 }, new double[] {0.2, 0.7 }, new double[] {1.1, 0.6 });
    PLATYKURTIC = new MixedLogNormalModelData(new double[] {0.5, 0.5 }, new double[] {0.2, 0.2 }, new double[] {0.5, 1.5 });
  }

  @Test(enabled = false)
  public void printTest() {
    for (int i = 0; i < 101; i++) {
      double k = FORWARD * (0.5 + 2.5 * i / 100.);
      EuropeanVanillaOption option = new EuropeanVanillaOption(k, T, true);
      double vol1 = VOL_FUNC.getVolatility(option, FORWARD, LEPTOKURTIC1);
      double vol2 = VOL_FUNC.getVolatility(option, FORWARD, LEPTOKURTIC2);
      double vol3 = VOL_FUNC.getVolatility(option, FORWARD, PLATYKURTIC);
      System.out.println(k + "\t" + vol1 + "\t" + vol2 + "\t" + vol3);
    }
  }

  @Test
  public void smileTest() {

    final double shift = 1e-4;
    EuropeanVanillaOption optionPlus = new EuropeanVanillaOption((1 + shift) * FORWARD, T, true);
    EuropeanVanillaOption option = new EuropeanVanillaOption(FORWARD, T, true);
    EuropeanVanillaOption optionMinus = new EuropeanVanillaOption((1 - shift) * FORWARD, T, true);
    MixedLogNormalModelData[] data = new MixedLogNormalModelData[] {LEPTOKURTIC1, LEPTOKURTIC2, PLATYKURTIC };
    double[] skew = new double[3];
    double[] kurt = new double[3];
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
    final double strike = 1.1 * FORWARD;
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, T, true);
    Function1D<MixedLogNormalModelData, double[]> modelAdjointFunc = VOL_FUNC.getModelAdjointFunction(option, FORWARD);
    Function1D<MixedLogNormalModelData, double[]> fdModelAdjointFunc = FD_VOL_FUNC.getModelAdjointFunction(option, FORWARD);

    double[] sense = modelAdjointFunc.evaluate(LEPTOKURTIC1);
    double[] fdSense = fdModelAdjointFunc.evaluate(LEPTOKURTIC1);
    int nParms = LEPTOKURTIC1.getNumberOfparameters();
    for (int i = 0; i < nParms; i++) {
      assertEquals("LEPTOKURTIC1: parameter " + i, fdSense[i], sense[i], 1e-6);
    }

    sense = modelAdjointFunc.evaluate(LEPTOKURTIC2);
    fdSense = fdModelAdjointFunc.evaluate(LEPTOKURTIC2);
    nParms = LEPTOKURTIC2.getNumberOfparameters();
    for (int i = 0; i < nParms; i++) {
      assertEquals("LEPTOKURTIC2: parameter " + i, fdSense[i], sense[i], 1e-6);
    }
  }

  @Test
  public void volatilityAdjointTest() {
    final double strike = 0.8 * FORWARD;
    EuropeanVanillaOption option = new EuropeanVanillaOption(strike, T, true);
    Function1D<MixedLogNormalModelData, double[]> modelAdjointFunc = VOL_FUNC.getVolatilityAdjointFunction(option, FORWARD);
    Function1D<MixedLogNormalModelData, double[]> fdModelAdjointFunc = FD_VOL_FUNC.getVolatilityAdjointFunction(option, FORWARD);

    double[] sense = modelAdjointFunc.evaluate(LEPTOKURTIC1);
    double[] fdSense = fdModelAdjointFunc.evaluate(LEPTOKURTIC1);
    int nParms = 3 + LEPTOKURTIC1.getNumberOfparameters();
    for (int i = 0; i < nParms; i++) {
      assertEquals("LEPTOKURTIC1: parameter " + i, fdSense[i], sense[i], 1e-6);
    }

    sense = modelAdjointFunc.evaluate(LEPTOKURTIC2);
    fdSense = fdModelAdjointFunc.evaluate(LEPTOKURTIC2);
    nParms = 3 + LEPTOKURTIC2.getNumberOfparameters();
    for (int i = 0; i < nParms; i++) {
      assertEquals("LEPTOKURTIC2: parameter " + i, fdSense[i], sense[i], 1e-6);
    }
  }

}
