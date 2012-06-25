/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.spring;

import static com.opengamma.web.spring.DemoStandardFunctionConfiguration.functionConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

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
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveFromMarketQuotesDefaults;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveFromMarketQuotesFunction;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveFromYieldCurveDefaultPropertiesFunction;
import com.opengamma.financial.analytics.model.curve.forward.FXForwardCurveFromYieldCurveFunction;
import com.opengamma.financial.analytics.model.curve.forward.ForwardSwapCurveFromMarketQuotesDefaults;
import com.opengamma.financial.analytics.model.curve.forward.ForwardSwapCurveFromMarketQuotesFunction;
import com.opengamma.financial.analytics.model.curve.forward.ForwardSwapCurveMarketDataFunction;
import com.opengamma.financial.analytics.model.curve.future.FuturePriceCurveFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveDefaultsNew;
import com.opengamma.financial.analytics.model.curve.interestrate.FXImpliedYieldCurveFunctionNew;
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurveParRateMethodFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.MultiYieldCurvePresentValueMethodFunction;
import com.opengamma.financial.analytics.model.curve.interestrate.YieldCurveDefaults;
import com.opengamma.financial.analytics.model.volatility.cube.RawSwaptionVolatilityCubeDataFunction;
import com.opengamma.financial.analytics.model.volatility.cube.SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew;
import com.opengamma.financial.analytics.model.volatility.cube.defaults.SABRNonLinearSwaptionVolatilityCubeFittingDefaults;
import com.opengamma.financial.analytics.volatility.cube.BloombergSwaptionVolatilityCubeInstrumentProvider;
import com.opengamma.financial.analytics.volatility.cube.BloombergVolatilityCubeDefinitionSource;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeFunction;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeMarketDataFunction;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.money.Currency;

/**
 * Creates function repository configuration for curve supplying functions.
 * 
 * Note [PLAT-1094] - the functions should really be built by scanning the curves and currencies available.
 */
public class DemoCurveFunctionConfiguration extends SingletonFactoryBean<RepositoryConfigurationSource> {

  private static final Logger s_logger = LoggerFactory.getLogger(DemoCurveFunctionConfiguration.class);

  private ConfigMaster _configMaster;
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
    configs.add(new StaticFunctionConfiguration(MultiYieldCurvePresentValueMethodFunction.class.getName()));
    configs.add(new StaticFunctionConfiguration(MultiYieldCurveParRateMethodFunction.class.getName()));
    configs.add(functionConfiguration(YieldCurveDefaults.class, "0.0001", "0.0001", "1000", DecompositionFactory.SV_COLT_NAME, "false", "USD", "CHF", "CAD", "GBP", "AUD"));
    configs.add(functionConfiguration(FXImpliedYieldCurveFunctionNew.class));
    configs.add(functionConfiguration(FXImpliedYieldCurveDefaultsNew.class, "0.0001", "0.0001", "1000", DecompositionFactory.SV_COLT_NAME, "false",
        "DoubleQuadratic", "LinearExtrapolator", "FlatExtrapolator", "MYR"));


    if (_configMaster != null) {
      // [PLAT-1094] Scan the config master for documents. This is probably in the wrong place; the code should live in OG-Financial as it is
      // tightly coupled to the ConfigDbInterpolatedYieldCurveSource and MarketInstrumentImpliedYieldCurveFunction classes
      final ConfigSearchRequest<YieldCurveDefinition> searchRequest = new ConfigSearchRequest<YieldCurveDefinition>();
      searchRequest.setType(YieldCurveDefinition.class);

      final ConfigSearchResult<YieldCurveDefinition> searchResult = _configMaster.search(searchRequest);
      for (final ConfigDocument<YieldCurveDefinition> configDocument : searchResult.getDocuments()) {
        final String documentName = configDocument.getName();
        final int underscore = documentName.lastIndexOf('_');
        if (underscore <= 0) {
          continue;
        }
        final String curveName = documentName.substring(0, underscore);
        final String currencyISO = documentName.substring(underscore + 1);
        s_logger.debug("Found {} curve for {}", curveName, currencyISO);
        addYieldCurveFunction(configs, currencyISO, curveName);
      }
    } else {
      // [PLAT-1094] This is the wrong approach and should be disposed of at the earliest opportunity
      s_logger.warn("[PLAT-1094] Using hardcoded curve definitions");
      addYieldCurveFunction(configs, "USD", "FUNDING");
      addYieldCurveFunction(configs, "USD", "FORWARD_3M");
      addYieldCurveFunction(configs, "GBP", "FUNDING");
      addYieldCurveFunction(configs, "GBP", "FORWARD_6M");
      addYieldCurveFunction(configs, "USD", "SECONDARY");
      addYieldCurveFunction(configs, "GBP", "SECONDARY");
    }
    s_logger.info("Created repository configuration with {} curve provider functions", configs.size());

