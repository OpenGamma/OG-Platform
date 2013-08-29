/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.curve.interestrate.sensitivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The return format is a vector (DoubleMatrix1D) with length equal to the total number of parameters in all the curves,
 * and ordered as the parameters to the different curves themselves in increasing order.
 * @deprecated Curve builders that use and populate {@link YieldCurveBundle}s are deprecated.
 */
@Deprecated
public class ParameterSensitivityCalculator extends AbstractParameterSensitivityCalculator {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterSensitivityCalculator(final InstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities to the continuously compounded rate.
   * @param sensitivity The point sensitivity.
   * @param fixedCurves The fixed curves names (for which the parameter sensitivity are not computed even if they are necessary for the instrument pricing).
   * The curve in the list may or may not be in the bundle. Not null.
   * @param bundle The curve bundle with all the curves with respect to which the sensitivity should be computed. Not null.
   * @return The sensitivity (as a DoubleMatrix1D).
   */
  @Override
  public DoubleMatrix1D pointToParameterSensitivity(final InterestRateCurveSensitivity sensitivity, final Set<String> fixedCurves, final YieldCurveBundle bundle) {
    final List<Double> result = new ArrayList<>();
    for (final String curveName : bundle.getAllNames()) {
      if (!fixedCurves.contains(curveName)) {
        final YieldAndDiscountCurve curve = bundle.getCurve(curveName);
        final List<Double> oneCurveSensitivity = pointToParameterSensitivity(sensitivity.getSensitivities().get(curveName), curve);
        result.addAll(oneCurveSensitivity);
      }
    }
    return new DoubleMatrix1D(result.toArray(new Double[result.size()]));
  }

}
