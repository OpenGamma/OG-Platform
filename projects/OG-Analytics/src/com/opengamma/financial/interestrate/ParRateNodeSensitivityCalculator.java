/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * 
 */
public class ParRateNodeSensitivityCalculator extends NodeSensitivityCalculator {
  private static final ParRateNodeSensitivityCalculator DEFAULT_INSTANCE = new ParRateNodeSensitivityCalculator();

  public static ParRateNodeSensitivityCalculator getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  public static ParRateNodeSensitivityCalculator using(final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> parRateSensitivityCalculator) {
    Validate.notNull(parRateSensitivityCalculator, "par rate sensitivity calculator");
    return new ParRateNodeSensitivityCalculator(parRateSensitivityCalculator);
  }

  private final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> _parRateSensitivityCalculator;

  public ParRateNodeSensitivityCalculator() {
    _parRateSensitivityCalculator = ParRateCurveSensitivityCalculator.getInstance();
  }

  public ParRateNodeSensitivityCalculator(final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> parRateSensitivityCalculator) {
    Validate.notNull(parRateSensitivityCalculator, "par rate sensitivity calculator");
    _parRateSensitivityCalculator = parRateSensitivityCalculator;
  }

  @Override
  public DoubleMatrix1D calculateSensitivities(final InstrumentDerivative ird, final YieldCurveBundle fixedCurves, final YieldCurveBundle interpolatedCurves) {
    return calculateSensitivities(ird, _parRateSensitivityCalculator, fixedCurves, interpolatedCurves);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _parRateSensitivityCalculator.hashCode();
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
    final ParRateNodeSensitivityCalculator other = (ParRateNodeSensitivityCalculator) obj;
    return ObjectUtils.equals(_parRateSensitivityCalculator, other._parRateSensitivityCalculator);
  }

}
