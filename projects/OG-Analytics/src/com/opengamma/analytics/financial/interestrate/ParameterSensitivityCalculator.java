/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The return format is a vector (DoubleMatrix1D) with length equal to the total number of parameters in all the curves, 
 * and ordered as the parameters to the different curves themselves in increasing order. 
 */
public class ParameterSensitivityCalculator {

  /**
   * The sensitivity calculator to compute the sensitivity of the value with respect to the zero-coupon continuously compounded rates at different times.
   */
  private final AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> _curveSensitivityCalculator;

  public ParameterSensitivityCalculator(AbstractInstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> curveSensitivityCalculator) {
    ArgumentChecker.notNull(curveSensitivityCalculator, "Sensitivity calculator");
    _curveSensitivityCalculator = curveSensitivityCalculator;
  }

  public DoubleMatrix1D calculateSensitivity(final InstrumentDerivative derivative, final YieldCurveBundle fixedCurves, final YieldCurveBundle sensitivityCurves) {
    Validate.notNull(derivative, "null InterestRateDerivative");
    Validate.notNull(sensitivityCurves, "interpolated curves");
    final YieldCurveBundle allCurves = sensitivityCurves.copy();
    if (fixedCurves != null) {
      for (final String name : sensitivityCurves.getAllNames()) {
        Validate.isTrue(!fixedCurves.containsName(name), "Fixed curves contain a name that is also in the sensitivity curves");
      }
      allCurves.addAll(fixedCurves);
    }
    final InterestRateCurveSensitivity sensitivity = _curveSensitivityCalculator.visit(derivative, allCurves);
    return pointToParameterSensitivity(sensitivity, sensitivityCurves);
  }

  public DoubleMatrix1D pointToParameterSensitivity(final InterestRateCurveSensitivity sensitivity, final YieldCurveBundle sensitivityCurves) {
    final List<Double> result = new ArrayList<Double>();
    for (final String name : sensitivityCurves.getAllNames()) { // loop over all curves (by name)
      final YieldAndDiscountCurve curve = sensitivityCurves.getCurve(name);
      result.addAll(pointToParameterSensitivity(sensitivity.getSensitivities().get(name), curve));
    }
    return new DoubleMatrix1D(result.toArray(new Double[0]));
  }

  private List<Double> pointToParameterSensitivity(final List<DoublesPair> sensitivity, final YieldAndDiscountCurve curve) {
    final List<Double> result = new ArrayList<Double>();
    if (sensitivity != null && sensitivity.size() > 0) {
      final double[][] sensitivityYP = new double[sensitivity.size()][];
      // Implementation note: Sensitivity of the interpolated yield to the parameters
      int k = 0;
      for (final DoublesPair timeAndS : sensitivity) {
        sensitivityYP[k++] = curve.getInterestRateParameterSensitivity(timeAndS.getFirst());
      }
      for (int j = 0; j < sensitivityYP[0].length; j++) {
        double temp = 0.0;
        k = 0;
        for (final DoublesPair timeAndS : sensitivity) {
          temp += timeAndS.getSecond() * sensitivityYP[k++][j];
        }
        result.add(temp);
      }
    } else {
      // TODO: for the extreme case where the initial sensitivity is empty, a list of zeros should be provided
    }
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curveSensitivityCalculator.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ParameterSensitivityCalculator other = (ParameterSensitivityCalculator) obj;
    return ObjectUtils.equals(_curveSensitivityCalculator, other._curveSensitivityCalculator);
  }

}
