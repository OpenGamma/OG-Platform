/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import java.util.List;
import java.util.Map;

import com.opengamma.engine.function.config.AbstractRepositoryConfigurationBean;
import com.opengamma.engine.function.config.CombiningRepositoryConfigurationSource;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions;
import com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyInfo;
import com.opengamma.financial.analytics.model.forex.defaultproperties.DefaultPropertiesFunctions.CurrencyPairInfo;
import com.opengamma.financial.analytics.model.forex.forward.ForwardFunctions;
import com.opengamma.financial.analytics.model.forex.option.OptionFunctions;
import com.opengamma.util.tuple.Pair;

/**
 * Function repository configuration source for the functions contained in this package and its sub-packages.
 */
public class ForexFunctions extends AbstractRepositoryConfigurationBean {

  /**
   * Default instance of a repository configuration source exposing the functions from this package and its sub-packages.
   *
   * @return the configuration source exposing functions from this package and its sub-packages
   */
  public static RepositoryConfigurationSource instance() {
    return new ForexFunctions().getObjectCreating();
  }

  public static RepositoryConfigurationSource defaults(final Map<String, CurrencyInfo> perCurrencyInfo, final Map<Pair<String, String>, CurrencyPairInfo> perCurrencyPairInfo) {
    final DefaultPropertiesFunctions factory = new DefaultPropertiesFunctions();
    factory.setPerCurrencyInfo(perCurrencyInfo);
    factory.setPerCurrencyPairInfo(perCurrencyPairInfo);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  public static RepositoryConfigurationSource defaults(final Map<String, CurrencyInfo> perCurrencyInfo, final Map<Pair<String, String>, CurrencyPairInfo> perCurrencyPairInfo,
      final String interpolatorName, final String leftExtrapolatorName, final String rightExtrapolatorName) {
    final DefaultPropertiesFunctions factory = new DefaultPropertiesFunctions();
    factory.setPerCurrencyInfo(perCurrencyInfo);
    factory.setPerCurrencyPairInfo(perCurrencyPairInfo);
    factory.setInterpolatorName(interpolatorName);
    factory.setLeftExtrapolatorName(leftExtrapolatorName);
    factory.setRightExtrapolatorName(rightExtrapolatorName);
    factory.afterPropertiesSet();
    return factory.getObject();
  }

  @Override
  protected void addAllConfigurations(final List<FunctionConfiguration> functions) {
    functions.add(functionConfiguration(BloombergFXSpotRateMarketDataFunction.class));
  }

  protected RepositoryConfigurationSource forwardFunctionConfiguration() {
    return ForwardFunctions.instance();
  }

  protected RepositoryConfigurationSource optionFunctionConfiguration() {
    return OptionFunctions.instance();
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return new CombiningRepositoryConfigurationSource(super.createObject(), forwardFunctionConfiguration(), optionFunctionConfiguration());
  }

}
