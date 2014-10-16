/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.issuer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pairs;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The return format is ParameterSensitivity object.
 * @param <DATA_TYPE> Data type.
 */
public class ParameterSensitivityIssuerCalculator<DATA_TYPE extends ParameterIssuerProviderInterface> 
  extends AbstractParameterSensitivityIssuerCalculator<DATA_TYPE> {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterSensitivityIssuerCalculator(final InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyMulticurveSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities to the continuously compounded rate and to the forward rates
   * for the supplied curve names.
   * @param sensitivity The point sensitivity.
   * @param multicurves The multi-curve provider. Not null.
   * @param curvesSet The set of curves for which the sensitivity will be computed. Not null.
   * @return The sensitivity (as a ParameterSensitivity).
   */
  @Override
  public MultipleCurrencyParameterSensitivity pointToParameterSensitivity(final MultipleCurrencyMulticurveSensitivity sensitivity, final DATA_TYPE multicurves,
      final Set<String> curvesSet) {
    MultipleCurrencyParameterSensitivity result = new MultipleCurrencyParameterSensitivity();
    // YieldAndDiscount
    for (final Currency ccySensi : sensitivity.getCurrencies()) {
      final Map<String, List<DoublesPair>> sensitivityDsc = sensitivity.getSensitivity(ccySensi).getYieldDiscountingSensitivities();
      for (final Map.Entry<String, List<DoublesPair>> entry : sensitivityDsc.entrySet()) {
        if (curvesSet.contains(entry.getKey())) {
          result = result.plus(Pairs.of(entry.getKey(), ccySensi), new DoubleMatrix1D(multicurves.getIssuerProvider().parameterSensitivity(entry.getKey(), entry.getValue())));
        }
      }
    }
    // Forward
    for (final Currency ccySensi : sensitivity.getCurrencies()) {
      final Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getSensitivity(ccySensi).getForwardSensitivities();
      for (final Map.Entry<String, List<ForwardSensitivity>> entry : sensitivityFwd.entrySet()) {
        if (curvesSet.contains(entry.getKey())) {
          result = result.plus(Pairs.of(entry.getKey(), ccySensi), new DoubleMatrix1D(multicurves.getIssuerProvider().parameterForwardSensitivity(entry.getKey(), entry.getValue())));
        }
      }
    }
    return result;
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities to the continuously compounded rate and to the forward rates
   * for all curves.
   * @param sensitivity The point sensitivity.
   * @param multicurves The multi-curve provider. Not null.
   * @return The sensitivity (as a ParameterSensitivity).
   */
  @Override
  public MultipleCurrencyParameterSensitivity pointToParameterSensitivity(final MultipleCurrencyMulticurveSensitivity sensitivity, final DATA_TYPE multicurves) {
    MultipleCurrencyParameterSensitivity result = new MultipleCurrencyParameterSensitivity();
    // YieldAndDiscount
    for (final Currency ccySensi : sensitivity.getCurrencies()) {
      final Map<String, List<DoublesPair>> sensitivityDsc = sensitivity.getSensitivity(ccySensi).getYieldDiscountingSensitivities();
      for (final Map.Entry<String, List<DoublesPair>> entry : sensitivityDsc.entrySet()) {
        result = result.plus(Pairs.of(entry.getKey(), ccySensi), new DoubleMatrix1D(multicurves.getIssuerProvider().parameterSensitivity(entry.getKey(), entry.getValue())));
      }
    }
    // Forward
    for (final Currency ccySensi : sensitivity.getCurrencies()) {
      final Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getSensitivity(ccySensi).getForwardSensitivities();
      for (final Map.Entry<String, List<ForwardSensitivity>> entry : sensitivityFwd.entrySet()) {
        result = result.plus(Pairs.of(entry.getKey(), ccySensi), new DoubleMatrix1D(multicurves.getIssuerProvider().parameterForwardSensitivity(entry.getKey(), entry.getValue())));
      }
    }
    return result;
  }
}
