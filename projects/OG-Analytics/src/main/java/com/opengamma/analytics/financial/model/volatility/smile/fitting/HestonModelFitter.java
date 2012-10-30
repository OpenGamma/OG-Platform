/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import com.opengamma.analytics.financial.model.volatility.smile.function.HestonModelData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.NonLinearParameterTransforms;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.UncoupledParameterTransforms;

/**
 * 
 */
public class HestonModelFitter extends SmileModelFitter<HestonModelData> {
  // kappa, theta, vol0, omega, rho

  private static final ParameterLimitsTransform[] DEFAULT_TRANSFORMS;

  static {
    DEFAULT_TRANSFORMS = new ParameterLimitsTransform[5];
    DEFAULT_TRANSFORMS[0] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // kappa > 0
    DEFAULT_TRANSFORMS[1] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // theta > 0
    DEFAULT_TRANSFORMS[2] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); //vol0 > 0
    DEFAULT_TRANSFORMS[3] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // omega > 0
    DEFAULT_TRANSFORMS[4] = new DoubleRangeLimitTransform(-1.0 , 1.0); // -1 <= rho <= 1
  }

  public HestonModelFitter(double forward, double[] strikes, double timeToExpiry, double[] impliedVols, double[] error, VolatilityFunctionProvider<HestonModelData> model) {
    super(forward, strikes, timeToExpiry, impliedVols, error, model);
  }

  @Override
  protected NonLinearParameterTransforms getTransform(DoubleMatrix1D start) {
    BitSet fixed = new BitSet();
    return new UncoupledParameterTransforms(start, DEFAULT_TRANSFORMS, fixed);
  }

  @Override
  protected NonLinearParameterTransforms getTransform(DoubleMatrix1D start, BitSet fixed) {
    return new UncoupledParameterTransforms(start, DEFAULT_TRANSFORMS, fixed);
  }

  @Override
  protected HestonModelData toSmileModelData(DoubleMatrix1D modelParameters) {
    return new HestonModelData(modelParameters.getData());
  }

}
