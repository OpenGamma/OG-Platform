/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.cache.ViewComputationCache;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.test.CalculationNodeUtils;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.test.TestCalculationNode;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.log.LogBridge;
import com.opengamma.util.log.LogEvent;
import com.opengamma.util.log.LogLevel;
import com.opengamma.util.log.SimpleLogEvent;
import com.opengamma.util.log.ThreadLocalLogEventListener;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

/**
 * Tests the {@link SimpleCalculationNode} class. Note the name so that Clover doesn't ignore it.
 */
@Test(groups = TestGroup.UNIT)
public class CalculationNodeTest {

  public void mockFunctionInvocationOneInputMissing() throws Exception {
    TestLifecycle.begin();
    try {
      final MockFunction mockFunction = CalculationNodeUtils.getMockFunction();
      final TestCalculationNode calcNode = CalculationNodeUtils.getTestCalcNode(mockFunction);
      TestLifecycle.register(calcNode);
      final CalculationJob calcJob = CalculationNodeUtils.getCalculationJob(mockFunction);

      final long startTime = System.nanoTime();
      final CalculationJobResult jobResult = calcNode.executeJob(calcJob);
      final long endTime = System.nanoTime();
      assertNotNull(jobResult);
      assertTrue(jobResult.getDuration() >= 0);
      assertTrue(endTime - startTime >= jobResult.getDuration());
      assertEquals(1, jobResult.getResultItems().size());
      final CalculationJobResultItem resultItem = jobResult.getResultItems().get(0);
      assertEquals(InvocationResult.MISSING_INPUTS, resultItem.getResult());

      final ExecutionLog executionLog = resultItem.getExecutionLog();
      assertNotNull(executionLog);
      assertEquals(MissingInputException.class.getName(), executionLog.getExceptionClass());
      assertEquals("Unable to execute because of 1 missing input(s)", executionLog.getExceptionMessage());
      assertNull(executionLog.getExceptionStackTrace());
    } finally {
      TestLifecycle.end();
    }
  }

  public void mockFunctionInvocationOneInputOneOutput() throws Exception {
    TestLifecycle.begin();
    try {
      final MockFunction mockFunction = CalculationNodeUtils.getMockFunction();
      final TestCalculationNode calcNode = CalculationNodeUtils.getTestCalcNode(mockFunction);
      TestLifecycle.register(calcNode);
      final CalculationJob calcJob = CalculationNodeUtils.getCalculationJob(mockFunction);
      final ValueSpecification inputSpec = CalculationNodeUtils.getMockFunctionInputs(mockFunction).iterator().next();
      final ComputedValue inputValue = new ComputedValue(inputSpec, "Just an input object");

      final ViewComputationCache cache = calcNode.getCache(calcJob.getSpecification());
      cache.putSharedValue(inputValue);

      final CalculationJobResult jobResult = calcNode.executeJob(calcJob);
      assertNotNull(jobResult);
      assertEquals(1, jobResult.getResultItems().size());
      final CalculationJobResultItem resultItem = jobResult.getResultItems().get(0);
      assertEquals(InvocationResult.SUCCESS, resultItem.getResult());
      assertEquals("Nothing we care about", cache.getValue(mockFunction.getResultSpec()));
    } finally {
      TestLifecycle.end();
    }
  }

  //-------------------------------------------------------------------------
  public void testLogIndicators() throws Exception {
    TestLifecycle.begin();
    try {
      final MockFunction mockFunction = getMockLoggingFunction();
      final ThreadLocalLogEventListener logEventListener = new ThreadLocalLogEventListener();
      final TestCalculationNode calcNode = new TestCalculationNode(logEventListener);
      TestLifecycle.register(calcNode);
      CalculationNodeUtils.configureTestCalcNode(calcNode, mockFunction);
      final CalculationJob calcJob = CalculationNodeUtils.getCalculationJob(mockFunction, ExecutionLogMode.INDICATORS);
      final CalculationJobResultItem resultItemLogIndicators = getResultWithLogging(mockFunction, logEventListener, calcNode, calcJob);

      final ExecutionLog executionLog = resultItemLogIndicators.getExecutionLog();
      assertNotNull(executionLog);
      assertTrue(executionLog.getLogLevels().contains(LogLevel.WARN));
      assertFalse(executionLog.getLogLevels().contains(LogLevel.ERROR));
      assertFalse(executionLog.getLogLevels().contains(LogLevel.INFO));

      assertNull(executionLog.getEvents());

      assertNull(executionLog.getExceptionClass());
      assertNull(executionLog.getExceptionMessage());
      assertNull(executionLog.getExceptionStackTrace());
    } finally {
      TestLifecycle.end();
    }
  }

