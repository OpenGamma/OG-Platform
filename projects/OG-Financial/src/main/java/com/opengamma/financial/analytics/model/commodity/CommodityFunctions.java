/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.commodity;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;

/**
 * Function repository configuration source for the functions contained in this package.
 */
public class CommodityFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   * 
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new CommodityFunctions().getObjectCreating();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(CommodityForwardCurveFromFutureCurveFunction.class));
  }

}
