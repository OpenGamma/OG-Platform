/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.calculator.discounting;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorSameMethodAdapter;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.util.amount.ReferenceAmount;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;

/**
 * Returns the change in present value of an instrument due to a parallel move of all the curve's parameters, scaled so that the move is one basis point (0.0001).
 */
public final class PV01CurveParametersCalculator extends InstrumentDerivativeVisitorSameMethodAdapter<MulticurveProviderInterface, ReferenceAmount<Pair<String, Currency>>> {

  /**
  * The unique instance of the sensitivity calculator.
  */
  private static final PV01CurveParametersCalculator INSTANCE = new PV01CurveParametersCalculator();

  /**
   * Returns the instance of the calculator.
   * @return The instance.
   */
  public static PV01CurveParametersCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * The size of the scaling: 1 basis point. 
   */
  private static final double BP1 = 1.0E-4;
  /**
   * The present value curve sensitivity calculator.
   */
  private final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> _parameterSensitivityCalculator;

  /**
   * Private standard constructor. Using the standard curve sensitivity calculator: PresentValueCurveSensitivityCalculator.
   */
  private PV01CurveParametersCalculator() {
    _parameterSensitivityCalculator = new ParameterSensitivityParameterCalculator<MulticurveProviderInterface>(PresentValueCurveSensitivityDiscountingCalculator.getInstance());
  }

  /**
   * Calculates the change in present value of an instrument due to a parallel move of each yield curve the instrument is sensitive to, scaled so that the move is 1bp.
   * @param ird The instrument. 
   * @param multicurves The multi-curves provider.
   * @return The scale sensitivity for each curve/currency.
   */
  @Override
  public ReferenceAmount<Pair<String, Currency>> visit(final InstrumentDerivative ird, final MulticurveProviderInterface multicurves) {
    final MultipleCurrencyParameterSensitivity sensi = _parameterSensitivityCalculator.calculateSensitivity(ird, multicurves, multicurves.getAllNames());
    final ReferenceAmount<Pair<String, Currency>> ref = new ReferenceAmount<Pair<String, Currency>>();
    for (Pair<String, Currency> nameCcy : sensi.getAllNamesCurrency()) {
      DoubleMatrix1D vector = sensi.getSensitivity(nameCcy);
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
