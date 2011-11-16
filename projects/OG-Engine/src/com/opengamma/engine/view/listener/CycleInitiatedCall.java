/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.listener;

import com.google.common.base.Function;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calc.ViewCycle;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;

import java.util.Map;
import java.util.Set;

/**
 * Represents a call to {@link com.opengamma.engine.view.listener.ViewResultListener#cycleInitiated(com.opengamma.engine.view.calc.ViewCycle)}
 */
public class CycleInitiatedCall implements Function<ViewResultListener, Object> {

  private ViewCycleExecutionOptions _viewCycleExecutionOptions;
  private Map<String, Map<ValueSpecification, Set<ValueRequirement>>> _specificationToRequirementMapping;

  public CycleInitiatedCall(ViewCycleExecutionOptions viewCycleExecutionOptions, Map<String, Map<ValueSpecification, Set<ValueRequirement>>> specificationToRequirementMapping) {
    update(viewCycleExecutionOptions, specificationToRequirementMapping);
  }

  public void update(ViewCycleExecutionOptions viewCycleExecutionOptions, Map<String, Map<ValueSpecification, Set<ValueRequirement>>> specificationToRequirementMapping) {
    _viewCycleExecutionOptions = viewCycleExecutionOptions;
    _specificationToRequirementMapping = specificationToRequirementMapping;
  }
    
  public ViewCycleExecutionOptions getViewCycleExecutionOptions() {
    return _viewCycleExecutionOptions;
  }

  public Map<String, Map<ValueSpecification, Set<ValueRequirement>>> getSpecificationToRequirementMapping() {
    return _specificationToRequirementMapping;
  }

  @Override
  public Object apply(ViewResultListener listener) {
    listener.cycleInitiated(getViewCycleExecutionOptions(), getSpecificationToRequirementMapping());
    return null;
  }

}
