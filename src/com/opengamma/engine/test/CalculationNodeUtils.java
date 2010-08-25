/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.Collections;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.value.ValueRequirement;
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
    return MockFunction.getMockFunction(target, output, new ValueRequirement("INPUT", target.toSpecification()));
  }
  
  public static CalculationJob getCalculationJob(MockFunction function) {
    
    long iterationTimestamp = System.currentTimeMillis();
    CalculationJobSpecification jobSpec = new CalculationJobSpecification("view", CALC_CONF_NAME, iterationTimestamp, 1L);
    
    CalculationJobItem calculationJobItem = new CalculationJobItem(
        function.getUniqueIdentifier(), 
        function.getDefaultParameters(),
        function.getTarget().toSpecification(), 
        function.getRequirements(), 
        function.getResultRequirements());
    CalculationJob calcJob = new CalculationJob(jobSpec, Collections.singletonList(calculationJobItem));
    return calcJob;
  }
  
}
