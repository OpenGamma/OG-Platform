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
import com.opengamma.analytics.financial.provider.description.inflation.InflationProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.MultipleCurrencyInflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The return format is ParameterSensitivity object.
 * @param <DATA_TYPE> Data type.
 */
public class ParameterInflationSensitivityParameterCalculator<DATA_TYPE extends ParameterProviderInterface> extends ParameterInflationSensitivityParameterAbstractCalculator<DATA_TYPE> {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterInflationSensitivityParameterCalculator(final InstrumentDerivativeVisitor<DATA_TYPE, MultipleCurrencyInflationSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  @Override
  public MultipleCurrencyParameterSensitivity pointToParameterSensitivity(final MultipleCurrencyInflationSensitivity sensitivity, final DATA_TYPE parameterMulticurves, final Set<String> curvesSet) {
    MultipleCurrencyParameterSensitivity result = new MultipleCurrencyParameterSensitivity();

    // YieldAndDiscount
    for (Currency ccySensi : sensitivity.getCurrencies()) {
      Map<String, List<DoublesPair>> sensitivityDsc = sensitivity.getSensitivity(ccySensi).getYieldDiscountingSensitivities();
      for (final String name : sensitivityDsc.keySet()) {
        if (curvesSet.contains(name)) {
          result = result
              .plus(new ObjectsPair<String, Currency>(name, ccySensi), new DoubleMatrix1D(parameterMulticurves.getMulticurveProvider().parameterSensitivity(name, sensitivityDsc.get(name))));
        }
      }
    }
    // Forward
    for (Currency ccySensi : sensitivity.getCurrencies()) {
      Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getSensitivity(ccySensi).getForwardSensitivities();
      for (final String name : sensitivityFwd.keySet()) {
        if (curvesSet.contains(name)) {
          result = result.plus(new ObjectsPair<String, Currency>(name, ccySensi),
              new DoubleMatrix1D(parameterMulticurves.getMulticurveProvider().parameterForwardSensitivity(name, sensitivityFwd.get(name))));
        }
      }
    }

    // IndexPrice
    for (Currency ccySensi : sensitivity.getCurrencies()) {
      Map<String, List<DoublesPair>> sensitivityPriceIndex = sensitivity.getSensitivity(ccySensi).getPriceCurveSensitivities();
      InflationProviderInterface inflationProviderInterface = (InflationProviderInterface) parameterMulticurves;
      for (final String name : sensitivityPriceIndex.keySet()) {
        if (curvesSet.contains(name)) {
          result = result.plus(new ObjectsPair<String, Currency>(name, ccySensi),
              new DoubleMatrix1D(inflationProviderInterface.parameterInflationSensitivity(name, sensitivityPriceIndex.get(name))));
        }
      }
    }
    return result;
  }

}