    addSwaptionVolCubeFunction(configs);
    s_logger.info("Added swaption vol cube to repository configuration");
    addFXForwardCurveFunction(configs);
    s_logger.info("Added FX forward curve to repository configuration");
    addFutureCurveFunction(configs);
    s_logger.info("Added future curve to repository configuration");
    return new RepositoryConfiguration(configs);
  }

  private void addSwaptionVolCubeFunction(final List<FunctionConfiguration> configs) {
    //These need to be replaced with meaningful cube defns
    addVolatilityCubeFunction(configs, "USD", "BLOOMBERG");

    final Set<Currency> volCubeCurrencies = BloombergSwaptionVolatilityCubeInstrumentProvider.BLOOMBERG.getAllCurrencies();
    for (final Currency currency : volCubeCurrencies) {
      addVolatilityCubeFunction(configs, currency.getCode(), BloombergVolatilityCubeDefinitionSource.DEFINITION_NAME);
    }

    addNewVolatilityCubeFunction(configs);
    addForwardSwapCurveFunction(configs);
  }

  private void addYieldCurveFunction(final List<FunctionConfiguration> configs, final String currency, final String curveName) {
    configs.add(new ParameterizedFunctionConfiguration(YieldCurveMarketDataFunction.class.getName(), Arrays.asList(currency, curveName)));
    configs.add(new ParameterizedFunctionConfiguration(YieldCurveInterpolatingFunction.class.getName(), Arrays.asList(currency, curveName)));
    configs.add(new ParameterizedFunctionConfiguration(YieldCurveSpecificationFunction.class.getName(), Arrays.asList(currency, curveName)));
  }

  private void addFXForwardCurveFunction(final List<FunctionConfiguration> configs) {
    configs.add(new StaticFunctionConfiguration(FXForwardCurveFromMarketQuotesFunction.class.getName()));
    configs.add(new ParameterizedFunctionConfiguration(FXForwardCurveFromMarketQuotesDefaults.class.getName(),
        Arrays.asList("DoubleQuadratic", "LinearExtrapolator", "FlatExtrapolator")));
    configs.add(new StaticFunctionConfiguration(FXForwardCurveFromYieldCurveFunction.class.getName()));
    configs.add(new ParameterizedFunctionConfiguration(FXForwardCurveFromYieldCurveDefaultPropertiesFunction.class.getName(), Arrays.asList("FUNDING-FUNDING", "FUNDING", "FUNDING")));
  }

  private void addForwardSwapCurveFunction(final List<FunctionConfiguration> configs) {
    configs.add(new StaticFunctionConfiguration(ForwardSwapCurveMarketDataFunction.class.getName()));
    configs.add(new StaticFunctionConfiguration(ForwardSwapCurveFromMarketQuotesFunction.class.getName()));
    configs.add(new ParameterizedFunctionConfiguration(ForwardSwapCurveFromMarketQuotesDefaults.class.getName(), Arrays.asList("DoubleQuadratic", "LinearExtrapolator", "FlatExtrapolator")));
  }
  private void addFutureCurveFunction(final List<FunctionConfiguration> configs) {
    configs.add(new StaticFunctionConfiguration(FuturePriceCurveFunction.class.getName()));
  }
  private void addVolatilityCubeFunction(final List<FunctionConfiguration> configs, final String... parameters) {
    addVolatilityCubeFunction(configs, Arrays.asList(parameters));
  }

  private void addVolatilityCubeFunction(final List<FunctionConfiguration> configs, final List<String> parameters) {
    if (parameters.size() != 2) {
      throw new IllegalArgumentException();
    }

    configs.add(new ParameterizedFunctionConfiguration(VolatilityCubeFunction.class.getName(), parameters));
    configs.add(new ParameterizedFunctionConfiguration(VolatilityCubeMarketDataFunction.class.getName(), parameters));
  }

  private void addNewVolatilityCubeFunction(final List<FunctionConfiguration> configs) {
    configs.add(new StaticFunctionConfiguration(RawSwaptionVolatilityCubeDataFunction.class.getName()));
    configs.add(new StaticFunctionConfiguration(SABRNonLinearSwaptionVolatilityCubeFittingFunctionNew.class.getName()));
    final List<String> defaults = Arrays.asList("0.05", "0.5", "0.7", "0.3", "false", "true", "false", "false", "0.001", "Linear", "FlatExtrapolator", "Linear", "FlatExtrapolator",
        "ForwardSwapQuotes", "DoubleQuadratic", "LinearExtrapolator", "FlatExtrapolator");
    configs.add(new ParameterizedFunctionConfiguration(SABRNonLinearSwaptionVolatilityCubeFittingDefaults.class.getName(), defaults));
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
