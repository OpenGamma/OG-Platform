/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import com.opengamma.analytics.financial.model.volatility.smile.function.SVIFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.NonLinearParameterTransforms;
import com.opengamma.analytics.math.minimization.NullTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform;
import com.opengamma.analytics.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.analytics.math.minimization.SingleRangeLimitTransform;
import com.opengamma.analytics.math.minimization.UncoupledParameterTransforms;

/**
 * 
 */
public class SVIModelFitter extends SmileModelFitter<SVIFormulaData> {
  private static final ParameterLimitsTransform[] DEFAULT_TRANSFORMS;

  static {
    DEFAULT_TRANSFORMS = new ParameterLimitsTransform[5];
    DEFAULT_TRANSFORMS[0] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); //a
    DEFAULT_TRANSFORMS[1] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); //b
    DEFAULT_TRANSFORMS[2] = new DoubleRangeLimitTransform(-1.0, 1.0); //rho
    DEFAULT_TRANSFORMS[3] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); //nu
    DEFAULT_TRANSFORMS[4] = new NullTransform(); //m
  }

  public SVIModelFitter(final double forward, final double[] strikes, final double timeToExpiry, final double[] impliedVols, final double[] error,
      final VolatilityFunctionProvider<SVIFormulaData> model) {
    super(forward, strikes, timeToExpiry, impliedVols, error, model);
  }

  @Override
  protected NonLinearParameterTransforms getTransform(final DoubleMatrix1D start) {
    final BitSet fixed = new BitSet(5);
    return new UncoupledParameterTransforms(start, DEFAULT_TRANSFORMS, fixed);
  }

  @Override
  protected NonLinearParameterTransforms getTransform(final DoubleMatrix1D start, final BitSet fixed) {
    return new UncoupledParameterTransforms(start, DEFAULT_TRANSFORMS, fixed);
  }

  @Override
  public SVIFormulaData toSmileModelData(final DoubleMatrix1D modelParameters) {
    return new SVIFormulaData(modelParameters.getData());
  }

  @Override
  protected DoubleMatrix1D getMaximumStep() {
    return null;
  }

}
