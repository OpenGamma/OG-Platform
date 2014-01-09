/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.generic;

import java.util.LinkedHashMap;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.AbstractParameterSensitivityParameterCalculator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Calculator of the sensitivity to the market quotes of instruments used to build the curves.
 * The sensitivities are computed as a block (vector) for each curve/currency pair.
 * The sensitivity to a particular curve can be in several currencies.
 * @param <DATA_TYPE> Data type.
 */
public final class MarketQuoteSensitivityBlockCalculator<DATA_TYPE extends ParameterProviderInterface> {

  /**
   * The matrix algebra used for matrix inversion.
   */
  private static final MatrixAlgebra MATRIX_ALGEBRA = new OGMatrixAlgebra(); //TODO make this a parameter
  /**
   * The parameter sensitivity calculator. The parameters are the parameters used to described the curve.
   */
  private final AbstractParameterSensitivityParameterCalculator<DATA_TYPE> _parameterSensitivityCalculator;

  /**
   * The constructor.
   * @param parameterSensitivityCalculator The parameter sensitivity calculator.
   */
  public MarketQuoteSensitivityBlockCalculator(final AbstractParameterSensitivityParameterCalculator<DATA_TYPE> parameterSensitivityCalculator) {
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
      ArgumentChecker.notNull(parameterSensitivity.getSensitivity(nameCcy), "sensitivity for " + nameCcy);
      ArgumentChecker.notNull(unitPair, "curve building block / Jacobian pair for " + nameCcy.getFirst());
      ArgumentChecker.notNull(unitPair.getSecond(), "Jacobian");
      final DoubleMatrix1D matrix = (DoubleMatrix1D) MATRIX_ALGEBRA.multiply(parameterSensitivity.getSensitivity(nameCcy), unitPair.getSecond());
      if (matrix != null) {
        final double[] oneCurveSensiArray = ((DoubleMatrix1D) MATRIX_ALGEBRA.multiply(parameterSensitivity.getSensitivity(nameCcy), unitPair.getSecond())).getData();
        for (final String name2 : unitPair.getFirst().getAllNames()) {
          final int nbParameters = unitPair.getFirst().getNbParameters(name2);
          final int start = unitPair.getFirst().getStart(name2);
          final double[] sensiName2 = new double[nbParameters];
          System.arraycopy(oneCurveSensiArray, start, sensiName2, 0, nbParameters);
          oneCurveSensiMap.put(Pairs.of(name2, nameCcy.getSecond()), new DoubleMatrix1D(sensiName2));
        }
        final MultipleCurrencyParameterSensitivity sensiName = new MultipleCurrencyParameterSensitivity(oneCurveSensiMap);
        result = result.plus(sensiName);
      }
    }
    return result;
  }

  /**
   * Compute the market quote sensitivity from an instrument.
   * @param instrument The instrument. Not null.
   * @param provider The provider
   * @param units The curve building units data.
   * @return The market quote sensitivity.
   */
  public MultipleCurrencyParameterSensitivity fromInstrument(final InstrumentDerivative instrument, final DATA_TYPE provider, final CurveBuildingBlockBundle units) {
    ArgumentChecker.notNull(instrument, "instrument");
    ArgumentChecker.notNull(provider, "provider");
    ArgumentChecker.notNull(units, "units");
    final MultipleCurrencyParameterSensitivity parameterSensitivity = _parameterSensitivityCalculator.calculateSensitivity(instrument, provider);
    return fromParameterSensitivity(parameterSensitivity, units);
  }

}
