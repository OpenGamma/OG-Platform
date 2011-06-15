/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.Map;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraphBuilder;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.util.tuple.Pair;

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
  
  // REVIEW Andrew 2010-09-14 -- Do we need the dependency graph model ?

  @Override
  public Map<Pair<String, ValueProperties>, ComputedValue> getValues(final ComputationTargetSpecification target) {
    return super.getValuesByName(target);
  }
  
  @Override
  public Collection<ComputedValue> getAllValues(final ComputationTargetSpecification target) {
    return super.getAllValues(target);
  }

  @Override
  public Collection<ComputationTargetSpecification> getAllTargets() {
    return getKeys();
  }

}
