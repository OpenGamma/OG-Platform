/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.inflationissuer;

import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.inflation.InflationIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.inflation.InflationSensitivity;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 *  For an instrument, computes the sensitivity of double value (often par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.) and also on the way
 * the parameters sensitivities are aggregated (the same parameter can be used in several curves).
 * The output is a DoubleMatrix1D.
 */
public abstract class ParameterSensitivityInflationIssuerMatrixProviderAbstractCalculator {

  /**
   * The sensitivity calculator to compute the sensitivity of the value with respect to the zero-coupon continuously compounded rates at different times for the discounting curve
   * and with respect to forward rates for the forward curves.
   */
  private final InstrumentDerivativeVisitor<InflationIssuerProviderInterface, InflationSensitivity> _curveSensitivityCalculator;

  /**
   * The constructor from a curve sensitivity calculator.
   * @param curveSensitivityCalculator The calculator.
   */
  public ParameterSensitivityInflationIssuerMatrixProviderAbstractCalculator(final InstrumentDerivativeVisitor<InflationIssuerProviderInterface, InflationSensitivity> curveSensitivityCalculator) {
    ArgumentChecker.notNull(curveSensitivityCalculator, "Sensitivity calculator");
    _curveSensitivityCalculator = curveSensitivityCalculator;
  }

  /**
   * Computes the sensitivity with respect to the parameters.
   * The sensitivities are stored in a DoubleMatrix1D in the order of the currencies and indexes.
   * @param instrument The instrument. Not null.
   * @param inflationCurves The inflation provider. Not null.
   * The sensitivity with respect to the curves in the fixedCurves list will not be part of the output total sensitivity. Not null.
   * @param curvesSet The set of curves for which the sensitivity will be computed. The multi-curve may contain more curves and other curves can be in the
   * instrument sensitivity but only the one in the set will be in the output. The curve order in the output is the set order.
   * @return The sensitivity (as a DoubleMatrix1D).
   */
  public DoubleMatrix1D calculateSensitivity(final InstrumentDerivative instrument, final InflationIssuerProviderInterface inflationCurves, final Set<String> curvesSet) {
    Validate.notNull(instrument, "null InterestRateDerivative");
    Validate.notNull(inflationCurves, "null inflationcurves");
    Validate.notNull(curvesSet, "null curves set");
    InflationSensitivity sensitivity = instrument.accept(_curveSensitivityCalculator, inflationCurves);
    sensitivity = sensitivity.cleaned();
    return pointToParameterSensitivity(sensitivity, inflationCurves, curvesSet);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities.
   * @param sensitivity The point sensitivity.
   * @param inflation The inflation provider. Not null.
   * @param curvesSet The set of curves for which the sensitivity will be computed. Not null.
   * @return The sensitivity (as a DoubleMatrix1D).
   */
  public abstract DoubleMatrix1D pointToParameterSensitivity(final InflationSensitivity sensitivity, final InflationIssuerProviderInterface inflation, final Set<String> curvesSet);
}