  public void testLogFull() throws Exception {
    TestLifecycle.begin();
    try {
      final MockFunction mockFunction = getMockLoggingFunction();
      final ThreadLocalLogEventListener logEventListener = new ThreadLocalLogEventListener();
      final TestCalculationNode calcNode = new TestCalculationNode(logEventListener);
      TestLifecycle.register(calcNode);
      CalculationNodeUtils.configureTestCalcNode(calcNode, mockFunction);
      final CalculationJob calcJob = CalculationNodeUtils.getCalculationJob(mockFunction, ExecutionLogMode.FULL);
      final CalculationJobResultItem resultItemLogIndicators = getResultWithLogging(mockFunction, logEventListener, calcNode, calcJob);

      final ExecutionLog executionLog = resultItemLogIndicators.getExecutionLog();
      assertNotNull(executionLog);
      assertTrue(executionLog.getLogLevels().contains(LogLevel.WARN));
      assertFalse(executionLog.getLogLevels().contains(LogLevel.ERROR));
      assertFalse(executionLog.getLogLevels().contains(LogLevel.INFO));

      assertNotNull(executionLog.getEvents());
      assertEquals(1, executionLog.getEvents().size());
      final LogEvent event = Iterables.getOnlyElement(executionLog.getEvents());
      assertNotNull(event);
      assertEquals(LogLevel.WARN, event.getLevel());
      assertEquals("Warning during execution", event.getMessage());

      assertNull(executionLog.getExceptionClass());
      assertNull(executionLog.getExceptionMessage());
      assertNull(executionLog.getExceptionStackTrace());
    } finally {
      TestLifecycle.end();
    }
  }

  private CalculationJobResultItem getResultWithLogging(final MockFunction mockFunction, final ThreadLocalLogEventListener logEventListener, final TestCalculationNode calcNode,
      final CalculationJob calcJob) throws AsynchronousExecution {
    LogBridge.getInstance().addListener(logEventListener);
    CalculationJobResult jobResult;
    try {
      jobResult = calcNode.executeJob(calcJob);
    } finally {
      LogBridge.getInstance().removeListener(logEventListener);
    }

    assertNotNull(jobResult);
    assertEquals(1, jobResult.getResultItems().size());
    final CalculationJobResultItem resultItem = jobResult.getResultItems().get(0);
    assertEquals(InvocationResult.SUCCESS, resultItem.getResult());
    final ViewComputationCache cache = calcNode.getCache(calcJob.getSpecification());
    assertEquals("Result", cache.getValue(mockFunction.getResultSpec()));
    return resultItem;
  }

  private MockFunction getMockLoggingFunction() {
    final MockFunction fn = new MockLoggingFunction(MockFunction.UNIQUE_ID, ComputationTarget.NULL);
    fn.addResult(new ValueSpecification("OUTPUT", ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, MockFunction.UNIQUE_ID).get()), "Result");
    return fn;
  }

  private class MockLoggingFunction extends MockFunction {

    public MockLoggingFunction(final String uniqueId, final ComputationTarget target) {
      super(uniqueId, target);
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final LogEvent logEvent = new SimpleLogEvent(LogLevel.WARN, "Warning during execution");
      LogBridge.getInstance().log(logEvent);
      return super.execute(executionContext, inputs, target, desiredValues);
    }

  }

}
