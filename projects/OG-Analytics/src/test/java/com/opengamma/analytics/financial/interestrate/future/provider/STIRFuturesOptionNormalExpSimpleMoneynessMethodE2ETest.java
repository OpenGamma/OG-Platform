/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.provider;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;

/**
 * 
 */
public class STIRFuturesOptionNormalExpSimpleMoneynessMethodE2ETest {

  private static final Interpolator1D SQUARE_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final Interpolator1D TIME_SQUARE_FLAT = CombinedInterpolatorExtrapolatorFactory.getInterpolator(
      Interpolator1DFactory.TIME_SQUARE, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);
  private static final GridInterpolator2D INTERPOLATOR_2D = new GridInterpolator2D(TIME_SQUARE_FLAT, SQUARE_FLAT);

  private static final double[] EXPIRY;
  private static final double[] SIMPLEMONEY;
  private static final double[] VOL = new double[] {
      1.0623, 1.0623, 1.0623, 1.0623, 1.0623, 0.9517, 0.8098, 0.6903, 0.6519, 0.6872, 0.7490, 0.8161, 0.8823,
      1.0623, 1.0623, 1.0623, 1.0623, 1.0623, 0.9517, 0.8098, 0.6903, 0.6519, 0.6872, 0.7490, 0.8161, 0.8823,
      1.0623, 1.0623, 1.0623, 1.0623, 1.0623, 0.9517, 0.8098, 0.6903, 0.6519, 0.6872, 0.7490, 0.8161, 0.8823,
      1.1414, 1.0815, 1.0316, 0.9926, 0.9638, 0.8791, 0.7843, 0.7094, 0.6817, 0.6948, 0.7252, 0.7617, 0.8002,
      1.1278, 1.0412, 0.9654, 0.9021, 0.8511, 0.8108, 0.7794, 0.7551, 0.7369, 0.7240, 0.7160, 0.7128, 0.7144,
      0.9697, 0.9412, 0.9130, 0.8854, 0.8585, 0.8327, 0.8084, 0.7861, 0.7664, 0.7502, 0.7383, 0.7318, 0.7317,
      0.9611, 0.9265, 0.8938, 0.8630, 0.8347, 0.8089, 0.7859, 0.7659, 0.7489, 0.7351, 0.7242, 0.7161, 0.7105,
      0.9523, 0.9116, 0.8741, 0.8401, 0.8101, 0.7843, 0.7626, 0.7451, 0.7310, 0.7197, 0.7098, 0.7000, 0.6886
  };
  private static final double[] EXPIRY_SET = new double[] {7.0 / 365.0, 14.0 / 365.0, 21.0 / 365.0, 30.0 / 365.0,
      60.0 / 365.0, 90.0 / 365.0, 120.0 / 365.0, 180.0 / 365.0 };
  private static final double[] MONEYNESS_SET = new double[] {-8.0E-3, -7.0E-3, -6.0E-3, -5.0E-3, -4.0E-3, -3.0E-3,
      -2.0E-3, -1.0E-3, 0.0, 1.0E-3, 2.0E-3, 3.0E-3, 4.0E-3 };
  private static final int NUM_EXPIRY = EXPIRY_SET.length;
  private static final int NUM_MONEY = MONEYNESS_SET.length;
  static {
    int nTotal = NUM_EXPIRY * NUM_MONEY;
    EXPIRY = new double[nTotal];
    SIMPLEMONEY = new double[nTotal];
    for (int i = 0; i < NUM_EXPIRY; ++i) {
      for (int j = 0; j < NUM_MONEY; ++j) {
        EXPIRY[i * NUM_MONEY + j] = EXPIRY_SET[i];
        SIMPLEMONEY[i * NUM_MONEY + j] = MONEYNESS_SET[j];
      }
    }
  }
  
  final private static InterpolatedDoublesSurface VOL_SURFACE_SIMPLEMONEY = InterpolatedDoublesSurface.from(EXPIRY,
      SIMPLEMONEY, VOL, INTERPOLATOR_2D);

  @Test
  public void volatilitySurfaceTest() {
    int nSample = 100;
    double minExpiry = EXPIRY_SET[0] * 0.8;
    double maxExpiry = EXPIRY_SET[NUM_EXPIRY - 1] * 1.2;
    double intervalExpiry = (maxExpiry - minExpiry) / (nSample - 1.0);

    double minMoney = MONEYNESS_SET[0] * 1.2;
    double maxMoney = MONEYNESS_SET[NUM_MONEY - 1] * 1.2;
    double intervalMoney = (maxMoney - minMoney) / (nSample - 1.0);

    for (int j = 0; j < nSample; ++j) {
      double moneyness = minMoney + intervalMoney * j;
      System.out.print("\t" + moneyness);
    }
    System.out.print("\n");
    for (int i = 0; i < nSample; ++i) {
      double expiry = minExpiry + intervalExpiry * i;
      System.out.print(expiry);
      for (int j = 0; j < nSample; ++j) {
        double moneyness = minMoney + intervalMoney * j;
        System.out.print("\t" + VOL_SURFACE_SIMPLEMONEY.getZValue(expiry, moneyness));
      }
      System.out.print("\n");
    }

  }
}
