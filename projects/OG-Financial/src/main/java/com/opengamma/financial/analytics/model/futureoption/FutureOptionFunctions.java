/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.futureoption;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class FutureOptionFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new FutureOptionFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(CommodityFutureOptionBlackDeltaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBlackForwardDeltaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBlackForwardGammaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBlackGammaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBlackPVFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBlackThetaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBlackVegaFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBAWPVFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBAWGreeksFunction.class));
    functions.add(functionConfiguration(CommodityFutureOptionBjerksundStenslandGreeksFunction.class));
  }

}
