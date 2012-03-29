/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation;

import java.util.BitSet;

import com.opengamma.analytics.financial.model.volatility.smile.fitting.SABRModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.fitting.SmileModelFitter;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRFormulaData;
import com.opengamma.analytics.financial.model.volatility.smile.function.SABRHaganVolatilityFunction;
import com.opengamma.analytics.financial.model.volatility.smile.function.VolatilityFunctionProvider;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class SmileInterpolatorSABR extends SmileInterpolator<SABRFormulaData> {

  private static final double DEFAULT_BETA = 0.9;
  private static final VolatilityFunctionProvider<SABRFormulaData> DEFAULT_SABR = new SABRHaganVolatilityFunction();

  private final double _beta;
  private final boolean _externalBeta;

  public SmileInterpolatorSABR() {
    this(DEFAULT_SABR);
  }

  public SmileInterpolatorSABR(final double beta) {
    this(DEFAULT_SABR, beta);
  }

  public SmileInterpolatorSABR(final VolatilityFunctionProvider<SABRFormulaData> model) {
    super(model);
    _beta = DEFAULT_BETA;
    _externalBeta = false;
  }

  public SmileInterpolatorSABR(final VolatilityFunctionProvider<SABRFormulaData> model, final double beta) {
    super(model);
    _beta = beta;
    _externalBeta = true;
  }

  @Override
  protected DoubleMatrix1D getGlobalStart(final double forward, final double[] strikes, final double expiry, final double[] impliedVols) {
    final DoubleMatrix1D fitP = getPolynomialFit(forward, strikes, impliedVols);
    final double a = fitP.getEntry(0);
    final double b = fitP.getEntry(1);
    final double c = fitP.getEntry(2);

    //TODO make better use of the polynomial fit information
    if (Math.abs(b) < 1e-3 && Math.abs(c) < 1e-3) { //almost flat smile
      if (_externalBeta && _beta != 1.0) {
        s_logger.warn("Smile almost flat. Cannot use beta = ", +_beta + " so extenal value ignored, and beta = 1.0 used");
      }
      return new DoubleMatrix1D(a, 1.0, 0.0, Math.max(0.0, 4 * c));
    }
    final double approxAlpha = a * Math.pow(forward, 1 - _beta);
    return new DoubleMatrix1D(approxAlpha, _beta, 0.0, Math.max(0.0, 4 * c));
  }

  @Override
  protected BitSet getGlobalFixedValues() {
    final BitSet res = new BitSet();
    if (_externalBeta) {
      res.set(1);
    }
    return res;
  }

  @Override
  protected BitSet getLocalFixedValues() {
    final BitSet res = new BitSet();
    res.set(1); //beta is always fixed for local (3-point) fit
    return res;
  }

  @Override
  protected SABRFormulaData toSmileModelData(final DoubleMatrix1D modelParameters) {
    return new SABRFormulaData(modelParameters.getData());
  }

  @Override
  protected SmileModelFitter<SABRFormulaData> getFitter(final double forward, final double[] strikes, final double expiry, final double[] impliedVols, final double[] errors) {
    return new SABRModelFitter(forward, strikes, expiry, impliedVols, errors, getModel());
  }

}
