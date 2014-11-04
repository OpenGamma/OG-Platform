/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderDiscount;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Function computing the error of valuation produce by an array representing the curve parameters.
 */
public class InflationDiscountFinderFunction extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

  /**
  * The instrument value calculator.
  */
  private final InstrumentDerivativeVisitor<ParameterInflationProviderInterface, Double> _calculator;

  /**
   * The data required for curve building.
   */
  private final InflationDiscountBuildingData _data;

  /**
   * Constructor
   * @param inflationCalculator The instrument value calculator.
   * @param data The data required for curve building.
   */
  public InflationDiscountFinderFunction(final InstrumentDerivativeVisitor<ParameterInflationProviderInterface, Double> inflationCalculator, final InflationDiscountBuildingData data) {
    ArgumentChecker.notNull(inflationCalculator, "Calculator");
    ArgumentChecker.notNull(data, "Data");
    _calculator = inflationCalculator;
    _data = data;
  }

  @Override
  public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
    final InflationProviderDiscount inflationBundle = _data.getKnownData().copy();
    final InflationProviderDiscount inflationnewCurves = _data.getGeneratorMarket().evaluate(x);
    inflationBundle.setAll(inflationnewCurves);
    final double[] res = new double[_data.getNumberOfInstruments()];
    for (int i = 0; i < _data.getNumberOfInstruments(); i++) {
      res[i] = _data.getInstrument(i).accept(_calculator, inflationBundle);
    }
    return new DoubleMatrix1D(res);
  }

}
