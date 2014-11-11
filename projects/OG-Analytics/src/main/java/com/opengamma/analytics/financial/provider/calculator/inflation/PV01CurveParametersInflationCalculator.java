/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.inflation;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.ParameterSensitivityInflationParameterCalculator;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Returns the change in present value of an instrument due to a parallel move of all the curve's parameters, 
 * scaled so that the move is one basis point (0.0001).
 * @param <T> The type of the multi-curve provider
 */
public final class PV01CurveParametersInflationCalculator<T extends ParameterInflationProviderInterface> extends
    InstrumentDerivativeVisitorSameMethodAdapter<T, ReferenceAmount<Pair<String, Currency>>> {

  /** The size of the scaling: 1 basis point. */
  private static final double BP1 = 1.0E-4;
  /** The present value curve sensitivity calculator. */
  private final ParameterSensitivityInflationParameterCalculator<T> _parameterSensitivityCalculator;

  /**
   * Constructs a PV01 calculator that uses a particular sensitivity calculator.
   * @param curveSensitivityCalculator The curve sensitivity calculator, not null
   */
  public PV01CurveParametersInflationCalculator(final InstrumentDerivativeVisitor<T, MultipleCurrencyInflationSensitivity> curveSensitivityCalculator) {
    ArgumentChecker.notNull(curveSensitivityCalculator, "curve sensitivity calculator");
    _parameterSensitivityCalculator = new ParameterSensitivityInflationParameterCalculator<>(curveSensitivityCalculator);
  }

  /**
   * Calculates the change in present value of an instrument due to a parallel move of each yield curve the instrument is sensitive to, scaled so that the move is 1bp.
   * @param ird The instrument.
   * @param multicurves The multi-curves provider.
   * @return The scale sensitivity for each curve/currency.
   */
  @Override
  public ReferenceAmount<Pair<String, Currency>> visit(final InstrumentDerivative ird, final T multicurves) {
    final MultipleCurrencyParameterSensitivity sensi = _parameterSensitivityCalculator.calculateSensitivity(ird, multicurves, multicurves.getAllCurveNames());
    final ReferenceAmount<Pair<String, Currency>> ref = new ReferenceAmount<>();
    for (final Pair<String, Currency> nameCcy : sensi.getAllNamesCurrency()) {
      final DoubleMatrix1D vector = sensi.getSensitivity(nameCcy);
      double total = 0.0;
      for (int loopv = 0; loopv < vector.getNumberOfElements(); loopv++) {
        total += vector.getEntry(loopv);
      }
      ref.add(nameCcy, total * BP1);
    }
    return ref;
  }

  @Override
  public ReferenceAmount<Pair<String, Currency>> visit(final InstrumentDerivative derivative) {
    throw new UnsupportedOperationException("Need curve data");
  }

}
