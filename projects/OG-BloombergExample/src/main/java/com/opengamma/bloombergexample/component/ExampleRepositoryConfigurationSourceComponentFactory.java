/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bloombergexample.component;

import java.util.Arrays;
import java.util.List;

import com.opengamma.component.factory.source.RepositoryConfigurationSourceComponentFactory;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.curve.interestrate.MarketInstrumentImpliedYieldCurveFunction;

/**
 * Component factory for the repository configuration source.
 */
public class ExampleRepositoryConfigurationSourceComponentFactory extends RepositoryConfigurationSourceComponentFactory {

  @Override
  protected List<RepositoryConfigurationSource> initSources() {
    final List<RepositoryConfigurationSource> sources = super.initSources();
    final RepositoryConfiguration configuration = new RepositoryConfiguration();
    configuration.addFunctions(new ParameterizedFunctionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class.getName(), Arrays.asList("ParRate")));
    sources.add(new SimpleRepositoryConfigurationSource(configuration));
    return sources;
  }

}
