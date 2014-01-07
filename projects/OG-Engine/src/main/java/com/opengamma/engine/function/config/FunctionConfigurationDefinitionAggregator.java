/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.config;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Aggregate functions defined in a {@link FunctionConfigurationDefinition}
 */
public final class FunctionConfigurationDefinitionAggregator {

  private final ConfigSource _configSource;

  public FunctionConfigurationDefinitionAggregator(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _configSource = configSource;
  }

  /**
   * Loads the given definition, aggregating any referenced definitions.
   * 
   * @param configDefinitionName the definition to load, not null
   * @param version the configuration version/correction timestamp, not null
   * @return the configuration bundle, not null
   */
  public FunctionConfigurationBundle aggregate(final String configDefinitionName, final VersionCorrection version) {
    ArgumentChecker.notNull(configDefinitionName, "configDefinitionName");
    FunctionConfigurationDefinition configDefinition = _configSource.getSingle(FunctionConfigurationDefinition.class, configDefinitionName, version);
    FunctionConfigurationBundle bundle = new FunctionConfigurationBundle();
    if (configDefinition != null) {
      bundle = functionAggregator(configDefinition, version);
    }
    return bundle;
  }

  /**
   * Loads the given definition, aggregating any referenced definitions.
   * 
   * @param configDefinitionName the definition to load, not null
   * @return a static {@link FunctionConfigurationSource} that returns the definitions, not null
   * @deprecated The configuration returned will be static - make repeated calls to {@link #aggregate(String, VersionCorrection)} if you need to handle configuration changes at run-time
   */
  @Deprecated
  public FunctionConfigurationSource aggregate(final String configDefinitionName) {
    return new SimpleFunctionConfigurationSource(aggregate(configDefinitionName, VersionCorrection.LATEST));
  }

  private FunctionConfigurationBundle functionAggregator(final FunctionConfigurationDefinition configDefinition, final VersionCorrection version) {
    final Set<FunctionConfiguration> functions = Sets.newHashSet();
    final List<String> visitedConfigs = Lists.newArrayList();
    functionAggregatorHelper(functions, visitedConfigs, configDefinition, version);
    return new FunctionConfigurationBundle(functions);
  }

  private void functionAggregatorHelper(final Set<FunctionConfiguration> functions, final List<String> visitedConfigs, final FunctionConfigurationDefinition configDefinition,
      final VersionCorrection version) {
    visitedConfigs.add(configDefinition.getName());
    functions.addAll(configDefinition.getStaticFunctions());
    functions.addAll(configDefinition.getParameterizedFunctions());

    for (String configDefinitionName : configDefinition.getFunctionConfigurationDefinitions()) {
      if (!visitedConfigs.contains(configDefinitionName)) {
        FunctionConfigurationDefinition linkedConfig = _configSource.getSingle(FunctionConfigurationDefinition.class, configDefinitionName, version);
        if (linkedConfig != null) {
          functionAggregatorHelper(functions, visitedConfigs, linkedConfig, version);
        }
      }
    }
  }
}
