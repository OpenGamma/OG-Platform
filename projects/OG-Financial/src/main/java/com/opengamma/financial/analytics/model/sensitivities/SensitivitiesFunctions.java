/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sensitivities;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.pnl.ExternallyProvidedSensitivityPnLFunction;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class SensitivitiesFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new SensitivitiesFunctions()).getObjectCreating();

  public static RepositoryConfigurationSource calculators(final String htsResolutionKey) {
    final List<FunctionConfiguration> functions = new ArrayList<FunctionConfiguration>();
    functions.add(functionConfiguration(ExternallyProvidedSensitivityPnLFunction.class, htsResolutionKey));
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(functions));
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(ExternallyProvidedSecurityMarkFunction.class));
    functions.add(functionConfiguration(ExternallyProvidedSensitivitiesCreditFactorsFunction.class));
    functions.add(functionConfiguration(ExternallyProvidedSensitivitiesNonYieldCurveFunction.class));
    functions.add(functionConfiguration(ExternallyProvidedSensitivitiesYieldCurveCS01Function.class));
    functions.add(functionConfiguration(ExternallyProvidedSensitivitiesYieldCurveNodeSensitivitiesFunction.class));
    functions.add(functionConfiguration(ExternallyProvidedSensitivitiesYieldCurvePV01Function.class));
  }

}
