/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.management;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;

/**
 * Contains mappings between {@link ValueRequirement}s and {@link ValueSpecification}s for a compiled view definition. These mappings can be large and are used by both the primitives and portfolio
 * grids so it makes sense to share them.
 */
public class ValueMappings {

  private static final Logger s_logger = LoggerFactory.getLogger(ValueMappings.class);
  
  private final class ConfigurationData {
    
    private final Map<ValueRequirement, ValueSpecification> _reqsToSpecs = Maps.newHashMap();

    public ConfigurationData(final CompiledViewCalculationConfiguration compiledConfig) {
      Map<ValueSpecification, Set<ValueRequirement>> terminalOutputs = compiledConfig.getTerminalOutputSpecifications();
      for (Map.Entry<ValueSpecification, Set<ValueRequirement>> entry : terminalOutputs.entrySet()) {
        Set<ValueRequirement> requirements = entry.getValue();
        if (requirements == null) {
          s_logger.error("Unexpected set of null requirements in terminal outputs map from " + entry.getKey() + ". This is a bug in incremental dependency graph compilation.");
          continue;
        }
        for (ValueRequirement valueRequirement : requirements) {
          _reqsToSpecs.put(createRequirement(valueRequirement), entry.getKey());
        }
      }
    }

    public ValueSpecification getValueSpecification(final ValueRequirement valueRequirement) {
      return _reqsToSpecs.get(createRequirement(valueRequirement));
    }
  }

  /**
   * Subclasses of ValueMappings need to alter the ValueRequirement, for example
   * UnversionedValueMappings strips out version from the unique id
   * @param valueRequirement in this case it is returned unaltered
   * @return valueRequirement
   */
  protected ValueRequirement createRequirement(ValueRequirement valueRequirement) {
    return valueRequirement;
  }

  /**
   * Data held for each view calculation configuration.
   */
  private final Map<String, ConfigurationData> _configurations;

  /**
   * Creates an instance with no mappings.
   */
  public ValueMappings() {
    _configurations = Collections.emptyMap();
  }

  public ValueMappings(CompiledViewDefinition compiledViewDef) {
    _configurations = Maps.newHashMap();
    for (ViewCalculationConfiguration calcConfig : compiledViewDef.getViewDefinition().getAllCalculationConfigurations()) {
      String configName = calcConfig.getName();
      CompiledViewCalculationConfiguration compiledConfig = compiledViewDef.getCompiledCalculationConfiguration(configName);
      // store the mappings from outputs to requirements for each calc config
      _configurations.put(configName, new ConfigurationData(compiledConfig));
    }
  }

  /**
   * Returns the {@link ValueSpecification} for a {@link ValueRequirement} in a particular calculation configuration.
   * 
   * @param calcConfigName The name of the calculation configuration
   * @param valueReq The requirement
   * @return The specification or null if there isn't one for the specified requirement and config
   */
  public ValueSpecification getValueSpecification(String calcConfigName, ValueRequirement valueReq) {
    final ConfigurationData configuration = _configurations.get(calcConfigName);
    if (configuration != null) {
      return configuration.getValueSpecification(valueReq);
    } else {
      return null;
    }
  }

}
