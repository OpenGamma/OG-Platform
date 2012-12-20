/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.test.CalculationNodeUtils;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.test.TestCalculationNode;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.engine.view.cache.ViewComputationCache;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.async.AsynchronousHandleExecution;
import com.opengamma.util.log.LogBridge;
import com.opengamma.util.log.LogEvent;
import com.opengamma.util.log.LogLevel;
import com.opengamma.util.log.SimpleLogEvent;
import com.opengamma.util.log.ThreadLocalLogEventListener;

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
    
    ExecutionLog executionLog = resultItem.getExecutionLog();
    assertNotNull(executionLog);
    assertEquals(MissingInputException.class.getName(), executionLog.getExceptionClass());
    assertEquals("Unable to execute because of 1 missing input(s)", executionLog.getExceptionMessage());
    assertNull(executionLog.getExceptionStackTrace());
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
  
  //-------------------------------------------------------------------------
  public void testLogIndicators() throws Exception {
    MockFunction mockFunction = getMockLoggingFunction();
    ThreadLocalLogEventListener logEventListener = new ThreadLocalLogEventListener();
    TestCalculationNode calcNode = new TestCalculationNode(logEventListener); 
    CalculationNodeUtils.configureTestCalcNode(calcNode, mockFunction);
    CalculationJob calcJob = CalculationNodeUtils.getCalculationJob(mockFunction, ExecutionLogMode.INDICATORS);
    CalculationJobResultItem resultItemLogIndicators = getResultWithLogging(mockFunction, logEventListener, calcNode, calcJob);
    
    ExecutionLog executionLog = resultItemLogIndicators.getExecutionLog();
    assertNotNull(executionLog);
    assertTrue(executionLog.hasWarn());
    assertFalse(executionLog.hasError());
    assertFalse(executionLog.hasInfo());
    
    assertNull(executionLog.getEvents());
    
    assertNull(executionLog.getExceptionClass());
    assertNull(executionLog.getExceptionMessage());
    assertNull(executionLog.getExceptionStackTrace());
  }
  
  public void testLogFull() throws Exception {
    MockFunction mockFunction = getMockLoggingFunction();
    ThreadLocalLogEventListener logEventListener = new ThreadLocalLogEventListener();
    TestCalculationNode calcNode = new TestCalculationNode(logEventListener); 
    CalculationNodeUtils.configureTestCalcNode(calcNode, mockFunction);
    CalculationJob calcJob = CalculationNodeUtils.getCalculationJob(mockFunction, ExecutionLogMode.FULL);
    CalculationJobResultItem resultItemLogIndicators = getResultWithLogging(mockFunction, logEventListener, calcNode, calcJob);
    
    ExecutionLog executionLog = resultItemLogIndicators.getExecutionLog();
    assertNotNull(executionLog);
    assertTrue(executionLog.hasWarn());
    assertFalse(executionLog.hasError());
    assertFalse(executionLog.hasInfo());
    
    assertNotNull(executionLog.getEvents());
    assertEquals(1, executionLog.getEvents().size());
    LogEvent event = Iterables.getOnlyElement(executionLog.getEvents());
    assertNotNull(event);
    assertEquals(LogLevel.WARN, event.getLevel());
    assertEquals("Warning during execution", event.getMessage());
    
    assertNull(executionLog.getExceptionClass());
    assertNull(executionLog.getExceptionMessage());
    assertNull(executionLog.getExceptionStackTrace());
  }

  private CalculationJobResultItem getResultWithLogging(MockFunction mockFunction, ThreadLocalLogEventListener logEventListener, TestCalculationNode calcNode, CalculationJob calcJob)
      throws AsynchronousHandleExecution, AsynchronousExecution {
    LogBridge.getInstance().addListener(logEventListener);
    CalculationJobResult jobResult;
    try {
      jobResult = calcNode.executeJob(calcJob);
    } finally {
      LogBridge.getInstance().removeListener(logEventListener);
    }
    
    assertNotNull(jobResult);
    assertEquals(1, jobResult.getResultItems().size());
    CalculationJobResultItem resultItem = jobResult.getResultItems().get(0);
    assertEquals(InvocationResult.SUCCESS, resultItem.getResult());
    ViewComputationCache cache = calcNode.getCache(calcJob.getSpecification());
    assertEquals("Result", cache.getValue(mockFunction.getResultSpec()));
    return resultItem;
  }
  
  private MockFunction getMockLoggingFunction() {
    ComputationTarget target = new ComputationTarget(ComputationTargetType.PRIMITIVE, "USD");
    MockFunction fn = new MockLoggingFunction(MockFunction.UNIQUE_ID, target);
    fn.addResult(new ValueRequirement("OUTPUT", target.toSpecification()), "Result");
    return fn;
  }
  
  private class MockLoggingFunction extends MockFunction {

    public MockLoggingFunction(String uniqueId, ComputationTarget target) {
      super(uniqueId, target);
    }

    @Override
    public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
      LogEvent logEvent = new SimpleLogEvent(LogLevel.WARN, "Warning during execution");
      LogBridge.getInstance().log(logEvent);
      return super.execute(executionContext, inputs, target, desiredValues);
    }
    
  }

}
