/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import java.util.BitSet;

import cern.jet.random.engine.RandomEngine;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.MixedLogNormalModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SmileModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.MixedLogNormalVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class SmileInterpolatorMixedLogNormal extends SmileInterpolator<MixedLogNormalModelData> {
  private static final VolatilityFunctionProvider<MixedLogNormalModelData> MODEL = MixedLogNormalVolatilityFunction.getInstance();

  public SmileInterpolatorMixedLogNormal() {
    super(MODEL);
  }

  public SmileInterpolatorMixedLogNormal(final int seed) {
    super(seed, MODEL);
  }

  public SmileInterpolatorMixedLogNormal(final WeightingFunction weightingFunction) {
    super(MODEL, weightingFunction);
  }

  public SmileInterpolatorMixedLogNormal(final int seed, final WeightingFunction weightingFunction) {
    super(seed, MODEL, weightingFunction);
  }

  @Override
  protected DoubleMatrix1D getGlobalStart(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {
    final RandomEngine random = getRandom();
    final DoubleMatrix1D fitP = getPolynomialFit(forward, strikes, impliedVols);

    final double a = fitP.getEntry(0);
    final double b = fitP.getEntry(1);
    final double c = fitP.getEntry(2);

    if (Math.abs(b) < 1e-3 && Math.abs(c) < 1e-3) { //almost flat smile
      final double theta = Math.PI / 2 - 0.01;
      return new DoubleMatrix1D(a, 0.01, theta, theta);
    }
    final double theta = Math.PI / 2 * random.nextDouble();
    return new DoubleMatrix1D(a * (0.8 + 0.4 * random.nextDouble()), a * 0.5 * random.nextDouble(), theta, theta);
  }

  @Override
  protected BitSet getLocalFixedValues() {
    final BitSet res = new BitSet();
    res.set(2); //fit vol 0 for local (3-point) fit
    return res;
  }

  @Override
  protected MixedLogNormalModelData toSmileModelData(final DoubleMatrix1D modelParameters) {
    ArgumentChecker.notNull(modelParameters, "model parameters");
    return new MixedLogNormalModelData(modelParameters.getData());
  }

  @Override
  protected SmileModelFitter<MixedLogNormalModelData> getFitter(final double forward, final double[] strikes, final double expiry, final double[] impliedVols, final double[] errors) {
    return new MixedLogNormalModelFitter(forward, strikes, expiry, impliedVols, errors, MODEL, 2, true);
  }

}
