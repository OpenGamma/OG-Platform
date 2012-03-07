/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;


/**
 * 
 */
//public class MixedLogNormalModelFitterTest extends SmileModelFitterTest<MixedLogNormalModelData> {
//  private static RandomEngine RANDOM = new MersenneTwister();
//  private static Logger LOGGER = LoggerFactory.getLogger(MixedLogNormalModelFitterTest.class);
//  private static int N = 3;
//  private static boolean USE_SHIFTED_MEANS = false;
//  private static MixedLogNormalModelData DATA;
//  private static double[] TRUE_PARAMS;
//
//  static {
//    double[] vols = new double[] {0.2, 0.7, 1.0 };
//    double[] w = new double[] {0.8, 0.08, 0.12 };
//    //    double[] f = new double[] {1.0, Double.NaN, 0.5 };
//    //    double temp = w[0] * f[0] + w[2] * f[2];
//    //    f[1] = (1.0 - temp) / w[1];
//    DATA = new MixedLogNormalModelData(w, vols);
//    TRUE_PARAMS = new double[DATA.getNumberOfparameters()];
//    for (int i = 0; i < DATA.getNumberOfparameters(); i++) {
//      TRUE_PARAMS[i] = DATA.getParameter(i) * (1 + RANDOM.nextDouble() * 0.2);
//    }
//  }
//
//  @Override
//  Logger getlogger() {
//    return LOGGER;
//  }
//
//  @Test
//  public void doNothingTest() {
//  }
//
//  @Override
//  MixedLogNormalVolatilityFunction getModel() {
//    return new MixedLogNormalVolatilityFunction();
//  }
//
//  @Override
//  MixedLogNormalModelData getModelData() {
//    return DATA;
//  }
//
//  @Override
//  SmileModelFitter<MixedLogNormalModelData> getFitter(double forward, double[] strikes, double timeToExpiry, double[] impliedVols, double[] error,
//      VolatilityFunctionProvider<MixedLogNormalModelData> model) {
//    return new MixedLogNormalModelFitter(forward, strikes, timeToExpiry, impliedVols, error, model, N, USE_SHIFTED_MEANS);
//  }
//
//  @Override
//  double[][] getStartValues() {
//    return new double[][] {{0.1, 0.1, 0.1, 0.8, 0.8 } };
//  }
//
//  @Override
//  double[] getRandomStartValues() {
//    final int n = USE_SHIFTED_MEANS ? 3 * N - 2 : 2 * N - 1;
//    double[] res = new double[n];
//    res[0] = 0.1 + 0.3 * RANDOM.nextDouble();
//    for (int i = 1; i < N; i++) {
//      res[i] = 0.5 * RANDOM.nextDouble();
//    }
//    for (int i = N; i < n; i++) {
//      res[i] = 2 * Math.PI * RANDOM.nextDouble();
//    }
//    return res;
//  }
//
//  @Override
//  BitSet[] getFixedValues() {
//    final int n = 1;
//    final BitSet[] fixed = new BitSet[n];
//    for (int i = 0; i < n; i++) {
//      fixed[i] = new BitSet();
//    }
//    return fixed;
//  }
//
//}
