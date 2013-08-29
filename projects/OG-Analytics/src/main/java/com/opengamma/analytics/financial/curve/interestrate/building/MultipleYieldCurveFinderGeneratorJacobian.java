/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.building;

import com.opengamma.analytics.financial.curve.interestrate.sensitivity.AbstractParameterSensitivityCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;

/**
 * Function computing the Jacobian of the error of valuation produce by a array representing the curve parameters.
 * The meaning of value is given by a calculator (usually present value or par spread).
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated. Use classes such as
 * {@link MulticurveDiscountBuildingRepository}.
 */
@Deprecated
public class MultipleYieldCurveFinderGeneratorJacobian extends Function1D<DoubleMatrix1D, DoubleMatrix2D> {

  /**
   * The instrument parameter sensitivity calculator.
   */
  private final AbstractParameterSensitivityCalculator _parameterSensitivityCalculator;
  /**
   * The data required for curve building.
   */
  private final MultipleYieldCurveFinderGeneratorDataBundle _data;

  /**
   * Constructor.
   * @param parameterSensitivityCalculator The instrument parameter sensitivity calculator.
   * @param data The data required for curve building.
   */
  public MultipleYieldCurveFinderGeneratorJacobian(final AbstractParameterSensitivityCalculator parameterSensitivityCalculator, final MultipleYieldCurveFinderGeneratorDataBundle data) {
    _parameterSensitivityCalculator = parameterSensitivityCalculator;
    _data = data;
  }

  @Override
  public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {
    final YieldCurveBundle bundle = _data.getKnownData().copy();
    final YieldCurveBundle newCurves = _data.getBuildingFunction().evaluate(x);
    bundle.addAll(newCurves);
    final int nbParameters = _data.getNumberOfInstruments();
    final double[][] res = new double[nbParameters][nbParameters];
    for (int loopinstrument = 0; loopinstrument < _data.getNumberOfInstruments(); loopinstrument++) {
      final InstrumentDerivative deriv = _data.getInstrument(loopinstrument);
      res[loopinstrument] = _parameterSensitivityCalculator.calculateSensitivity(deriv, _data.getKnownData().getAllNames(), bundle).getData();
    }
    return new DoubleMatrix2D(res);
  }

}
