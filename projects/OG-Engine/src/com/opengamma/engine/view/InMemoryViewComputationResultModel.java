/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class InMemoryViewComputationResultModel extends InMemoryViewResultModel implements ViewComputationResultModel {
  
  private static final long serialVersionUID = 1L;
  
  private final Set<ComputedValue> _allMarketData = new HashSet<ComputedValue>();

  private final Map<ValueRequirement, ValueSpecification> _requirementToSpecificationMapping = new HashMap<ValueRequirement, ValueSpecification>();
  
  public void addMarketData(ComputedValue marketData) {
    _allMarketData.add(marketData);    
  }
  
  public Set<ComputedValue> getAllMarketData() {
    return Collections.unmodifiableSet(_allMarketData);
  }

  public void clearRequirements() {
    this._requirementToSpecificationMapping.clear();
  }

  public void addRequirement(ValueRequirement requirement, ValueSpecification specification) {
    this._requirementToSpecificationMapping.put(requirement, specification);
  }

  public void addRequirements(Map<ValueRequirement, ValueSpecification> requirements) {
    for (ValueRequirement requirement : requirements.keySet()) {
      this._requirementToSpecificationMapping.put(requirement, requirements.get(requirement));
    }
  }

  @Override
  public Map<ValueRequirement, ValueSpecification> getRequirementToSpecificationMapping() {
    return _requirementToSpecificationMapping;
  }

}
