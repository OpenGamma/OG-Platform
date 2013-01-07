/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondfutureoption;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class BondFutureOptionFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new BondFutureOptionFunctions()).getObjectCreating();

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BondFutureOptionBlackPresentValueFunction.class));
    functions.add(functionConfiguration(BondFutureOptionBlackDeltaFunction.class));
    functions.add(functionConfiguration(BondFutureOptionBlackGammaFunction.class));
    functions.add(functionConfiguration(BondFutureOptionBlackPV01Function.class));
    functions.add(functionConfiguration(BondFutureOptionBlackYCNSFunction.class));
    functions.add(functionConfiguration(BondFutureOptionBlackVegaFunction.class));
    functions.add(functionConfiguration(BondFutureOptionBlackFromFuturePresentValueFunction.class));
  }

}
