/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.volatility.local;

import java.util.List;
import java.util.Map;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.swaption.deprecated.DeprecatedFunctions;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions;
import com.opengamma.financial.analytics.model.volatility.local.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo;

/**
 * Function repository configuration source for the functions contained in this package and sub-packages.
 */
public class LocalFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package.
   *
   * @return the configuration source exposing functions from this package
   */
  public static RepositoryConfigurationSource instance() {
    return new LocalFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource deprecated() {
    return new DeprecatedFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource defaults(final Map<String, CurrencyInfo> perCurrencyInfo) {
    final DefaultPropertiesFunctions factory = new DefaultPropertiesFunctions();
    factory.setPerCurrencyInfo(perCurrencyInfo);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  public static RepositoryConfigurationSource defaults(final Map<String, CurrencyInfo> perCurrencyInfo, final double theta, final int nTimeSteps, final int nSpaceSteps, final double timeStepBunching,
      final double spaceStepBunching,
      final double maxProxyDelta, final double centreMoneyness, final double maxMoneynessScale, final String spaceDirectionInterpolator) {
    final DefaultPropertiesFunctions factory = new DefaultPropertiesFunctions();
    factory.setPerCurrencyInfo(perCurrencyInfo);
    factory.setTheta(theta);
    factory.setNTimeSteps(nTimeSteps);
    factory.setNSpaceSteps(nSpaceSteps);
    factory.setTimeStepBunching(timeStepBunching);
    factory.setSpaceStepBunching(spaceStepBunching);
    factory.setMaxProxyDelta(maxProxyDelta);
    factory.setCentreMoneyness(centreMoneyness);
    factory.setMaxMoneynessScale(maxMoneynessScale);
    factory.setSpaceDirectionInterpolator(spaceDirectionInterpolator);
    factory.afterPropertiesSet();
    return factory.getObject();
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
