/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Computes the sensitivity to the node in the curves.
 * @deprecated Use the calculators that reference {@link ParameterProviderInterface}
 */
@Deprecated
public class PresentValueNodeSensitivityCalculator extends NodeYieldSensitivityCalculator {

  private static final PresentValueNodeSensitivityCalculator DEFAULT_INSTANCE = new PresentValueNodeSensitivityCalculator();

  public static PresentValueNodeSensitivityCalculator getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  public static PresentValueNodeSensitivityCalculator using(final InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Map<String, List<DoublesPair>>> presentValueSensitivityCalculator) {
    Validate.notNull(presentValueSensitivityCalculator, "present value sensitivity calculator");
    return new PresentValueNodeSensitivityCalculator(presentValueSensitivityCalculator);
  }

  private final InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Map<String, List<DoublesPair>>> _presentValueSensitivityCalculator;

  public PresentValueNodeSensitivityCalculator() {
    _presentValueSensitivityCalculator = PresentValueCurveSensitivityCalculator.getInstance();
  }

  public PresentValueNodeSensitivityCalculator(final InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Map<String, List<DoublesPair>>> presentValueSensitivityCalculator) {
    Validate.notNull(presentValueSensitivityCalculator, "present value sensitivity calculator");
    _presentValueSensitivityCalculator = presentValueSensitivityCalculator;
  }

  @Override
  public DoubleMatrix1D calculateSensitivities(final InstrumentDerivative ird, final YieldCurveBundle fixedCurves, final YieldCurveBundle interpolatedCurves) {
    return calculateSensitivities(ird, _presentValueSensitivityCalculator, fixedCurves, interpolatedCurves);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _presentValueSensitivityCalculator.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final PresentValueNodeSensitivityCalculator other = (PresentValueNodeSensitivityCalculator) obj;
    return ObjectUtils.equals(_presentValueSensitivityCalculator, other._presentValueSensitivityCalculator);
  }
}
