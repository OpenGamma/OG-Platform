/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.black;

import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.BlackSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public abstract class AbstractParameterSensitivityBlackSwaptionProviderCalculator {
  private final InstrumentDerivativeVisitor<BlackSwaptionProviderInterface, MultipleCurrencyMulticurveSensitivity> _curveSensitivityCalculator;

  public AbstractParameterSensitivityBlackSwaptionProviderCalculator(
      final InstrumentDerivativeVisitor<BlackSwaptionProviderInterface, MultipleCurrencyMulticurveSensitivity> curveSensitivityCalculator) {
    ArgumentChecker.notNull(curveSensitivityCalculator, "curve sensitivity calculator");
    _curveSensitivityCalculator = curveSensitivityCalculator;
  }

  public MultipleCurrencyParameterSensitivity calculateSensitivity(final InstrumentDerivative instrument, final BlackSwaptionProviderInterface blackData, final Set<String> curvesSet) {
    ArgumentChecker.notNull(instrument, "derivative");
    ArgumentChecker.notNull(blackData, "Black data");
    ArgumentChecker.notNull(curvesSet, "curves");
    MultipleCurrencyMulticurveSensitivity sensitivity = instrument.accept(_curveSensitivityCalculator, blackData);
    sensitivity = sensitivity.cleaned(); // TODO: for testing purposes mainly. Could be removed after the tests.
    return pointToParameterSensitivity(sensitivity, blackData, curvesSet);
  }

  public abstract MultipleCurrencyParameterSensitivity pointToParameterSensitivity(final MultipleCurrencyMulticurveSensitivity sensitivity, final BlackSwaptionProviderInterface blackData,
      Set<String> curvesSet);
}
