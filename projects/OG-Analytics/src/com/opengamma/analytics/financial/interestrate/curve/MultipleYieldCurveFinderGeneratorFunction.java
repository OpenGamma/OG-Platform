/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.curve;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Function computing the error of valuation produce by a array representing the curve parameters. 
 * The meaning of value is given by a calculator (usually present value or par spread).
 */
public class MultipleYieldCurveFinderGeneratorFunction extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

  /**
   * The instrument value calculator.
   */
  private final InstrumentDerivativeVisitor<YieldCurveBundle, Double> _calculator;
  /**
   * The data required for curve building.
   */
  private final MultipleYieldCurveFinderGeneratorDataBundle _data;

  /**
   * Constructor. 
   * @param calculator The instrument value calculator.
   * @param data The data required for curve building.
   */
  public MultipleYieldCurveFinderGeneratorFunction(InstrumentDerivativeVisitor<YieldCurveBundle, Double> calculator, MultipleYieldCurveFinderGeneratorDataBundle data) {
    ArgumentChecker.notNull(calculator, "Calculator");
    ArgumentChecker.notNull(data, "Data");
    _calculator = calculator;
    _data = data;
  }

  @Override
  public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
    final YieldCurveBundle bundle = _data.getKnownData().copy();
    YieldCurveBundle newCurves = _data.getBuildingFunction().evaluate(x);
    bundle.addAll(newCurves);
    final double[] res = new double[_data.getNumberOfInstruments()];
    for (int i = 0; i < _data.getNumberOfInstruments(); i++) {
      res[i] = _calculator.visit(_data.getInstrument(i), bundle);
    }
    return new DoubleMatrix1D(res);
  }

}
