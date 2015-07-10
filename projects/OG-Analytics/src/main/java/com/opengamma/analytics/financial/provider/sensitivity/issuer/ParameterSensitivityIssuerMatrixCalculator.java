/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.issuer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ForwardSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For an instrument, computes the sensitivity of double value (often par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The return format is DoubleMatrix1D object.
 */
public class ParameterSensitivityIssuerMatrixCalculator extends AbstractParameterSensitivityIssuerMatrixCalculator {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterSensitivityIssuerMatrixCalculator(final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MulticurveSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  @Override
  public DoubleMatrix1D pointToParameterSensitivity(final MulticurveSensitivity sensitivity, final ParameterIssuerProviderInterface issuer, final Set<String> curvesSet) {
    SimpleParameterSensitivity ps = new SimpleParameterSensitivity();
    // YieldAndDiscount
    final Map<String, List<DoublesPair>> sensitivityDsc = sensitivity.getYieldDiscountingSensitivities();
    for (final Map.Entry<String, List<DoublesPair>> entry : sensitivityDsc.entrySet()) {
      if (curvesSet.contains(entry.getKey())) {
        ps = ps.plus(entry.getKey(), new DoubleMatrix1D(issuer.getIssuerProvider().parameterSensitivity(entry.getKey(), entry.getValue())));
      }
    }
    // Forward
    final Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getForwardSensitivities();
    for (final Map.Entry<String, List<ForwardSensitivity>> entry : sensitivityFwd.entrySet()) {
      if (curvesSet.contains(entry.getKey())) {
        ps = ps.plus(entry.getKey(), new DoubleMatrix1D(issuer.getIssuerProvider().parameterForwardSensitivity(entry.getKey(), entry.getValue())));
      }
    }
    // By curve name in the curves set (to have the right order)
    double[] result = new double[0];
    for (final String name : curvesSet) {
      final DoubleMatrix1D sensi = ps.getSensitivity(name);
      if (sensi != null) {
        result = ArrayUtils.addAll(result, sensi.getData());
      } else {
        result = ArrayUtils.addAll(result, new double[issuer.getIssuerProvider().getNumberOfParameters(name)]);
      }
    }
    return new DoubleMatrix1D(result);
  }

}
