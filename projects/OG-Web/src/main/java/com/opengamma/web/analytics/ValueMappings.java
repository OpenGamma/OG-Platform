/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains mappings between {@link ValueRequirement}s and {@link ValueSpecification}s for a compiled view definition.
 * These mappings can be large and are used by both the primitives and portfolio grids so it makes sense to share
 * them.
 */
/* package */ class ValueMappings {

  /** Mappings of requirements to specifications. */
  private final Map<ValueRequirementKey, ValueSpecification> _reqsToSpecs;

  /**
   * Creates an instance with no mappings.
   */
  /* package */ ValueMappings() {
    _reqsToSpecs = Collections.emptyMap();
  }

  /* package */ ValueMappings(CompiledViewDefinition compiledViewDef) {
    _reqsToSpecs = Maps.newHashMap();
    for (ViewCalculationConfiguration calcConfig : compiledViewDef.getViewDefinition().getAllCalculationConfigurations()) {
      String configName = calcConfig.getName();
      CompiledViewCalculationConfiguration compiledConfig = compiledViewDef.getCompiledCalculationConfiguration(configName);
      // store the mappings from outputs to requirements for each calc config
      Map<ValueSpecification, Set<ValueRequirement>> terminalOutputs = compiledConfig.getTerminalOutputSpecifications();
      for (Map.Entry<ValueSpecification, Set<ValueRequirement>> entry : terminalOutputs.entrySet()) {
        for (ValueRequirement valueRequirement : entry.getValue()) {
          _reqsToSpecs.put(new ValueRequirementKey(valueRequirement, configName), entry.getKey());
        }
      }
    }
  }

  /**
   * Returns the {@link ValueSpecification} for a {@link ValueRequirement} in a particular calculation configuration.
   * @param calcConfigName The name of the calculation configuration
   * @param valueReq The requirement
   * @return The specification or {@code null} if there isn't one for the specified requirement and config
   */
  /* package */ ValueSpecification getValueSpecification(String calcConfigName, ValueRequirement valueReq) {
    ValueRequirementKey requirementKey = new ValueRequirementKey(valueReq, calcConfigName);
    return _reqsToSpecs.get(requirementKey);
  }

  /** Map key that consists of a {@link ValueRequirement} and corresponding calculation configuration name. */
    private static class ValueRequirementKey {

    private final ValueRequirement _valueRequirement;
    private final String _calcConfigName;

    /* package */ ValueRequirementKey(ValueRequirement valueRequirement, String calcConfigName) {
      ArgumentChecker.notNull(valueRequirement, "valueRequirement");
      ArgumentChecker.notNull(calcConfigName, "calcConfigName");
      _valueRequirement = valueRequirement;
      _calcConfigName = calcConfigName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ValueRequirementKey that = (ValueRequirementKey) o;

      if (!_calcConfigName.equals(that._calcConfigName)) {
        return false;
      }
      return _valueRequirement.equals(that._valueRequirement);
    }

    @Override
    public int hashCode() {
      int result = _valueRequirement.hashCode();
      result = 31 * result + _calcConfigName.hashCode();
      return result;
    }

    @Override
    public String toString() {
      return "ValueRequirementKey [" +
          "_valueRequirement=" + _valueRequirement +
          ", _calcConfigName='" + _calcConfigName + '\'' +
          "]";
    }
  }
}
