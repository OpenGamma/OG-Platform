/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
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
public class SABRModelFitter extends SmileModelFitter<SABRFormulaData> {
  private static final ParameterLimitsTransform[] DEFAULT_TRANSFORMS;

  static {
    DEFAULT_TRANSFORMS = new ParameterLimitsTransform[4];
    DEFAULT_TRANSFORMS[0] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // alpha > 0
    DEFAULT_TRANSFORMS[1] = new DoubleRangeLimitTransform(0, 1.0); // 0 <= beta <= 1
    DEFAULT_TRANSFORMS[2] = new DoubleRangeLimitTransform(-1.0, 1.0); // -1 <= rho <= 1
    DEFAULT_TRANSFORMS[3] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // nu > 0
  }

  public SABRModelFitter(final double forward, final double[] strikes, final double timeToExpiry, final double[] impliedVols,
      final double[] error, final VolatilityFunctionProvider<SABRFormulaData> model) {
    super(forward, strikes, timeToExpiry, impliedVols, error, model);
  }

  @Override
  public SABRFormulaData toSmileModelData(final DoubleMatrix1D modelParameters) {
    return new SABRFormulaData(modelParameters.getData());
  }

  @Override
  protected NonLinearParameterTransforms getTransform(final DoubleMatrix1D start) {
    final BitSet fixed = new BitSet();
    return new UncoupledParameterTransforms(start, DEFAULT_TRANSFORMS, fixed);
  }

  @Override
  protected NonLinearParameterTransforms getTransform(final DoubleMatrix1D start, final BitSet fixed) {
    return new UncoupledParameterTransforms(start, DEFAULT_TRANSFORMS, fixed);
  }

  @Override
  protected DoubleMatrix1D getMaximumStep() {
    return null;
  }

}
