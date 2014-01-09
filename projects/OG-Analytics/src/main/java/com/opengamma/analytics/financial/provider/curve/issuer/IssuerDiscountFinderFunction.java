/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.issuer;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Function computing the error of valuation produce by an array representing the curve parameters.
 */
public class IssuerDiscountFinderFunction extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

  /**
   * The instrument value calculator.
   */
  private final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, Double> _calculator;
  /**
   * The data required for curve building.
   */
  private final IssuerDiscountBuildingData _data;

  /**
   * Constructor.
   * @param calculator The instrument value calculator.
   * @param data The data required for curve building.
   */
  public IssuerDiscountFinderFunction(final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, Double> calculator, final IssuerDiscountBuildingData data) {
    ArgumentChecker.notNull(calculator, "Calculator");
    ArgumentChecker.notNull(data, "Data");
    _calculator = calculator;
    _data = data;
  }

  @Override
  public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
    final IssuerProviderDiscount provider = _data.getKnownData().copy();
    final IssuerProviderDiscount newCurves = _data.getGeneratorMarket().evaluate(x);
    provider.setAll(newCurves);
    final double[] res = new double[_data.getNumberOfInstruments()];
    for (int i = 0; i < _data.getNumberOfInstruments(); i++) {
      res[i] = _data.getInstrument(i).accept(_calculator, provider);
    }
    return new DoubleMatrix1D(res);
  }

}
