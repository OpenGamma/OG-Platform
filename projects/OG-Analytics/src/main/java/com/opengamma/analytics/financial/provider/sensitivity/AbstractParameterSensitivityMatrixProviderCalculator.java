/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity;

import java.util.Set;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.market.description.CurveSensitivityMarket;
import com.opengamma.analytics.financial.provider.description.MulticurveProviderInterface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;

/**
 * For an instrument, computes the sensitivity of double value (often par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.) and also on the way 
 * the parameters sensitivities are aggregated (the same parameter can be used in several curves).
 * The output is a DoubleMatrix1D.
 */
public abstract class AbstractParameterSensitivityMatrixProviderCalculator {

  /**
   * The sensitivity calculator to compute the sensitivity of the value with respect to the zero-coupon continuously compounded rates at different times for the discounting curve
   * and with respect to forward rates for the forward curves.
   */
  private final InstrumentDerivativeVisitor<MulticurveProviderInterface, CurveSensitivityMarket> _curveSensitivityCalculator;

  /**
   * The constructor from a curve sensitivity calculator.
   * @param curveSensitivityCalculator The calculator.
   */
  public AbstractParameterSensitivityMatrixProviderCalculator(InstrumentDerivativeVisitor<MulticurveProviderInterface, CurveSensitivityMarket> curveSensitivityCalculator) {
    ArgumentChecker.notNull(curveSensitivityCalculator, "Sensitivity calculator");
    _curveSensitivityCalculator = curveSensitivityCalculator;
  }

  /**
   * Computes the sensitivity with respect to the parameters.
   * The sensitivities are stored in a DoubleMatrix1D in the order of the currencies and indexes.
   * @param instrument The instrument. Not null.
   * @param fixedCurves The fixed curves names (for which the parameter sensitivity are not computed even if they are necessary for the instrument pricing).
   * The curve in the list may or may not be in the bundle. Not null.
   * @param multicurve The multi-curve provider. Not null.
   * The sensitivity with respect to the curves in the fixedCurves list will not be part of the output total sensitivity. Not null.
   * @return The sensitivity (as a DoubleMatrix1D).
   */
  public DoubleMatrix1D calculateSensitivity(final InstrumentDerivative instrument, final Set<String> fixedCurves, final MulticurveProviderInterface multicurve) {
    Validate.notNull(instrument, "null InterestRateDerivative");
    Validate.notNull(fixedCurves, "null set of fixed curves.");
    Validate.notNull(multicurve, "null multicurve");
    CurveSensitivityMarket sensitivity = _curveSensitivityCalculator.visit(instrument, multicurve);
    sensitivity = sensitivity.cleaned();
    // TODO: for testing purposes mainly. Could be removed after the tests.
    return pointToParameterSensitivity(sensitivity, fixedCurves, multicurve);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities.
   * @param sensitivity The point sensitivity.
   * @param fixedCurves The fixed curves names (for which the parameter sensitivity are not computed even if they are necessary for the instrument pricing).
   * The curve in the list may or may not be in the bundle. Not null.
   * @param multicurve The multi-curve provider. Not null.
   * @return The sensitivity (as a DoubleMatrix1D).
   */
  public abstract DoubleMatrix1D pointToParameterSensitivity(final CurveSensitivityMarket sensitivity, final Set<String> fixedCurves, final MulticurveProviderInterface multicurve);

}
