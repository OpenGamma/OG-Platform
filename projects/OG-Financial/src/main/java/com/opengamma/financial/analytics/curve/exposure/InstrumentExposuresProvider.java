/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import java.util.Set;

import com.opengamma.financial.security.FinancialSecurity;

/**
 *
 */
public interface InstrumentExposuresProvider {

  Set<String> getCurveConstructionConfigurationsForConfig(String instrumentExposureConfigurationName, FinancialSecurity security);
}
