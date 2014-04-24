/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.inflationissuer;

import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.sensitivity.inflationissuer.ParameterSensitivityInflationIssuerMatrixProviderAbstractCalculator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Function computing the Jacobian of the error of valuation produce by a array representing the curve parameters. 
 */
public class InflationIssuerDiscountFinderJacobian extends Function1D<DoubleMatrix1D, DoubleMatrix2D> {

  /**
   * The instrument parameter sensitivity calculator.
   */
  private final ParameterSensitivityInflationIssuerMatrixProviderAbstractCalculator _parameterSensitivityCalculator;
  /**
   * The data required for curve building.
   */
  private final InflationIssuerDiscountBuildingData _data;

  /**
   * Constructor.
   * @param parameterSensitivityCalculator The instrument parameter sensitivity calculator.
   * @param data The data required for curve building.
   */
  public InflationIssuerDiscountFinderJacobian(final ParameterSensitivityInflationIssuerMatrixProviderAbstractCalculator parameterSensitivityCalculator,
      final InflationIssuerDiscountBuildingData data) {
    _parameterSensitivityCalculator = parameterSensitivityCalculator;
    _data = data;
  }

  @Override
  public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
    final InflationIssuerProviderDiscount bundle = _data.getKnownData().copy();
    final InflationIssuerProviderDiscount newCurves = _data.getGeneratorMarket().evaluate(x);
    bundle.setAll(newCurves);
    final Set<String> curvesSet = _data.getGeneratorMarket().getInflationCurvesList();
    final int nbParameters = _data.getNumberOfInstruments();
    final double[][] res = new double[nbParameters][nbParameters];
    for (int loopinstrument = 0; loopinstrument < _data.getNumberOfInstruments(); loopinstrument++) {
      final InstrumentDerivative deriv = _data.getInstrument(loopinstrument);
      res[loopinstrument] = _parameterSensitivityCalculator.calculateSensitivity(deriv, bundle, curvesSet).getData();
    }
    return new DoubleMatrix2D(res);
  }

}
