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
  
  public FunctionConfigurationSource aggregate(String configDefinitionName) {
    ArgumentChecker.notNull(configDefinitionName, "configDefinitionName");
    FunctionConfigurationDefinition configDefinition = _configSource.getSingle(FunctionConfigurationDefinition.class, configDefinitionName, VersionCorrection.LATEST);
    FunctionConfigurationBundle bundle = new FunctionConfigurationBundle();
    if (configDefinition != null) {
      bundle = functionAggregator(configDefinition);
    }
    return new SimpleFunctionConfigurationSource(bundle);
  }

  private FunctionConfigurationBundle functionAggregator(final FunctionConfigurationDefinition configDefinition) {
    final Set<FunctionConfiguration> functions = Sets.newHashSet();
    final List<String> visitedConfigs = Lists.newArrayList();
    functionAggregatorHelper(functions, visitedConfigs, configDefinition);
    return new FunctionConfigurationBundle(functions);
  }

  private void functionAggregatorHelper(final Set<FunctionConfiguration> functions, final List<String> visitedConfigs, final FunctionConfigurationDefinition configDefinition) {
    visitedConfigs.add(configDefinition.getName());
    functions.addAll(configDefinition.getStaticFunctions());
    functions.addAll(configDefinition.getParameterizedFunctions());
    
    for (String configDefinitionName : configDefinition.getFunctionConfigurationDefinitions()) {
      if (!visitedConfigs.contains(configDefinitionName)) {
        FunctionConfigurationDefinition linkedConfig = _configSource.getSingle(FunctionConfigurationDefinition.class, configDefinitionName, VersionCorrection.LATEST);
        if (linkedConfig != null) {
          functionAggregatorHelper(functions, visitedConfigs, linkedConfig);
        }
      }
    }
  }
}
