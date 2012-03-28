/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.testng.annotations.Test;

import com.opengamma.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.financial.model.volatility.smile.fitting.interpolation.SmileInterpolator;
import com.opengamma.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorMixedLogNormal;
import com.opengamma.financial.model.volatility.smile.fitting.interpolation.SmileInterpolatorSABR;
import com.opengamma.financial.model.volatility.smile.function.MixedLogNormalModelData;
import com.opengamma.financial.model.volatility.smile.function.MixedLogNormalVolatilityFunction;
import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.math.function.Function1D;

/**
 * 
 */
public class SmileInterpolatorMixedLogNormalTest {

  private static final double FORWARD = 1172.011012;
  private static final double EXPIRY = 1.5;
  private static final double[] STRIKES = new double[] {782.9777301, 982.3904005, 1242.99164, 1547.184937, 1854.305534 };
  private static final double[] VOLS = new double[] {0.311, 0.288, 0.267, 0.271, 0.276 };
  private static final SmileInterpolator<MixedLogNormalModelData> FITTER = new SmileInterpolatorMixedLogNormal();

  @Test
  public void test() {
    Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, VOLS);
    final int n = STRIKES.length;

    for (int i = 0; i < n; i++) {
      final double k = STRIKES[i];
      final double vol = smile.evaluate(k);
      assertEquals(VOLS[i], vol, 1e-6);
    }
  }

  @Test
  public void flatTest() {
    final int n = STRIKES.length;
    final double[] vols = new double[n];
    Arrays.fill(vols, 0.2);

    //    List<MixedLogNormalModelData> parms = FITTER.getFittedModelParameters(FORWARD, STRIKES, EXPIRY, vols);
    //    for (int i = 0; i < parms.size(); i++) {
    //      System.out.println(parms.get(i).toString());
    //    }

    Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      assertEquals(0.2, vol, 1e-8);
    }
  }

  @Test
  //can make this larger than for SABR
  public void smallBumpTest() {
    double bump = 5e-3;
    int index = 1;
    double[] vols = Arrays.copyOf(VOLS, VOLS.length);
    vols[index] += bump;
    Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);
    final int n = STRIKES.length;
    for (int i = 0; i < n; i++) {
      final double k = STRIKES[i];
      final double vol = smile.evaluate(k);
      assertEquals(vols[i], vol, 1e-6);
    }
  }

  @Test//(enabled = false)
  //TODO find out why test is not working
  public void flatWithBumpTest() {
    final int n = STRIKES.length;
    final double[] vols = new double[n];
    Arrays.fill(vols, 0.2);
    double bump = 5e-3;
    int index = 1;
    vols[index] += bump;

    //    List<MixedLogNormalModelData> parms = FITTER.getFittedModelParameters(FORWARD, STRIKES, EXPIRY, vols);
    //    for (int i = 0; i < parms.size(); i++) {
    //      System.out.println(parms.get(i).toString());
    //    }

    Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);

    for (int i = 0; i < n; i++) {
      final double k = STRIKES[i];
      final double vol = smile.evaluate(k);
      assertEquals(vols[i], vol, 1e-6);
    }
  }

  //**********************************************************************************************
  // The following are debug tests that print output. Ensure enabled = false before committing
  //**********************************************************************************************

  @Test
  (enabled = false)
  public void printTest() {

    Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, VOLS);
    //double vol = fitter.getVol(1550);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

  @Test
  (enabled = false)
  public void bumpTest() {
    double bump = 1e-2;
    int index = 1;
    double[] vols = Arrays.copyOf(VOLS, VOLS.length);
    vols[index] += bump;

    List<MixedLogNormalModelData> parms = FITTER.getFittedModelParameters(FORWARD, STRIKES, EXPIRY, vols);
    for (int i = 0; i < parms.size(); i++) {
      System.out.println(parms.get(i).toString());
    }

    MixedLogNormalVolatilityFunction volFunc = new MixedLogNormalVolatilityFunction();

    Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);
    //double vol = fitter.getVol(1550);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      EuropeanVanillaOption option = new EuropeanVanillaOption(k, EXPIRY, true);
      double vol0 = volFunc.getVolatility(option, FORWARD, parms.get(0));
      double vol1 = volFunc.getVolatility(option, FORWARD, parms.get(1));
      double vol2 = volFunc.getVolatility(option, FORWARD, parms.get(2));
      System.out.println(k + "\t" + vol + "\t" + vol0 + "\t" + vol1 + "\t" + vol2);
    }
  }

  @Test
  (enabled = false)
  public void FlatBumpTest() {
    final int n = STRIKES.length;
    final double[] vols = new double[n];
    Arrays.fill(vols, 0.2);
    double bump = 1e-3;
    int index = 1;
    vols[index] += bump;

    List<MixedLogNormalModelData> parms = FITTER.getFittedModelParameters(FORWARD, STRIKES, EXPIRY, vols);
    for (int i = 0; i < parms.size(); i++) {
      System.out.println(parms.get(i).toString());
    }

    Function1D<Double, Double> smile = FITTER.getVolatilityFunction(FORWARD, STRIKES, EXPIRY, vols);
    //double vol = fitter.getVol(1550);

    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      final double vol = smile.evaluate(k);
      System.out.println(k + "\t" + vol);
    }
  }

  @Test(enabled = false)
  public void badFitTest() {
    final double forward = 1.30276013603506;
    final double[] strikes = new double[] {1.080256504787705, 1.161299691076151, 1.329077636516407, 1.5210230159922162, 1.635211041136184 };
    final double expiry = 1.0;
    final double[] impVols = new double[] {0.2, 0.2, 0.2, 0.2, 0.2 };
    double bump = 1e-3;
    int index = 2;
    impVols[index] += bump;
    Function1D<Double, Double> smile = FITTER.getVolatilityFunction(forward, strikes, expiry, impVols);

    final SmileInterpolator<SABRFormulaData> fitterSABR = new SmileInterpolatorSABR();
    Function1D<Double, Double> smileSABR = fitterSABR.getVolatilityFunction(forward, strikes, expiry, impVols);

    for (int i = 0; i < 200; i++) {
      final double k = 0.8 + 1.2 * i / 199.;
      final double vol = smile.evaluate(k);
      final double vol2 = smileSABR.evaluate(k);
      System.out.println(k + "\t" + vol+ "\t" + vol2);
    }
  }

  @Test(enabled = false)
  public void printSmileTest() {
    double sigma = 0.1;
    double dSigma = 0.103;
    double theta = 0.4928;
    double phi = 0.6117;

    //    double sigma = 0.2004;
    //    double dSigma = 0.01;
    //    double theta = 0.7854;
    //    double phi = 0.8854;
    MixedLogNormalModelData data = new MixedLogNormalModelData(new double[] {sigma, dSigma, theta, phi });
    System.out.println(data.toString());
    MixedLogNormalVolatilityFunction volFunc = new MixedLogNormalVolatilityFunction();
    for (int i = 0; i < 200; i++) {
      final double k = 700 + 1300 * i / 199.;
      double vol = volFunc.getVolatility(new EuropeanVanillaOption(k, EXPIRY, true), FORWARD, data);
      System.out.println(k + "\t" + vol);
    }

  }

}
