/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.financial.provider.description.HullWhiteOneFactorProviderInterface;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Function computing the error of valuation produce by an array representing the curve parameters. 
 * @author marc
 */
public class HullWhiteProviderDiscountFinderFunction extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

  /**
   * The instrument value calculator.
   */
  private final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, Double> _calculator;
  /**
   * The data required for curve building.
   */
  private final HullWhiteProviderDiscountBuildingData _data;

  /**
   * Constructor. 
   * @param calculator The instrument value calculator.
   * @param data The data required for curve building.
   */
  public HullWhiteProviderDiscountFinderFunction(final InstrumentDerivativeVisitor<HullWhiteOneFactorProviderInterface, Double> calculator, HullWhiteProviderDiscountBuildingData data) {
    ArgumentChecker.notNull(calculator, "Calculator");
    ArgumentChecker.notNull(data, "Data");
    _calculator = calculator;
    _data = data;
  }

  @Override
  public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
    final HullWhiteOneFactorProviderDiscount bundle = _data.getKnownData().copy();
    HullWhiteOneFactorProviderDiscount newCurves = _data.getGeneratorMarket().evaluate(x);
    bundle.setAll(newCurves);
    final double[] res = new double[_data.getNumberOfInstruments()];
    for (int i = 0; i < _data.getNumberOfInstruments(); i++) {
      res[i] = _calculator.visit(_data.getInstrument(i), bundle);
    }
    return new DoubleMatrix1D(res);
  }

}
