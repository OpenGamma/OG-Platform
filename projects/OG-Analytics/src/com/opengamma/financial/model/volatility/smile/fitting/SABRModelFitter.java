/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.model.volatility.smile.fitting;

import java.util.BitSet;

import com.opengamma.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.minimization.DoubleRangeLimitTransform;
import com.opengamma.math.minimization.NonLinearParameterTransforms;
import com.opengamma.math.minimization.ParameterLimitsTransform;
import com.opengamma.math.minimization.ParameterLimitsTransform.LimitType;
import com.opengamma.math.minimization.SingleRangeLimitTransform;
import com.opengamma.math.minimization.UncoupledParameterTransforms;

/**
 * 
 */
public class SABRModelFitter extends SmileModelFitter<SABRFormulaData> {
  private static final ParameterLimitsTransform[] DEFAULT_TRANSFORMS;

  static {
    DEFAULT_TRANSFORMS = new ParameterLimitsTransform[4];
    DEFAULT_TRANSFORMS[0] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // alpha > 0
    DEFAULT_TRANSFORMS[1] = new DoubleRangeLimitTransform(0, 2.0); // 0 <= beta <= 2
    DEFAULT_TRANSFORMS[2] = new DoubleRangeLimitTransform(-1.0, 1.0); // -1 <= rho <= 1
    DEFAULT_TRANSFORMS[3] = new SingleRangeLimitTransform(0, LimitType.GREATER_THAN); // nu > 0
  }

  public SABRModelFitter(final double forward, final double[] strikes, final double timeToExpiry, final double[] impliedVols,
      final double[] error, VolatilityFunctionProvider<SABRFormulaData> model) {
    super(forward, strikes, timeToExpiry, impliedVols, error, model);
  }

  @Override
  protected SABRFormulaData toSmileModelData(final DoubleMatrix1D modelParameters) {
    return new SABRFormulaData(modelParameters.getData());
  }

  @Override
  protected NonLinearParameterTransforms getTransform(DoubleMatrix1D start) {
    BitSet fixed = new BitSet();
    return new UncoupledParameterTransforms(start, DEFAULT_TRANSFORMS, fixed);
  }

  @Override
  protected NonLinearParameterTransforms getTransform(DoubleMatrix1D start, final BitSet fixed) {
    return new UncoupledParameterTransforms(start, DEFAULT_TRANSFORMS, fixed);
  }

}
