/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Simple implementation of {@link ViewComputationResultModel}.
 */
public class InMemoryViewComputationResultModel extends InMemoryViewResultModel implements ViewComputationResultModel {

  private static final long serialVersionUID = 1L;

  private final Map<ValueSpecification, ComputedValue> _allMarketData = new HashMap<ValueSpecification, ComputedValue>();
  private final Map<ValueSpecification, Set<ValueRequirement>> _specToRequirementsMap = new HashMap<ValueSpecification, Set<ValueRequirement>>();

  /**
   * Adds a market data value, replacing any previous item with the same value specification.
   * 
   * @param marketData  the market data value, not null
   */
  public void addMarketData(ComputedValue marketData) {
    _allMarketData.put(marketData.getSpecification(), marketData);
  }

  @Override
  public Set<ComputedValue> getAllMarketData() {
    return new HashSet<ComputedValue>(_allMarketData.values());
  }

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

  public InMemoryViewComputationResultModel addRequirements(Map<ValueSpecification, Set<ValueRequirement>> specifications) {
    synchronized (_specToRequirementsMap) {
      for (ValueSpecification specification : specifications.keySet()) {
        Set<ValueRequirement> requirements = _specToRequirementsMap.get(specification);
        if (requirements == null) {
          requirements = new HashSet<ValueRequirement>();
          _specToRequirementsMap.put(specification, requirements);
        }
        requirements.addAll(specifications.get(specification));
      }
    }
    return this;
  }

  @Override
  public Map<ValueSpecification, Set<ValueRequirement>> getRequirementToSpecificationMapping() {
    //warning the contained sets are still mutable
    return Collections.unmodifiableMap(_specToRequirementsMap);
  }

}
