/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.function;

import static com.opengamma.web.spring.DemoStandardFunctionConfiguration.functionConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.engine.function.config.SimpleRepositoryConfigurationSource;
import com.opengamma.engine.function.config.StaticFunctionConfiguration;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.analytics.ircurve.YieldCurveInterpolatingFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveMarketDataFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveSpecificationFunction;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveFromYieldCurvesFunction;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveFromYieldCurvesPrimitiveDefaults;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveFromYieldCurvesSecurityDefaults;
import com.opengamma.financial.analytics.model.curve.future.IRFuturePriceCurveFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurveParRateMethodFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePresentValueMethodFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.YieldCurveDefaults;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.impl.ConfigSearchIterator;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates function repository configuration for curve supplying functions.
 * 
 * Note [PLAT-1094] - the functions should really be built by scanning the curves and currencies available.
 */
public class ExampleCurveFunctionConfiguration extends SingletonFactoryBean<RepositoryConfigurationSource> {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ExampleCurveFunctionConfiguration.class);

  /**
   * The config master.
   */
  private ConfigMaster _configMaster;
  /**
   * The convention bundle source.
   */
  @SuppressWarnings("unused")
  private ConventionBundleSource _conventionBundleSource; //TODO not sure if we'll need this in the future

  public void setConfigMaster(final ConfigMaster configMaster) {
    _configMaster = configMaster;
  }

  public void setConventionBundleSource(final ConventionBundleSource conventionBundleSource) {
    _conventionBundleSource = conventionBundleSource;
  }

  public RepositoryConfiguration constructRepositoryConfiguration() {
    final List<FunctionConfiguration> configs = new ArrayList<FunctionConfiguration>();

    if (_configMaster != null) {
      // [PLAT-1094] Scan the config master for documents. This is probably in the wrong place; the code should live in OG-Financial as it is
      // tightly coupled to the ConfigDbInterpolatedYieldCurveSource and MarketInstrumentImpliedYieldCurveFunction classes
      final ConfigSearchRequest<YieldCurveDefinition> searchRequest = new ConfigSearchRequest<YieldCurveDefinition>();
      searchRequest.setType(YieldCurveDefinition.class);
      for (final ConfigDocument configDocument : ConfigSearchIterator.iterable(_configMaster, searchRequest)) {
        final String documentName = configDocument.getName();
        final int underscore = documentName.lastIndexOf('_');
        if (underscore <= 0) {
          continue;
        }
        final String curveName = documentName.substring(0, underscore);
        final String currencyISO = documentName.substring(underscore + 1);
        s_logger.info("Found {} curve for {}", curveName, currencyISO);
        addYieldCurveFunction(configs, currencyISO, curveName);
      }
    }
    s_logger.info("Created repository configuration with {} curve provider functions", configs.size());

    addFXForwardCurveFunction(configs);
    s_logger.info("Added FX forward curve to repository configuration");
    addFutureCurveFunction(configs);
    s_logger.info("Added future curve to repository configuration");
    addYieldCurveFunctionConfigs(configs);
    s_logger.info("Added yield curves to repository configuration");
    return new RepositoryConfiguration(configs);
  }

  private void addYieldCurveFunctionConfigs(final List<FunctionConfiguration> functionConfigs) {
    functionConfigs.add(functionConfiguration(MultiYieldCurvePresentValueMethodFunction.class));
    functionConfigs.add(functionConfiguration(MultiYieldCurveParRateMethodFunction.class));
    functionConfigs.add(functionConfiguration(YieldCurveDefaults.class, "0.0001", "0.0001", "1000", DecompositionFactory.SV_COLT_NAME, "false", "USD",
        "EUR", "GBP", "JPY", "CHF", "AUD"));
  }

  private void addYieldCurveFunction(final List<FunctionConfiguration> configs, final String currency, final String curveName) {
    configs.add(new ParameterizedFunctionConfiguration(YieldCurveMarketDataFunction.class.getName(), Arrays.asList(currency, curveName)));
    configs.add(new ParameterizedFunctionConfiguration(YieldCurveInterpolatingFunction.class.getName(), Arrays.asList(currency, curveName)));
    configs.add(new ParameterizedFunctionConfiguration(YieldCurveSpecificationFunction.class.getName(), Arrays.asList(currency, curveName)));
  }

  private void addFXForwardCurveFunction(final List<FunctionConfiguration> configs) {
    configs.add(new StaticFunctionConfiguration(FXForwardCurveFromYieldCurvesFunction.class.getName()));
    configs.add(new ParameterizedFunctionConfiguration(FXForwardCurveFromYieldCurvesPrimitiveDefaults.class.getName(),
        Arrays.asList(
            "USD", "DefaultTwoCurveUSDConfig", "Discounting",
            "EUR", "DefaultTwoCurveEURConfig", "Discounting")));
    configs.add(new ParameterizedFunctionConfiguration(FXForwardCurveFromYieldCurvesSecurityDefaults.class.getName(),
        Arrays.asList(
            "USD", "DefaultTwoCurveUSDConfig", "Discounting",
            "EUR", "DefaultTwoCurveEURConfig", "Discounting")));
  }

  private void addFutureCurveFunction(final List<FunctionConfiguration> configs) {
    configs.add(new StaticFunctionConfiguration(IRFuturePriceCurveFunction.class.getName()));
  }

  //-------------------------------------------------------------------------
  public RepositoryConfigurationSource constructRepositoryConfigurationSource() {
    return new SimpleRepositoryConfigurationSource(constructRepositoryConfiguration());
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return constructRepositoryConfigurationSource();
  }

}
