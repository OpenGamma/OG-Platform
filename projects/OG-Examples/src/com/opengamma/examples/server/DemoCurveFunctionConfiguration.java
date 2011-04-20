/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.examples.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.function.config.FunctionConfiguration;
import com.opengamma.engine.function.config.ParameterizedFunctionConfiguration;
import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.financial.analytics.ircurve.MarketInstrumentImpliedYieldCurveFunction;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.financial.convention.InMemoryConventionBundleMaster;
import com.opengamma.id.Identifier;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Creates function repository configuration for curve supplying functions.
 * 
 * Note [PLAT-1094] - the functions should really be built by scanning the curves and currencies available. 
 */
public class DemoCurveFunctionConfiguration extends SingletonFactoryBean<RepositoryConfigurationSource> {

  private static final Logger s_logger = LoggerFactory.getLogger(DemoCurveFunctionConfiguration.class);

  private ConfigMaster _configMaster;
  private ConventionBundleSource _conventionBundleSource;

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
      
      final ConfigSearchResult<YieldCurveDefinition> searchResult = _configMaster.search(searchRequest);
      final Map<String, Set<String>> currencyToCurves = new HashMap<String, Set<String>>();
      for (ConfigDocument<YieldCurveDefinition> configDocument : searchResult.getDocuments()) {
        final String documentName = configDocument.getName();
        final int underscore = documentName.lastIndexOf('_');
        if (underscore <= 0) {
          continue;
        }
        final String curveName = documentName.substring(0, underscore);
        final String currencyISO = documentName.substring(underscore + 1);
        s_logger.debug("Found {} curve for {}", curveName, currencyISO);
        if (!currencyToCurves.containsKey(currencyISO)) {
          currencyToCurves.put(currencyISO, new HashSet<String>());
        }
        currencyToCurves.get(currencyISO).add(curveName);
      }
      for (Map.Entry<String, Set<String>> currencyCurves : currencyToCurves.entrySet()) {
        final String currencyISO = currencyCurves.getKey();
        final Set<String> curveNames = currencyCurves.getValue();
        if (_conventionBundleSource.getConventionBundle(Identifier.of(InMemoryConventionBundleMaster.SIMPLE_NAME_SCHEME, currencyISO + "_SWAP")) != null) {
          if (curveNames.contains("SINGLE")) {
            configs.add(new ParameterizedFunctionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class.getName(), Arrays.asList(currencyISO, "SINGLE")));
          }
          if (curveNames.contains("FUNDING") && curveNames.contains("FORWARD")) {
            configs.add(new ParameterizedFunctionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class.getName(), Arrays.asList(currencyISO, "FUNDING", "FORWARD")));
          }
        } else {
          s_logger.debug("Ignoring {} as no swap convention required by MarketInstrumentImpliedYieldCurveFunction", currencyISO);
        }
      }
    } else {
      // [PLAT-1094] This is the wrong approach and should be disposed of at the earliest opportunity
      s_logger.warn("[PLAT-1094] Using hardcoded curve definitions");
      configs.add(new ParameterizedFunctionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class.getName(), Arrays.asList("USD", "FUNDING", "FORWARD")));
      configs.add(new ParameterizedFunctionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class.getName(), Arrays.asList("GBP", "FUNDING", "FORWARD")));
      configs.add(new ParameterizedFunctionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class.getName(), Arrays.asList("USD", "SINGLE")));
      configs.add(new ParameterizedFunctionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class.getName(), Arrays.asList("GBP", "SINGLE")));
    }

    // The curves below are for testing curve names as value requirements - they might not be particularly useful
    configs.add(new ParameterizedFunctionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class.getName(), Arrays.asList("USD", "SWAP_ONLY_NO3YR", "SWAP_ONLY_NO3YR")));
    configs.add(new ParameterizedFunctionConfiguration(MarketInstrumentImpliedYieldCurveFunction.class.getName(), Arrays.asList("USD", "SWAP_ONLY", "SWAP_ONLY")));

    s_logger.info("Created repository configuration with {} curve provider functions", configs.size());
    return new RepositoryConfiguration(configs);
  }

  public RepositoryConfigurationSource constructRepositoryConfigurationSource() {
    return new RepositoryConfigurationSource() {
      private final RepositoryConfiguration _config = constructRepositoryConfiguration();

      @Override
      public RepositoryConfiguration getRepositoryConfiguration() {
        return _config;
      }
    };
  }

  @Override
  protected RepositoryConfigurationSource createObject() {
    return constructRepositoryConfigurationSource();
  }

}
