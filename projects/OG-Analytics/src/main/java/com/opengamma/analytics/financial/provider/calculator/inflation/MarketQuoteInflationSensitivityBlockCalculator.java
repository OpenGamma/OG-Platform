/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import java.util.LinkedHashMap;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.AbstractParameterInflationSensitivityParameterCalculator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.matrix.OGMatrixAlgebra;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *  Calculator of the sensitivity to the market quotes of instruments used to build the curves .
 * The sensitivities are computed as a block (vector) for each curve/currency pair.
 * The sensitivity to a particular curve can be in several currencies.
 * This class is a generalized (to inflation) of MarketQuoteSensitivityBlockCalculator.
 * @param <DATA_TYPE> Data type.
 */
public class MarketQuoteInflationSensitivityBlockCalculator<DATA_TYPE extends InflationProviderInterface> {

  /**
   * The matrix algebra used for matrix inversion.
   */
  private static final MatrixAlgebra MATRIX_ALGEBRA = new OGMatrixAlgebra(); //TODO make this a parameter
  /**
   * The parameter sensitivity calculator. The parameters are the parameters used to described the curve.
   */
  private final AbstractParameterInflationSensitivityParameterCalculator<DATA_TYPE> _parameterInflationSensitivityCalculator;

  /**
   * The constructor.
   * @param parameterInflationSensitivityCalculator The parameter sensitivity calculator.
   */
  public MarketQuoteInflationSensitivityBlockCalculator(final AbstractParameterInflationSensitivityParameterCalculator<DATA_TYPE> parameterInflationSensitivityCalculator) {
    _parameterInflationSensitivityCalculator = parameterInflationSensitivityCalculator;
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
        oneCurveSensiMap.put(Pairs.of(name2, nameCcy.getSecond()), new DoubleMatrix1D(sensiName2));
      }
      final MultipleCurrencyParameterSensitivity sensiName = new MultipleCurrencyParameterSensitivity(oneCurveSensiMap);
      result = result.plus(sensiName);
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
    final MultipleCurrencyParameterSensitivity parameterSensitivity = _parameterInflationSensitivityCalculator.calculateSensitivity(instrument, provider,
        provider.getAllNames());
    return fromParameterSensitivity(parameterSensitivity, units);
  }
}
