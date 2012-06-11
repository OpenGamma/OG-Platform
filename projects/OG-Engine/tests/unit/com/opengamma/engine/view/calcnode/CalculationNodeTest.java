/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.Test;

import com.opengamma.engine.test.CalculationNodeUtils;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.test.TestCalculationNode;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.ViewComputationCache;

/**
 * Tests the {@link SimpleCalculationNode} class. Note the name so that Clover doesn't ignore it.
 */
@Test
public class CalculationNodeTest {
  
  public void mockFunctionInvocationOneInputMissing() throws Exception {
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
    assertEquals(InvocationResult.MISSING_INPUTS, resultItem.getResult());
  }

  public void mockFunctionInvocationOneInputOneOutput() throws Exception {
    MockFunction mockFunction = CalculationNodeUtils.getMockFunction();
    TestCalculationNode calcNode = CalculationNodeUtils.getTestCalcNode(mockFunction);
    CalculationJob calcJob = CalculationNodeUtils.getCalculationJob(mockFunction);
    
    ValueSpecification inputSpec = mockFunction.getRequirements().iterator ().next ();
    ComputedValue inputValue = new ComputedValue(inputSpec, "Just an input object");
    
    ViewComputationCache cache = calcNode.getCache(calcJob.getSpecification());
    cache.putSharedValue(inputValue);
    
    CalculationJobResult jobResult = calcNode.executeJob(calcJob);
    assertNotNull(jobResult);
    assertEquals(1, jobResult.getResultItems().size());
    CalculationJobResultItem resultItem = jobResult.getResultItems().get(0);
    assertEquals(InvocationResult.SUCCESS, resultItem.getResult());
    assertEquals("Nothing we care about", cache.getValue(mockFunction.getResultSpec()));
  }

}
