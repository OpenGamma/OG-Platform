/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.Arrays;
import java.util.BitSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.MathException;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.statistics.leastsquare.LeastSquareResultsWithTransform;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MixedLogNormalModelFitterTest extends SmileModelFitterTest<MixedLogNormalModelData> {
  private static RandomEngine RANDOM = new MersenneTwister();
  private static Logger LOGGER = LoggerFactory.getLogger(MixedLogNormalModelFitterTest.class);
  private static int N = 3;
  private static boolean USE_SHIFTED_MEANS = false;
  private static MixedLogNormalModelData DATA;
  private static double[] TRUE_PARAMS;

  public MixedLogNormalModelFitterTest() {
    _paramValueEps = 4e-4;
  }

  static {
    final double[] vols = new double[] {0.2, 0.7, 1.0 };
    final double[] w = new double[] {0.8, 0.08, 0.12 };
    //    double[] f = new double[] {1.0, Double.NaN, 0.5 };
    //    double temp = w[0] * f[0] + w[2] * f[2];
    //    f[1] = (1.0 - temp) / w[1];
    DATA = new MixedLogNormalModelData(w, vols);
    TRUE_PARAMS = new double[DATA.getNumberOfParameters()];
    for (int i = 0; i < DATA.getNumberOfParameters(); i++) {
      TRUE_PARAMS[i] = DATA.getParameter(i) * (1 + RANDOM.nextDouble() * 0.2);
    }
  }

  @Override
  Logger getlogger() {
    return LOGGER;
  }

  @Test
  public void doNothingTest() {
  }

  @Test(enabled = false)
  public void exactFittingTest() {
    final double forward = 1172.011012;
    final double expiry = 1.5;
    final double[] strikes = new double[] {700, 782.9777301, 982.3904005, 1242.99164, 1547.184937, 1854.305534, 2000 };
    final double[] vols = new double[] {0.311, 0.311, 0.298, 0.267, 0.271, 0.276, 0.276 };
    final int n = vols.length;
    final double[] errors = new double[n];
    Arrays.fill(errors, 1e-4);
    errors[0] = 1e5;
    errors[n - 1] = 1e5;
    final MixedLogNormalVolatilityFunction model = getModel();

    final MixedLogNormalModelFitter fitter = new MixedLogNormalModelFitter(forward, strikes, expiry, vols, errors, model, 3, true);

    double bestChi2 = Double.POSITIVE_INFINITY;
    LeastSquareResultsWithTransform best = null;
    final int tries = 10;
    int fails = 0;
    for (int i = 0; i < tries; i++) {
      final DoubleMatrix1D start = getRandomStart();
      try {
        final LeastSquareResultsWithTransform res = fitter.solve(start);
        if (res.getChiSq() < bestChi2) {
          bestChi2 = res.getChiSq();
          best = res;
        }
      } catch (final MathException e) {
        fails++;
      } catch (final IllegalArgumentException e) {
        System.out.print(e.toString());
        System.out.println(start);
      }
    }
    System.out.println("fail rate:" + (100.0 * fails) / tries + "%");

    // DoubleMatrix1D start = new DoubleMatrix1D(0.1653806454982462, 0.2981998932366687, 1.298321083180569, 0.16115590666749585);
    //  best = fitter.solve(start);
    if (best != null) {
      System.out.println(best.toString());
      final MixedLogNormalModelData data = new MixedLogNormalModelData(best.getModelParameters().getData());
      System.out.println(data.toString());
      for (int i = 0; i < 200; i++) {
        final double k = 500 + 1700 * i / 199.;

        final EuropeanVanillaOption option = new EuropeanVanillaOption(k, expiry, true);
        final double vol = model.getVolatility(option, forward, data);
        System.out.println(k + "\t" + vol);
      }
    }

    //    BitSet fixed = new BitSet();
    //    fixed.set(0);
    //    MixedLogNormalModelData[] localFits = new MixedLogNormalModelData[3];
    //    for(int i=0;i<3;i++) {
    //      double[] tStrikes = Arrays.copyOfRange(strikes, i, i + 3);
    //      double[] tVols = Arrays.copyOfRange(vols, i, i + 3);
    //      errors = new double[3];
    //      Arrays.fill(errors, 0.0001); //1bps
    //
    //      fitter = new MixedLogNormalModelFitter(forward, tStrikes, expiry, tVols, errors, model, 2, true);
    //      LeastSquareResultsWithTransform res = fitter.solve(best.getFitParameters(),fixed);
    //      localFits[i] = new MixedLogNormalModelData(res.getModelParameters().getData());
    //      System.out.println("chi2:"+res.getChiSq()+localFits[i].toString());
    //    }
  }

  private DoubleMatrix1D getRandomStart() {
    final double theta1 = Math.PI / 2 * RANDOM.nextDouble();
    final double theta2 = Math.PI / 2 * RANDOM.nextDouble();
    return new DoubleMatrix1D(0.5 * RANDOM.nextDouble(), 0.5 * RANDOM.nextDouble(), 0.5 * RANDOM.nextDouble(), theta1, theta2, theta1, theta2);
  }

  @Override
  MixedLogNormalVolatilityFunction getModel() {
    return MixedLogNormalVolatilityFunction.getInstance();
  }

  @Override
  MixedLogNormalModelData getModelData() {
    return DATA;
  }

  @Override
  SmileModelFitter<MixedLogNormalModelData> getFitter(final double forward, final double[] strikes, final double timeToExpiry, final double[] impliedVols, final double[] error,
      final VolatilityFunctionProvider<MixedLogNormalModelData> model) {
    return new MixedLogNormalModelFitter(forward, strikes, timeToExpiry, impliedVols, error, model, N, USE_SHIFTED_MEANS);
  }

  @Override
  double[][] getStartValues() {
    return new double[][] { {0.2, 0.4, 0.4, 0.1, 0.1 }, {0.2, 0.4, 0.8, 0.8, 0.8 } };
  }

  @Override
  double[] getRandomStartValues() {
    final int n = USE_SHIFTED_MEANS ? 3 * N - 2 : 2 * N - 1;
    final double[] res = new double[n];
    res[0] = 0.1 + 0.3 * RANDOM.nextDouble();
    for (int i = 1; i < N; i++) {
      res[i] = 0.5 * RANDOM.nextDouble();
    }
    for (int i = N; i < n; i++) {
      res[i] = 2 * Math.PI * RANDOM.nextDouble();
    }
    return res;
  }

  @Override
  BitSet[] getFixedValues() {
    final int n = 2;
    final BitSet[] fixed = new BitSet[n];
    for (int i = 0; i < n; i++) {
      fixed[i] = new BitSet();
    }
    return fixed;
  }

  @Override
  protected DoubleMatrix1D toStandardForm(final DoubleMatrix1D from) {
    final int n = from.getNumberOfElements();
    final double[] temp = new double[n];
    final double[] f = from.getData();
    System.arraycopy(f, 0, temp, 0, N);
    for (int i = N; i < 2 * N - 1; i++) {
      temp[i] = toZeroToPiByTwo(f[i]);
    }
    if (USE_SHIFTED_MEANS) {
      for (int i = 2 * N - 1; i < 3 * N - 2; i++) {
        temp[i] = toZeroToPiByTwo(f[i]);
      }
    }
    return new DoubleMatrix1D(temp);
  }

  private double toZeroToPiByTwo(final double theta) {
    double x = theta;
    if (x < 0) {
      x = -x;
    }
    if (x > Math.PI / 2) {
      final int p = (int) (x / Math.PI);
      x -= p * Math.PI;
      if (x > Math.PI / 2) {
        x = -x + Math.PI;
      }
    }
    return x;
  }

}
