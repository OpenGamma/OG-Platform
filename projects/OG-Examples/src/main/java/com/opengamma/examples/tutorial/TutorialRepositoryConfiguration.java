/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.tutorial;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates a repository configuration containing the example functions.
 */
public class TutorialRepositoryConfiguration extends SingletonFactoryBean<RepositoryConfigurationSource> {

  protected void getFunctions(final List<FunctionConfiguration> functions) {
    functions.add(new StaticFunctionConfiguration(TutorialValueFunction.class.getName()));
  }

  protected RepositoryConfiguration createConfiguration() {
    final List<FunctionConfiguration> functions = new ArrayList<FunctionConfiguration>();
    getFunctions(functions);
    return new RepositoryConfiguration(functions);
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return new SimpleRepositoryConfigurationSource(createConfiguration());
  }

}
