/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderForward;
import com.opengamma.analytics.financial.provider.sensitivity.AbstractParameterSensitivityMatrixProviderCalculator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Function computing the Jacobian of the error of valuation produce by a array representing the curve parameters. 
 * @author marc
 */
public class ProviderForwardFinderJacobian extends Function1D<DoubleMatrix1D, DoubleMatrix2D> {

  /**
   * The instrument parameter sensitivity calculator.
   */
  private final AbstractParameterSensitivityMatrixProviderCalculator _parameterSensitivityCalculator;
  /**
   * The data required for curve building.
   */
  private final ProviderForwardBuildingData _data;

  /**
   * Constructor.
   * @param parameterSensitivityCalculator The instrument parameter sensitivity calculator.
   * @param data The data required for curve building.
   */
  public ProviderForwardFinderJacobian(final AbstractParameterSensitivityMatrixProviderCalculator parameterSensitivityCalculator, final ProviderForwardBuildingData data) {
    _parameterSensitivityCalculator = parameterSensitivityCalculator;
    _data = data;
  }

  @Override
  public DoubleMatrix2D evaluate(DoubleMatrix1D x) {
    final MulticurveProviderForward bundle = _data.getKnownData().copy();
    MulticurveProviderForward newCurves = _data.getGeneratorMarket().evaluate(x);
    bundle.setAll(newCurves);
    final int nbParameters = _data.getNumberOfInstruments();
    final double[][] res = new double[nbParameters][nbParameters];
    for (int loopinstrument = 0; loopinstrument < _data.getNumberOfInstruments(); loopinstrument++) {
      InstrumentDerivative deriv = _data.getInstrument(loopinstrument);
      res[loopinstrument] = _parameterSensitivityCalculator.calculateSensitivity(deriv, _data.getKnownData().getAllNames(), bundle).getData();
    }
    return new DoubleMatrix2D(res);
  }

}
