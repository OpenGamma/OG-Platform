/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import java.util.List;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.swaption.deprecated.DeprecatedFunctions;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class LocalFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   */
  public static final RepositoryConfigurationSource DEFAULT = (new LocalFunctions()).getObjectCreating();

  public static RepositoryConfigurationSource deprecated() {
    return DeprecatedFunctions.DEFAULT;
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(ForexDupireLocalVolatilitySurfaceFunction.SABR.class));
    functions.add(functionConfiguration(ForexDupireLocalVolatilitySurfaceFunction.MixedLogNormal.class));
    functions.add(functionConfiguration(ForexDupireLocalVolatilitySurfaceFunction.Spline.class));
    functions.add(functionConfiguration(EquityDupireLocalVolatilitySurfaceFunction.SABR.class));
    functions.add(functionConfiguration(EquityDupireLocalVolatilitySurfaceFunction.MixedLogNormal.class));
    functions.add(functionConfiguration(EquityDupireLocalVolatilitySurfaceFunction.Spline.class));
  }

}
