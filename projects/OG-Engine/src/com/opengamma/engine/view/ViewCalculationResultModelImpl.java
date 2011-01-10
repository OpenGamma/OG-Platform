/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.Map;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.value.ComputedValue;

/**
 * A simple implementation of the calculation result model.
 */
public class ViewCalculationResultModelImpl extends AbstractResultModel<ComputationTargetSpecification> implements ViewCalculationResultModel {

  private DependencyGraphBuilder _dependencyGraphModel;

  public void setDependencyGraphModel(DependencyGraphBuilder dependencyGraphModel) {
    _dependencyGraphModel = dependencyGraphModel;
  }

  public DependencyGraphBuilder getDependencyGraphModel() {
    return _dependencyGraphModel;
  }
  
  public Map<String, ComputedValue> getValues(final ComputationTargetSpecification target) {
    return super.getValues(target);    
  }
  
  // REVIEW Andrew 2010-09-14 -- Do we need the dependency graph model ?

  @Override
  public Collection<ComputationTargetSpecification> getAllTargets() {
    return getKeys();
  }

}
