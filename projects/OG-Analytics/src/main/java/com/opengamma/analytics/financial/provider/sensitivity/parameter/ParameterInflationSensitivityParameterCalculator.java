/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.analytics.financial.provider.sensitivity.parameter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.inflation.ParameterInflationProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pairs;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The return format is ParameterSensitivity object.
 * @param <DATA_TYPE> Data type.
 */
public class ParameterInflationSensitivityParameterCalculator<DATA_TYPE extends ParameterInflationProviderInterface> extends AbstractParameterInflationSensitivityParameterCalculator<DATA_TYPE> {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterInflationSensitivityParameterCalculator(final InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyInflationSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  @Override
  public MultipleCurrencyParameterSensitivity pointToParameterSensitivity(final MultipleCurrencyInflationSensitivity sensitivity, final DATA_TYPE parameterMulticurves, final Set<String> curvesSet) {
    ArgumentChecker.notNull(sensitivity, "sensitivity");
    ArgumentChecker.notNull(parameterMulticurves, "parameterMulticurves");
    ArgumentChecker.notNull(curvesSet, "curvesSet");
    MultipleCurrencyParameterSensitivity result = new MultipleCurrencyParameterSensitivity();

    // YieldAndDiscount
    for (final Currency ccySensi : sensitivity.getCurrencies()) {
      final Map<String, List<DoublesPair>> sensitivityDsc = sensitivity.getSensitivity(ccySensi).getYieldDiscountingSensitivities();
      for (final Map.Entry<String, List<DoublesPair>> entry : sensitivityDsc.entrySet()) {
        if (curvesSet.contains(entry.getKey())) {
          result = result
              .plus(Pairs.of(entry.getKey(), ccySensi), new DoubleMatrix1D(parameterMulticurves.parameterSensitivity(entry.getKey(), entry.getValue())));
        }
      }
    }
    // Forward
    for (final Currency ccySensi : sensitivity.getCurrencies()) {
      final Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getSensitivity(ccySensi).getForwardSensitivities();
      for (final Map.Entry<String, List<ForwardSensitivity>> entry : sensitivityFwd.entrySet()) {
        if (curvesSet.contains(entry.getKey())) {
          result = result.plus(Pairs.of(entry.getKey(), ccySensi),
              new DoubleMatrix1D(parameterMulticurves.parameterForwardSensitivity(entry.getKey(), entry.getValue())));
        }
      }
    }

    // IndexPrice
    for (final Currency ccySensi : sensitivity.getCurrencies()) {
      final Map<String, List<DoublesPair>> sensitivityPriceIndex = sensitivity.getSensitivity(ccySensi).getPriceCurveSensitivities();
      for (final Map.Entry<String, List<DoublesPair>> entry : sensitivityPriceIndex.entrySet()) {
        if (curvesSet.contains(entry.getKey())) {
          result = result.plus(Pairs.of(entry.getKey(), ccySensi),
              new DoubleMatrix1D(parameterMulticurves.parameterInflationSensitivity(entry.getKey(), entry.getValue())));
        }
      }
    }
    return result;
  }

  @Override
  public MultipleCurrencyParameterSensitivity pointToParameterSensitivity(final MultipleCurrencyInflationSensitivity sensitivity, final DATA_TYPE parameterMulticurves) {
    ArgumentChecker.notNull(sensitivity, "sensitivity");
    ArgumentChecker.notNull(parameterMulticurves, "parameterMulticurves");
    MultipleCurrencyParameterSensitivity result = new MultipleCurrencyParameterSensitivity();

    // YieldAndDiscount
    for (final Currency ccySensi : sensitivity.getCurrencies()) {
      final Map<String, List<DoublesPair>> sensitivityDsc = sensitivity.getSensitivity(ccySensi).getYieldDiscountingSensitivities();
      for (final Map.Entry<String, List<DoublesPair>> entry : sensitivityDsc.entrySet()) {
        result = result
            .plus(Pairs.of(entry.getKey(), ccySensi), new DoubleMatrix1D(parameterMulticurves.getMulticurveProvider().parameterSensitivity(entry.getKey(), entry.getValue())));
      }
    }
    // Forward
    for (final Currency ccySensi : sensitivity.getCurrencies()) {
      final Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getSensitivity(ccySensi).getForwardSensitivities();
      for (final Map.Entry<String, List<ForwardSensitivity>> entry : sensitivityFwd.entrySet()) {
        result = result.plus(Pairs.of(entry.getKey(), ccySensi),
            new DoubleMatrix1D(parameterMulticurves.getMulticurveProvider().parameterForwardSensitivity(entry.getKey(), entry.getValue())));
      }
    }

    // IndexPrice
    for (final Currency ccySensi : sensitivity.getCurrencies()) {
      final Map<String, List<DoublesPair>> sensitivityPriceIndex = sensitivity.getSensitivity(ccySensi).getPriceCurveSensitivities();
      for (final Map.Entry<String, List<DoublesPair>> entry : sensitivityPriceIndex.entrySet()) {
        result = result.plus(Pairs.of(entry.getKey(), ccySensi),
            new DoubleMatrix1D(parameterMulticurves.getInflationProvider().parameterInflationSensitivity(entry.getKey(), entry.getValue())));
      }
    }
    return result;
  }
}
