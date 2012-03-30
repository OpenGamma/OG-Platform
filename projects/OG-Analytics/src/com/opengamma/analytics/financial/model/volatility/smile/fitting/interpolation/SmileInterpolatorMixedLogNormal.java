/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import java.util.BitSet;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.MixedLogNormalModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SmileModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class SmileInterpolatorMixedLogNormal extends SmileInterpolator<MixedLogNormalModelData> {
  private static final VolatilityFunctionProvider<MixedLogNormalModelData> MODEL = new MixedLogNormalVolatilityFunction();

  /**
   * @param model
   */
  public SmileInterpolatorMixedLogNormal() {
    super(MODEL);
  }

  //  @Override
  //  protected double[][] getStrikesVolsAndErrors(final int index, final double[] strikes, final double[] impliedVols, final double[] errors) {
  //    return getStrikesVolsAndErrorsForThreePoints(index, strikes, impliedVols, errors)
  //    //return getStrikesVolsAndErrorsForAllPoints(index, strikes, impliedVols, errors);
  //  }

  @Override
  protected DoubleMatrix1D getGlobalStart(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {
    final DoubleMatrix1D fitP = getPolynomialFit(forward, strikes, impliedVols);

    final double a = fitP.getEntry(0);
    final double b = fitP.getEntry(1);
    final double c = fitP.getEntry(2);

    if (Math.abs(b) < 1e-3 && Math.abs(c) < 1e-3) { //almost flat smile
      final double theta = Math.PI / 2 - 0.01;
      return new DoubleMatrix1D(a, 0.01, theta, theta);
    }
    final double theta = Math.PI / 2 * RANDOM.nextDouble();
    return new DoubleMatrix1D(a * (0.8 + 0.4 * RANDOM.nextDouble()), a * 0.5 * RANDOM.nextDouble(), theta, theta);
  }

  @Override
  protected BitSet getLocalFixedValues() {
    final BitSet res = new BitSet();
    res.set(2); //fit vol 0 for local (3-point) fit
    return res;
  }

  @Override
  protected MixedLogNormalModelData toSmileModelData(final DoubleMatrix1D modelParameters) {
    return new MixedLogNormalModelData(modelParameters.getData());
  }

  @Override
  protected SmileModelFitter<MixedLogNormalModelData> getFitter(final double forward, final double[] strikes, final double expiry, final double[] impliedVols, final double[] errors) {
    return new MixedLogNormalModelFitter(forward, strikes, expiry, impliedVols, errors, MODEL, 2, true);
  }

}
