/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.black;

import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.BlackSwaptionProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyMulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;

/**
 * 
 */
public class ParameterSensitivityBlackSwaptionCalculator extends AbstractParameterSensitivityBlackSwaptionProviderCalculator {

  public ParameterSensitivityBlackSwaptionCalculator(
      final InstrumentDerivativeVisitor<BlackSwaptionProviderInterface, MultipleCurrencyMulticurveSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  @Override
  public MultipleCurrencyParameterSensitivity pointToParameterSensitivity(final MultipleCurrencyMulticurveSensitivity sensitivity, final BlackSwaptionProviderInterface blackData,
      final Set<String> curvesSet) {
    return null;
  }

}
