/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.engine.test.CalculationNodeUtils;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.test.TestCalculationNode;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.ViewComputationCache;

/**
 * 
 */
public class AbstractCalculationNodeTest {
  
  @Test
  public void mockFunctionInvocationOneInputMissing() {
    
    MockFunction mockFunction = CalculationNodeUtils.getMockFunction();
    TestCalculationNode calcNode = CalculationNodeUtils.getTestCalcNode(mockFunction);
    CalculationJob calcJob = CalculationNodeUtils.getCalculationJob(mockFunction);
    
    long startTime = System.nanoTime();
    CalculationJobResult jobResult = calcNode.executeJob(calcJob);
    long endTime = System.nanoTime();
    assertNotNull(jobResult);
    assertTrue(jobResult.getDuration() >= 0);
    assertTrue(endTime - startTime >= jobResult.getDuration());
    assertEquals(1, jobResult.getResultItems().size());
    CalculationJobResultItem resultItem = jobResult.getResultItems().get(0);
    assertEquals(calcJob.getJobItems().get(0), resultItem.getItem());
    assertEquals(InvocationResult.MISSING_INPUTS, resultItem.getResult());
  }

  @Test
  public void mockFunctionInvocationOneInputOneOutput() {
    MockFunction mockFunction = CalculationNodeUtils.getMockFunction();
    TestCalculationNode calcNode = CalculationNodeUtils.getTestCalcNode(mockFunction);
    CalculationJob calcJob = CalculationNodeUtils.getCalculationJob(mockFunction);
    
    ValueSpecification inputSpec = new ValueSpecification(mockFunction.getRequirement());
    ComputedValue inputValue = new ComputedValue(inputSpec, "Just an input object");
    
    ViewComputationCache cache = calcNode.getCache(calcJob.getSpecification());
    cache.putValue(inputValue);
    
    CalculationJobResult jobResult = calcNode.executeJob(calcJob);
    assertNotNull(jobResult);
    assertEquals(1, jobResult.getResultItems().size());
    CalculationJobResultItem resultItem = jobResult.getResultItems().get(0);
    assertEquals(calcJob.getJobItems().get(0), resultItem.getItem());
    assertEquals(InvocationResult.SUCCESS, resultItem.getResult());
    assertEquals("Nothing we care about", cache.getValue(mockFunction.getResultSpec()));
  }

}
