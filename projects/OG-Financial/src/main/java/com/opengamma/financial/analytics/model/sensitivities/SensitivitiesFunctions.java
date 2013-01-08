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
import com.opengamma.financial.analytics.model.pnl.ExternallyProvidedSensitivityPnLFunction;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class SensitivitiesFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new SensitivitiesFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource calculators(final String htsResolutionKey) {
    final Calculators factory = new Calculators();
    factory.setHtsResolutionKey(htsResolutionKey);
    return factory.getObjectCreating();
  }

  /**
   * Function repository configuration source for the configurable functions contained in this package.
   */
  public static class Calculators extends AbstractRepositoryConfigurationBean {

    private String _htsResolutionKey;

    public void setHtsResolutionKey(final String htsResolutionKey) {
      _htsResolutionKey = htsResolutionKey;
    }

    public String getHtsResolutionKey() {
      return _htsResolutionKey;
    }

    @Override
    protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
      functions.add(functionConfiguration(ExternallyProvidedSensitivityPnLFunction.class, getHtsResolutionKey()));
    }

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
