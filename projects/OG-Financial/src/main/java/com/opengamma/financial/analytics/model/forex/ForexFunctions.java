/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.forex.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.forex.option.OptionFunctions;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class ForexFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new ForexFunctions()).getObjectCreating();

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BloombergFXSpotRateMarketDataFunction.class));
  }

  protected RepositoryConfigurationSource forwardFunctionConfiguration() {
    return ForwardFunctions.DEFAULT;
  }

  protected RepositoryConfigurationSource optionFunctionConfiguration() {
    return OptionFunctions.DEFAULT;
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return new CombiningRepositoryConfigurationSource(super.createObject(), forwardFunctionConfiguration(), optionFunctionConfiguration());
  }

}
