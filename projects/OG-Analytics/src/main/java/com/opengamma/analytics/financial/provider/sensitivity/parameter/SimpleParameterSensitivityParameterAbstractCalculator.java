/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.parameter;

import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.SimpleParameterSensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * For an instrument, computes the sensitivity of a multiple currency value (often the present value) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.) and also on the way
 * the parameters sensitivities are aggregated (the same parameter can be used in several curves).
 * @param <DATA_TYPE> Data type.
 */
public abstract class SimpleParameterSensitivityParameterAbstractCalculator<DATA_TYPE extends ParameterProviderInterface> {

  /**
   * The sensitivity calculator to compute the sensitivity of the (double) value with respect to the zero-coupon continuously compounded rates at different times (discounting) or forward rates.
   */
  private final InstrumentDerivativeVisitor<DATA_TYPE, MulticurveSensitivity> _curveSensitivityCalculator;

  /**
   * The constructor from a curve sensitivity calculator.
   * @param curveSensitivityCalculator The calculator.
   */
  public SimpleParameterSensitivityParameterAbstractCalculator(final InstrumentDerivativeVisitor<DATA_TYPE, MulticurveSensitivity> curveSensitivityCalculator) {
    ArgumentChecker.notNull(curveSensitivityCalculator, "curve sensitivity calculator");
    _curveSensitivityCalculator = curveSensitivityCalculator;
  }

  /**
   * Computes the sensitivity with respect to the parameters.
   * @param instrument The instrument. Not null.
   * @param parameterMulticurves The parameters and multi-curves provider.
   * @param curvesSet The set of curves for which the sensitivity will be computed. The multi-curve may contain more curves and other curves can be in the
   * instrument sensitivity but only the one in the set will be in the output. The curve order in the output is the set order.
   * @return The sensitivity (as a ParameterSensitivity).
   */
  public SimpleParameterSensitivity calculateSensitivity(final InstrumentDerivative instrument, final DATA_TYPE parameterMulticurves, final Set<String> curvesSet) {
    ArgumentChecker.notNull(instrument, "derivative");
    ArgumentChecker.notNull(parameterMulticurves, "Black data");
    ArgumentChecker.notNull(curvesSet, "curves");
    MulticurveSensitivity sensitivity = instrument.accept(_curveSensitivityCalculator, parameterMulticurves);
    sensitivity = sensitivity.cleaned(); // TODO: for testing purposes mainly. Could be removed after the tests.
    return pointToParameterSensitivity(sensitivity, parameterMulticurves, curvesSet);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities to the continuously compounded rate.
   * @param sensitivity The point sensitivity.
   * @param parameterMulticurves The parameters and multi-curves provider.
   * @param curvesSet The set of curves for which the sensitivity will be computed. The multi-curve may contain more curves and other curves can be in the
   * instrument sensitivity but only the one in the set will be in the output. The curve order in the output is the set order.
   * @return The sensitivity (as a ParameterSensitivity).
   */
  public abstract SimpleParameterSensitivity pointToParameterSensitivity(final MulticurveSensitivity sensitivity, final DATA_TYPE parameterMulticurves, final Set<String> curvesSet);
}
