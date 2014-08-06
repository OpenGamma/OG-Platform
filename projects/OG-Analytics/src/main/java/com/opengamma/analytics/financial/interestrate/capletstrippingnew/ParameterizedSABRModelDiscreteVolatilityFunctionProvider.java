/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.capletstrippingnew;

import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;

/**
 * 
 */
public class ParameterizedSABRModelDiscreteVolatilityFunctionProvider extends ParameterizedSmileModelDiscreateVolatilityFunctionProvider<SABRFormulaData> {

  private static final VolatilityFunctionProvider<SABRFormulaData> SABR = new SABRHaganVolatilityFunction();

  public ParameterizedSABRModelDiscreteVolatilityFunctionProvider(final ForwardCurve fwdCurve, final VectorFunction modelToSmileModelParms) {
    super(SABR, fwdCurve, modelToSmileModelParms);
  }

  @Override
  protected SABRFormulaData toModelData(final double[] x) {
    return new SABRFormulaData(x);
  }

  @Override
  public int getNumSmileModelParamters() {
    return 4;
  }

}
