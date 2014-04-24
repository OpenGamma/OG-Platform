/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.inflationissuer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class ParameterSensitivityInflationIssuerMatrixCalculator extends ParameterSensitivityInflationIssuerMatrixProviderAbstractCalculator {

  /**
   * Constructor
   * @param inflationSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterSensitivityInflationIssuerMatrixCalculator(final InstrumentDerivativeVisitor<InflationIssuerProviderInterface, InflationSensitivity> inflationSensitivityCalculator) {
    super(inflationSensitivityCalculator);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities.
   * @param sensitivity The point sensitivity.
   * @param inflation The inflation provider. Not null.
   * @param curvesSet The set of curves for which the sensitivity will be computed. Not null.
   * @return The sensitivity (as a Matrix). The order of the sensitivity is by curve as provided by the curvesSet.
   */
  @Override
  public DoubleMatrix1D pointToParameterSensitivity(final InflationSensitivity sensitivity, final InflationIssuerProviderInterface inflation, final Set<String> curvesSet) {
    SimpleParameterSensitivity ps = new SimpleParameterSensitivity();

    // inflation curve
    final Map<String, List<DoublesPair>> sensitivityPriceCurve = sensitivity.getPriceCurveSensitivities();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensitivityPriceCurve.entrySet()) {
      if (curvesSet.contains(entry.getKey())) {
        ps = ps.plus(entry.getKey(), new DoubleMatrix1D(inflation.parameterInflationSensitivity(entry.getKey(), entry.getValue())));
      }
    }

    // YieldAndDiscount
    final Map<String, List<DoublesPair>> sensitivityDsc = sensitivity.getYieldDiscountingSensitivities();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensitivityDsc.entrySet()) {
      if (curvesSet.contains(entry.getKey())) {
        ps = ps.plus(entry.getKey(), new DoubleMatrix1D(inflation.getMulticurveProvider().parameterSensitivity(entry.getKey(), entry.getValue())));
      }
    }
    // Forward
    final Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getForwardSensitivities();
    for (final Map.Entry<String, List<ForwardSensitivity>> entry : sensitivityFwd.entrySet()) {
      if (curvesSet.contains(entry.getKey())) {
        ps = ps.plus(entry.getKey(), new DoubleMatrix1D(inflation.getMulticurveProvider().parameterForwardSensitivity(entry.getKey(), entry.getValue())));
      }
    }

    // By curve name in the curves set (to have the right order)
    double[] result = new double[0];
    for (final String name : curvesSet) {
      final DoubleMatrix1D sensi = ps.getSensitivity(name);
      if (sensi != null) {
        result = ArrayUtils.addAll(result, sensi.getData());
      } else {
        result = ArrayUtils.addAll(result, new double[inflation.getNumberOfParameters(name)]);
      }
    }
    return new DoubleMatrix1D(result);
  }
}
