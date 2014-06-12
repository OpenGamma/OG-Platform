/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.calculator;

import java.util.LinkedHashMap;
import java.util.Set;

import com.opengamma.analytics.financial.curve.interestrate.sensitivity.AbstractParameterSensitivityBlockCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

/**
 * Calculator of the sensitivity to the market quotes of instruments used to build the curves.
 * The sensitivities are computed as a block (vector) for each curve/currency pair.
 * The sensitivity to a particular curve can be in several currencies.
 */
@SuppressWarnings("deprecation")
public final class MarketQuoteSensitivityBlockCalculator {

  /**
   * The matrix algebra used for matrix inversion.
   */
  private static final MatrixAlgebra MATRIX_ALGEBRA = new OGMatrixAlgebra(); //TODO make this a parameter
  /**
   * The parameter sensitivity calculator. The parameters are the parameters used to described the curve.
   */
  private final AbstractParameterSensitivityBlockCalculator _parameterSensitivityCalculator;

  /**
   * The constructor.
   * @param parameterSensitivityCalculator The parameter sensitivity calculator.
   */
  public MarketQuoteSensitivityBlockCalculator(final AbstractParameterSensitivityBlockCalculator parameterSensitivityCalculator) {
    _parameterSensitivityCalculator = parameterSensitivityCalculator;
  }

  /**
   * Compute the market quote sensitivity from a parameter sensitivity.
   * @param parameterSensitivity The parameter sensitivity.
   * @param units The curve building units data.
   * @return The market quote sensitivity.
   */
  public MultipleCurrencyParameterSensitivity fromParameterSensitivity(final MultipleCurrencyParameterSensitivity parameterSensitivity, final CurveBuildingBlockBundle units) {
    ArgumentChecker.notNull(parameterSensitivity, "Sensitivity");
    ArgumentChecker.notNull(units, "Units");
    MultipleCurrencyParameterSensitivity result = new MultipleCurrencyParameterSensitivity();
    for (final Pair<String, Currency> nameCcy : parameterSensitivity.getAllNamesCurrency()) {
      final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> oneCurveSensiMap = new LinkedHashMap<>();
      final Pair<CurveBuildingBlock, DoubleMatrix2D> unitPair = units.getBlock(nameCcy.getFirst());
      final double[] oneCurveSensiArray = ((DoubleMatrix1D) MATRIX_ALGEBRA.multiply(parameterSensitivity.getSensitivity(nameCcy), unitPair.getSecond())).getData();
      for (final String name2 : unitPair.getFirst().getAllNames()) {
        final int nbParameters = unitPair.getFirst().getNbParameters(name2);
        final int start = unitPair.getFirst().getStart(name2);
        final double[] sensiName2 = new double[nbParameters];
        System.arraycopy(oneCurveSensiArray, start, sensiName2, 0, nbParameters);
        oneCurveSensiMap.put(ObjectsPair.of(name2, nameCcy.getSecond()), new DoubleMatrix1D(sensiName2));
      }
      final MultipleCurrencyParameterSensitivity sensiName = new MultipleCurrencyParameterSensitivity(oneCurveSensiMap);
      result = result.plus(sensiName);
    }
    return result;
  }

  /**
   * Compute the market quote sensitivity from an instrument.
   * @param instrument The instrument. Not null.
   * @param fixedCurves The fixed curves names (for which the parameter sensitivity are not computed even if they are necessary for the instrument pricing).
   * The curve in the list may or may not be in the bundle. Not null.
   * @param bundle The curve bundle with all the curves with respect to which the sensitivity should be computed. Not null.
   * @param units The curve building units data.
   * @return The market quote sensitivity.
   * @deprecated {@link YieldCurveBundle} is deprecated
   */
  @Deprecated
  public MultipleCurrencyParameterSensitivity fromInstrument(final InstrumentDerivative instrument, final Set<String> fixedCurves, final YieldCurveBundle bundle,
      final CurveBuildingBlockBundle units) {
    final MultipleCurrencyParameterSensitivity parameterSensitivity = _parameterSensitivityCalculator.calculateSensitivity(instrument, fixedCurves, bundle);
    return fromParameterSensitivity(parameterSensitivity, units);
  }

}
