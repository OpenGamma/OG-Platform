/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;

/**
 * Simple implementation of {@link ViewComputationResultModel}.
 */
public class InMemoryViewComputationResultModel extends InMemoryViewResultModel implements ViewComputationResultModel {

  private static final long serialVersionUID = 1L;

  private final Map<ValueSpecification, ComputedValue> _allMarketData = new HashMap<ValueSpecification, ComputedValue>();
  /**
   * @deprecated used to support a deprecated method on {@link ViewComputationResultModel} only
   */
  @Deprecated
  private final Map<ValueSpecification, Set<ValueRequirement>> _specToRequirementsMap = new HashMap<ValueSpecification, Set<ValueRequirement>>();

  /**
   * Adds a market data value, replacing any previous item with the same value specification.
   * 
   * @param marketData the market data value, not null
   */
  public void addMarketData(ComputedValue marketData) {
    _allMarketData.put(marketData.getSpecification(), marketData);
  }

  @Override
  public Set<ComputedValue> getAllMarketData() {
    return new HashSet<ComputedValue>(_allMarketData.values());
  }

  /**
   * @param requirement the requirement, not null
   * @param specification the specification, not null
   * @return this object
   * @deprecated used to support a deprecated method on {@link ViewComputationResultModel} only
   */
  @Deprecated
  public InMemoryViewComputationResultModel addRequirement(ValueRequirement requirement, ValueSpecification specification) {
    synchronized (_specToRequirementsMap) {
      Set<ValueRequirement> requirements = _specToRequirementsMap.get(specification);
      if (requirements == null) {
        requirements = new HashSet<ValueRequirement>();
        _specToRequirementsMap.put(specification, requirements);
      }
      requirements.add(requirement);
    }
    return this;
  }

  /**
   * @param newRequirements the requirements the specification satisfies, not null
   * @param specification the specification, not null
   * @return this object
   * @deprecated used to support a deprecated method on {@link ViewComputationResultModel} only
   */
  @Deprecated
  public InMemoryViewComputationResultModel addRequirements(Set<ValueRequirement> newRequirements, ValueSpecification specification) {
    synchronized (_specToRequirementsMap) {
      Set<ValueRequirement> requirements = _specToRequirementsMap.get(specification);
      if (requirements == null) {
        requirements = new HashSet<ValueRequirement>();
        _specToRequirementsMap.put(specification, requirements);
      }
      requirements.addAll(newRequirements);
    }
    return this;
  }

  /**
   * @param specifications the map of specifications to the requesting requirements, not null and not containing nulls
   * @return this object
   * @deprecated used to support a deprecated method on {@link ViewComputationResultModel} only
   */
  @Deprecated
  public InMemoryViewComputationResultModel addRequirements(Map<ValueSpecification, Set<ValueRequirement>> specifications) {
    synchronized (_specToRequirementsMap) {
      for (ValueSpecification specification : specifications.keySet()) {
        addRequirements(specifications.get(specification), specification);
      }
    }
    return this;
  }

  /**
   * @return the map
   * @deprecated see {@link ViewComputationResultModel}
   */
  @Override
  @Deprecated
  public Map<ValueSpecification, Set<ValueRequirement>> getRequirementToSpecificationMapping() {
    //warning the contained sets are still mutable
    return Collections.unmodifiableMap(_specToRequirementsMap);
  }

}
