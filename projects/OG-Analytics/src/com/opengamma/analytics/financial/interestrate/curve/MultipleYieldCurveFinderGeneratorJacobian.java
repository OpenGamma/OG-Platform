/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.curve;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Function computing the Jacobian of the error of valuation produce by a array representing the curve parameters. 
 * The meaning of value is given by a calculator (usually present value or par spread).
 */
public class MultipleYieldCurveFinderGeneratorJacobian extends Function1D<DoubleMatrix1D, DoubleMatrix2D> {

  private final AbstractParameterSensitivityCalculator _parameterSensitivityCalculator;
  private final MultipleYieldCurveFinderGeneratorDataBundle _data;

  public MultipleYieldCurveFinderGeneratorJacobian(final AbstractParameterSensitivityCalculator parameterSensitivityCalculator, MultipleYieldCurveFinderGeneratorDataBundle data) {
    _parameterSensitivityCalculator = parameterSensitivityCalculator;
    _data = data;
  }

  @Override
  public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
    final YieldCurveBundle bundle = _data.getKnownData().copy();
    YieldCurveBundle newCurves = _data.getBuildingFunction().evaluate(x);
    bundle.addAll(newCurves);
    final int nbParameters = _data.getNumberOfInstruments();
    final double[][] res = new double[nbParameters][nbParameters];
    for (int i = 0; i < _data.getNumberOfInstruments(); i++) { // loop over all instruments
      InstrumentDerivative deriv = _data.getInstrument(i);
      res[i] = _parameterSensitivityCalculator.calculateSensitivity(deriv, null, bundle).getData(); // REVIEW: dowe need to pass the knownData?
    }
    return new DoubleMatrix2D(res);
  }

}
