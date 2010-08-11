/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.MockFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.ViewComputationCache;

/**
 * 
 */
public class AbstractCalculationNodeTest {
  
  public static String CALC_CONF_NAME = "Default";

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
  
  public static CalculationJob getCalculationJob(MockFunction function,
      ResultWriter resultWriter) {
    
    long iterationTimestamp = System.currentTimeMillis();
    CalculationJobSpecification jobSpec = new CalculationJobSpecification("view", CALC_CONF_NAME, iterationTimestamp, 1L);
    
    CalculationJobItem calculationJobItem = new CalculationJobItem(
        function.getUniqueIdentifier(), 
        function.getTarget().toSpecification(), 
        Sets.newHashSet(new ValueSpecification(function.getRequirement())), 
        Sets.newHashSet(function.getResultSpec().getRequirementSpecification()),
        true);
    CalculationJob calcJob = new CalculationJob(jobSpec, Collections.singletonList(calculationJobItem), resultWriter);
    return calcJob;
  }
  
  @Test
  public void mockFunctionInvocationOneInputMissing() {
    
    MockFunction mockFunction = getMockFunction();
    TestCalculationNode calcNode = getTestCalcNode(mockFunction);
    TestResultWriter result = new TestResultWriter ();
    CalculationJob calcJob = getCalculationJob(mockFunction, result);
    
    long startTime = System.nanoTime();
    calcNode.executeJob(calcJob);
    CalculationJobResult jobResult = result.getResult ();
    long endTime = System.nanoTime();
    assertNotNull(jobResult);
    assertTrue(jobResult.getDuration() >= 0);
    assertTrue(endTime - startTime >= jobResult.getDuration());
    assertEquals(1, jobResult.getResultItems().size());
    CalculationJobResultItem resultItem = jobResult.getResultItems().get(0);
    assertEquals(calcJob.getJobItems().get(0), resultItem.getItem());
    assertEquals(InvocationResult.ERROR, resultItem.getResult());
  }

  @Test
  public void mockFunctionInvocationOneInputOneOutput() {
    MockFunction mockFunction = getMockFunction();
    TestCalculationNode calcNode = getTestCalcNode(mockFunction);
    TestResultWriter result = new TestResultWriter ();
    CalculationJob calcJob = getCalculationJob(mockFunction, result);
    
    ValueSpecification inputSpec = new ValueSpecification(mockFunction.getRequirement());
    ComputedValue inputValue = new ComputedValue(inputSpec, "Just an input object");
    
    ViewComputationCache cache = calcNode.getCache(calcJob.getSpecification());
    cache.putValue(inputValue);
    
    calcNode.executeJob(calcJob);
    CalculationJobResult jobResult = result.getResult ();
    assertNotNull(jobResult);
    assertEquals(1, jobResult.getResultItems().size());
    CalculationJobResultItem resultItem = jobResult.getResultItems().get(0);
    assertEquals(calcJob.getJobItems().get(0), resultItem.getItem());
    assertEquals(InvocationResult.SUCCESS, resultItem.getResult());
    assertEquals("Nothing we care about", cache.getValue(mockFunction.getResultSpec()));
  }

}
