/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.surface;

import java.util.LinkedHashMap;

import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;

/**
 * 
 */
public class VolatilitySurfaceFitterSABR extends VolatilitySurfaceFitter<SABRFormulaData> {

  private static final ParameterLimitsTransform[] DEFAULT_TRANSFORMS;

  static {
    DEFAULT_TRANSFORMS = new ParameterLimitsTransform[4];
    DEFAULT_TRANSFORMS[0] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // alpha > 0
    DEFAULT_TRANSFORMS[1] = new DoubleRangeLimitTransform(0, 2.0); // 0 <= beta <= 2
    DEFAULT_TRANSFORMS[2] = new DoubleRangeLimitTransform(-1.0, 1.0); // -1 <= rho <= 1
    DEFAULT_TRANSFORMS[3] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // nu > 0
  }

  /**
   * @param forwards Forward values of the underlying at the (increasing) expiry times
   * @param strikes An array of arrays that gives a set of strikes at each maturity (the outer array corresponds to the expiries and the
   *  inner arrays to the set of strikes at a particular expiry)
   * @param expiries The set of (increasing) expiry times
   * @param impliedVols An array of arrays that gives a set of implied volatilities at each maturity (with the same structure as strikes)
   * @param errors An array of arrays that gives a set of 'measurement' errors at each maturity (with the same structure as strikes)
   * @param model A smile model
   * @param knotPoints The time position of the nodes on each model parameter curve
   * @param interpolators The base interpolator used for each model parameter curve
   */
  public VolatilitySurfaceFitterSABR(final double[] forwards, final double[][] strikes, final double[] expiries, final double[][] impliedVols, final double[][] errors,
      final VolatilityFunctionProvider<SABRFormulaData> model, final LinkedHashMap<String, double[]> knotPoints, final LinkedHashMap<String, Interpolator1D> interpolators) {
    super(forwards, strikes, expiries, impliedVols, errors, model, knotPoints, interpolators);
  }

  @Override
  protected SABRFormulaData toSmileModelData(final double[] modelParameters) {
    return new SABRFormulaData(modelParameters);
  }

  @Override
  protected ParameterLimitsTransform[] getTransforms() {
    return DEFAULT_TRANSFORMS;
  }

}
