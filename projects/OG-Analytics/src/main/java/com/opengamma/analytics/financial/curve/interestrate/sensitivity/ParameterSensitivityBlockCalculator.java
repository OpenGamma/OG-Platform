/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.sensitivity;

import java.util.LinkedHashMap;
import java.util.Set;

import com.opengamma.analytics.financial.forex.method.MultipleCurrencyInterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * For an instrument, computes the sensitivity of a present value to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The return format is a ParameterSensitivity object, i.e. a map between Curve/Currency and the sensitivity to the parameters in the curve.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
 */
@Deprecated
public class ParameterSensitivityBlockCalculator extends AbstractParameterSensitivityBlockCalculator {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterSensitivityBlockCalculator(final InstrumentDerivativeVisitor<YieldCurveBundle, MultipleCurrencyInterestRateCurveSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities to the continuously compounded rate.
   * @param sensitivity The point sensitivity.
   * @param fixedCurves The fixed curves names (for which the parameter sensitivity are not computed even if they are necessary for the instrument pricing).
   * The curve in the list may or may not be in the bundle. Not null.
   * @param bundle The curve bundle with all the curves with respect to which the sensitivity should be computed. Not null.
   * @return The sensitivity.
   */
  @Override
  public MultipleCurrencyParameterSensitivity pointToParameterSensitivity(final MultipleCurrencyInterestRateCurveSensitivity sensitivity, final Set<String> fixedCurves,
      final YieldCurveBundle bundle) {
    ArgumentChecker.notNull(sensitivity, "Sensitivity");
    ArgumentChecker.notNull(fixedCurves, "Fixed Curves");
    ArgumentChecker.notNull(bundle, "Curve bundle");
    final LinkedHashMap<Pair<String, Currency>, DoubleMatrix1D> result = new LinkedHashMap<>();
    for (final Currency ccy : sensitivity.getCurrencies()) {
      for (final String curveName : sensitivity.getSensitivity(ccy).getSensitivities().keySet()) {
        if (!fixedCurves.contains(curveName)) {
          final YieldAndDiscountCurve curve = bundle.getCurve(curveName);
          final Double[] oneCurveSensitivity = pointToParameterSensitivity(sensitivity.getSensitivity(ccy).getSensitivities().get(curveName), curve);
          result.put(Pairs.of(curveName, ccy), new DoubleMatrix1D(oneCurveSensitivity));
        }
      }
    }
    return new MultipleCurrencyParameterSensitivity(result);
  }

}
