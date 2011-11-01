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
 *
 */
public class InMemoryViewComputationResultModel extends InMemoryViewResultModel implements ViewComputationResultModel {

  private static final long serialVersionUID = 1L;

  private final Set<ComputedValue> _allMarketData = new HashSet<ComputedValue>();

  private final Map<ValueSpecification, Set<ValueRequirement>> _specificationsWithRequirements = new HashMap<ValueSpecification, Set<ValueRequirement>>();

  public void addMarketData(ComputedValue marketData) {
    _allMarketData.add(marketData);
  }

  public Set<ComputedValue> getAllMarketData() {
    return Collections.unmodifiableSet(_allMarketData);
  }

  public InMemoryViewComputationResultModel clearRequirements() {
    this._specificationsWithRequirements.clear();
    return this;
  }

  public InMemoryViewComputationResultModel addRequirement(ValueRequirement requirement, ValueSpecification specification) {
    synchronized (_specificationsWithRequirements) {
      Set<ValueRequirement> requirements = _specificationsWithRequirements.get(specification);
      if (requirements == null) {
        requirements = new HashSet<ValueRequirement>();
        _specificationsWithRequirements.put(specification, requirements);
      }
      requirements.add(requirement);
    }
    return this;
  }

  public InMemoryViewComputationResultModel addRequirements(Map<ValueSpecification, Set<ValueRequirement>> specifications) {
    synchronized (_specificationsWithRequirements) {
      for (ValueSpecification specification : specifications.keySet()) {
        Set<ValueRequirement> requirements = _specificationsWithRequirements.get(specification);
        if (requirements == null) {
          requirements = new HashSet<ValueRequirement>();
          _specificationsWithRequirements.put(specification, requirements);
        }
        requirements.addAll(specifications.get(specification));
      }
    }
    return this;
  }

  @Override
  public Map<ValueSpecification, Set<ValueRequirement>> getRequirementToSpecificationMapping() {
    //warning the contained sets are still mutable
    return Collections.unmodifiableMap(_specificationsWithRequirements);
  }

}
