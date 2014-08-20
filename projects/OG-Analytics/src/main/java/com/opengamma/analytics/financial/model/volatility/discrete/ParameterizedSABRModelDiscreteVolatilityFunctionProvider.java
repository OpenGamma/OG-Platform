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
import com.opengamma.analytics.math.function.DoublesVectorFunctionProvider;
import com.opengamma.analytics.math.function.ParameterizedCurve;
import com.opengamma.analytics.math.function.VectorFunction;

/**
 * This is a concrete implementation of a ParameterizedSmileModelDiscreateVolatilityFunctionProvider where the 
 * 'smile' model is Hagan's SABR formula 
 */
public class ParameterizedSABRModelDiscreteVolatilityFunctionProvider extends ParameterizedSmileModelDiscreateVolatilityFunctionProvider<SABRFormulaData> {
  private static final int NUM_MODEL_PARMS = 4;

  private static final VolatilityFunctionProvider<SABRFormulaData> SABR = new SABRHaganVolatilityFunction();

  /**
   * Set up  a {@link DiscreteVolatilityFunctionProvider} backed by a SABR model
   * @param fwdCurve The forward curve 
   * @param smileModelParameterProviders each of these providers represents a different smile parameter - <b>there 
   * must be one for each smile model parameter</b>. Given a (common) set of expiries, each one provides a 
   * {@link VectorFunction} that gives the corresponding smile model parameter at each expiry for a set of model 
   * parameters. This gives a lot of flexibility as to how the (smile model) parameter term structures are represented. 
   */
  public ParameterizedSABRModelDiscreteVolatilityFunctionProvider(final ForwardCurve fwdCurve, final DoublesVectorFunctionProvider[] smileModelParameterProviders) {
    super(SABR, fwdCurve, smileModelParameterProviders);
  }

  /**
   * Set up the {@link DiscreteVolatilityFunctionProvider} backed by a SABR model
   * @param fwdCurve The forward curve 
   * @param smileParameterTS each of these represents a different smile parameter term structure- <b>there 
   * must be one for each smile model parameter</b>. 
   */
  public ParameterizedSABRModelDiscreteVolatilityFunctionProvider(final ForwardCurve fwdCurve, final ParameterizedCurve[] smileParameterTS) {
    super(SABR, fwdCurve, smileParameterTS);
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
