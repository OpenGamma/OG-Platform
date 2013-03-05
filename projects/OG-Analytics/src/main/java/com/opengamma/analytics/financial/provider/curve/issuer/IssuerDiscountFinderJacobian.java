/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.issuer;

import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.issuer.AbstractParameterSensitivityIssuerMatrixCalculator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Function computing the Jacobian of the error of valuation produce by a array representing the curve parameters. 
 */
public class IssuerDiscountFinderJacobian extends Function1D<DoubleMatrix1D, DoubleMatrix2D> {

  /**
   * The instrument parameter sensitivity calculator.
   */
  private final AbstractParameterSensitivityIssuerMatrixCalculator _parameterSensitivityCalculator;
  /**
   * The data required for curve building.
   */
  private final IssuerDiscountBuildingData _data;

  /**
   * Constructor.
   * @param parameterSensitivityCalculator The instrument parameter sensitivity calculator.
   * @param data The data required for curve building.
   */
  public IssuerDiscountFinderJacobian(final AbstractParameterSensitivityIssuerMatrixCalculator parameterSensitivityCalculator, final IssuerDiscountBuildingData data) {
    _parameterSensitivityCalculator = parameterSensitivityCalculator;
    _data = data;
  }

  @Override
  public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
    final IssuerProviderDiscount provider = _data.getKnownData().copy();
    IssuerProviderDiscount newCurves = _data.getGeneratorMarket().evaluate(x);
    provider.setAll(newCurves);
    Set<String> curvesSet = _data.getGeneratorMarket().getCurvesList();
    final int nbParameters = _data.getNumberOfInstruments();
    final double[][] res = new double[nbParameters][nbParameters];
    for (int loopinstrument = 0; loopinstrument < _data.getNumberOfInstruments(); loopinstrument++) {
      InstrumentDerivative deriv = _data.getInstrument(loopinstrument);
      res[loopinstrument] = _parameterSensitivityCalculator.calculateSensitivity(deriv, provider, curvesSet).getData();
    }
    return new DoubleMatrix2D(res);
  }

}
