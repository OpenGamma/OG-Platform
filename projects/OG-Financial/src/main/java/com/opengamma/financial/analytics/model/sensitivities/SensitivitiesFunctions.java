/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.sensitivities;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class SensitivitiesFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new SensitivitiesFunctions()).getObjectCreating();

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
