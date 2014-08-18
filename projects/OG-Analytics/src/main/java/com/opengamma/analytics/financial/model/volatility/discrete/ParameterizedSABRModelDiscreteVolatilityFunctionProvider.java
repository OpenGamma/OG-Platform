/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.discrete;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.function.VectorFunction;

/**
 * 
 */
public class ParameterizedSABRModelDiscreteVolatilityFunctionProvider extends ParameterizedSmileModelDiscreateVolatilityFunctionProvider<SABRFormulaData> {
  private static final int NUM_MODEL_PARMS = 4;

  private static final VolatilityFunctionProvider<SABRFormulaData> SABR = new SABRHaganVolatilityFunction();

  public ParameterizedSABRModelDiscreteVolatilityFunctionProvider(final ForwardCurve fwdCurve, final VectorFunction modelToSmileModelParms) {
    super(SABR, fwdCurve, modelToSmileModelParms);
  }

  @Override
  public int getNumSmileModelParamters() {
    return NUM_MODEL_PARMS;
  }

  @Override
  protected SABRFormulaData toSmileModelData(final double[] modelParameters) {
    return new SABRFormulaData(modelParameters);
  }

}
