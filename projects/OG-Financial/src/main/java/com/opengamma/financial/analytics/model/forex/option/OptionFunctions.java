/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option;

import java.util.Collections;
import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.forex.option.black.BlackFunctions;
import com.opengamma.financial.analytics.model.forex.option.localvol.LocalVolFunctions;

/**
 * Function repository configuration source for the functions contained in this package and its sub-packages.
 */
public class OptionFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static RepositoryConfigurationSource instance() {
    return new OptionFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BloombergFXSpotRatePercentageChangeFunction.class));
    functions.add(functionConfiguration(BloombergFXOptionSpotRateFunction.class));
  }

  protected RepositoryConfigurationSource blackFunctionConfiguration() {
    return BlackFunctions.instance();
  }

  protected RepositoryConfigurationSource callSpreadBlackFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  protected RepositoryConfigurationSource localVolFunctionConfiguration() {
    return LocalVolFunctions.instance();
  }

  protected RepositoryConfigurationSource vannaVolgaFunctionConfiguration() {
    // TODO
    return new SimpleRepositoryConfigurationSource(new RepositoryConfiguration(Collections.<FunctionConfiguration>emptyList()));
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return CombiningRepositoryConfigurationSource.of(super.createObject(), blackFunctionConfiguration(), callSpreadBlackFunctionConfiguration(), localVolFunctionConfiguration(),
        vannaVolgaFunctionConfiguration());
  }

}
