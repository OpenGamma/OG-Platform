/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.security.FinancialSecurity;

/**
 * Provides a set of names of {@link CurveConstructionConfiguration}s that are required to price
 * a security for a given {@link ExposureFunction}.
 */
public interface InstrumentExposuresProvider {

  /**
   * Gets a list of relevant curve construction configurations for a given {@link ExposureFunction} and
   * {@link FinancialSecurity}
   * @param instrumentExposureConfigurationName The instrument exposure configuration name
   * @param security The security
   * @return A set of {@link CurveConstructionConfiguration} names
   * @throws OpenGammaRuntimeException If no matching configuration(s) are found for the security
   */
  Set<String> getCurveConstructionConfigurationsForConfig(String instrumentExposureConfigurationName, FinancialSecurity security);
}
