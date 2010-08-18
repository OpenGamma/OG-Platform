/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.Collections;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calcnode.CalculationJob;
import com.opengamma.engine.view.calcnode.CalculationJobItem;
import com.opengamma.engine.view.calcnode.CalculationJobSpecification;

/**
 * 
 */
public class CalculationNodeUtils {

  /**
   * Default calculation configuration name
   */
  public static final String CALC_CONF_NAME = "Default";

  public static TestCalculationNode getTestCalcNode(MockFunction mockFunction) {
    TestCalculationNode calcNode = new TestCalculationNode();
    
    InMemoryFunctionRepository functionRepo = (InMemoryFunctionRepository) calcNode.getFunctionRepository();
    functionRepo.addFunction(mockFunction, mockFunction);
    
    return calcNode;
  }
  
  public static MockFunction getMockFunction() {
    ComputationTarget target = new ComputationTarget(ComputationTargetType.PRIMITIVE, "USD");
    return getMockFunction(target, "Nothing we care about");    
  }
  
  public static MockFunction getMockFunction(ComputationTarget target, Object output) {

    ValueRequirement inputReq = new ValueRequirement("INPUT", target.toSpecification());
    ValueRequirement outputReq = new ValueRequirement("OUTPUT", target.toSpecification());
    
    ValueSpecification outputSpec = new ValueSpecification(outputReq);
    ComputedValue outputValue = new ComputedValue(outputSpec, output);
    
    MockFunction fn = new MockFunction(
        target,
        Sets.newHashSet(inputReq),
        Sets.newHashSet(outputValue));
    return fn;
  }
  
  public static CalculationJob getCalculationJob(MockFunction function) {
    
    long iterationTimestamp = System.currentTimeMillis();
    CalculationJobSpecification jobSpec = new CalculationJobSpecification("view", CALC_CONF_NAME, iterationTimestamp, 1L);
    
    CalculationJobItem calculationJobItem = new CalculationJobItem(
        function.getUniqueIdentifier(), 
        function.getTarget().toSpecification(), 
        Sets.newHashSet(new ValueSpecification(function.getRequirement())), 
        Sets.newHashSet(function.getResultSpec().getRequirementSpecification()));
    CalculationJob calcJob = new CalculationJob(jobSpec, Collections.singletonList(calculationJobItem));
    return calcJob;
  }
  
}
