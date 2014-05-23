/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.issuer;

import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * For an instrument, computes the sensitivity of double value (often par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.) and also on the way
 * the parameters sensitivities are aggregated (the same parameter can be used in several curves).
 * The output is a DoubleMatrix1D.
 */
public abstract class AbstractParameterSensitivityIssuerMatrixCalculator {

  /**
   * The sensitivity calculator to compute the sensitivity of the value with respect to the zero-coupon continuously compounded rates at different times for the discounting curve
   * and with respect to forward rates for the forward curves.
   */
  private final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MulticurveSensitivity> _curveSensitivityCalculator;

  /**
   * The constructor from a curve sensitivity calculator.
   * @param curveSensitivityCalculator The calculator.
   */
  public AbstractParameterSensitivityIssuerMatrixCalculator(final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MulticurveSensitivity> curveSensitivityCalculator) {
    ArgumentChecker.notNull(curveSensitivityCalculator, "Sensitivity calculator");
    _curveSensitivityCalculator = curveSensitivityCalculator;
  }

  /**
   * Computes the sensitivity with respect to the parameters.
   * The sensitivities are stored in a DoubleMatrix1D in the order of the currencies and indexes.
   * @param instrument The instrument. Not null.
   * @param multicurves The multi-curve provider. Not null.
   * The sensitivity with respect to the curves in the fixedCurves list will not be part of the output total sensitivity. Not null.
   * @param curvesSet The set of curves for which the sensitivity will be computed. The multi-curve may contain more curves and other curves can be in the
   * instrument sensitivity but only the one in the set will be in the output. The curve order in the output is the set order.
   * @return The sensitivity (as a DoubleMatrix1D).
   */
  public DoubleMatrix1D calculateSensitivity(final InstrumentDerivative instrument, final ParameterIssuerProviderInterface multicurves, final Set<String> curvesSet) {
    Validate.notNull(instrument, "null InterestRateDerivative");
    Validate.notNull(multicurves, "null multicurve");
    Validate.notNull(curvesSet, "null curves set");
    MulticurveSensitivity sensitivity = instrument.accept(_curveSensitivityCalculator, multicurves);
    sensitivity = sensitivity.cleaned();
    // TODO: for testing purposes mainly. Could be removed after the tests.
    return pointToParameterSensitivity(sensitivity, multicurves, curvesSet);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities.
   * @param sensitivity The point sensitivity.
   * @param multicurves The multi-curve provider. Not null.
   * @param curvesSet The set of curves for which the sensitivity will be computed. Not null.
   * @return The sensitivity (as a DoubleMatrix1D).
   */
  public abstract DoubleMatrix1D pointToParameterSensitivity(final MulticurveSensitivity sensitivity, final ParameterIssuerProviderInterface multicurves, final Set<String> curvesSet);

}
