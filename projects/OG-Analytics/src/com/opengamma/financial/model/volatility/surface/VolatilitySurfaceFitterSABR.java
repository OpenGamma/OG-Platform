/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.surface;

import java.util.LinkedHashMap;

import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.math.minimization.SingleRangeLimitTransform;

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
   * @param forwards
   * @param strikes
   * @param expiries
   * @param impliedVols
   * @param errors
   * @param model
   * @param knotPoints
   * @param interpolators
   */
  public VolatilitySurfaceFitterSABR(double[] forwards, double[][] strikes, double[] expiries, double[][] impliedVols, double[][] errors, VolatilityFunctionProvider<SABRFormulaData> model,
      LinkedHashMap<String, double[]> knotPoints, LinkedHashMap<String, Interpolator1D> interpolators) {
    super(forwards, strikes, expiries, impliedVols, errors, model, knotPoints, interpolators);
  }

  @Override
  protected SABRFormulaData toSmileModelData(double[] modelParameters) {
    return new SABRFormulaData(modelParameters);
  }

  @Override
  protected ParameterLimitsTransform[] getTransforms() {
    return DEFAULT_TRANSFORMS;
  }

}
