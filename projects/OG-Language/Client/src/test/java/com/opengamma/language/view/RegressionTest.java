/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.view;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.threeten.bp.temporal.ChronoUnit.DAYS;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;

/**
 * Tests a view running over historical market data.
 */
@Test(groups = TestGroup.INTEGRATION)
public class RegressionTest {

  private static final Logger s_logger = LoggerFactory.getLogger(RegressionTest.class);
  private static final long s_timeout = Timeout.standardTimeoutMillis() * 5;

  private final ViewProcessorTestEnvironment _env = new ViewProcessorTestEnvironment();
  private final LinkedBlockingQueue<Object> _results = new LinkedBlockingQueue<Object>();
  private final LinkedBlockingQueue<Object> _jobResults = new LinkedBlockingQueue<Object>();

  private ComputationTarget createDummyTarget() {
    return new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("OpenGamma", "Foo"));
  }

  private UniqueId createRegressionView() {
    final UniqueId viewId = UniqueId.of("Test", "1");
    final ViewDefinition viewDefinition = new ViewDefinition(viewId, "Regression Test View", "Test");
    final ViewCalculationConfiguration calcConfig = new ViewCalculationConfiguration(viewDefinition, "Default");
    calcConfig.addSpecificRequirement(new ValueRequirement("Dummy", createDummyTarget().toSpecification()));
    viewDefinition.addViewCalculationConfiguration(calcConfig);
    _env.setViewDefinition(viewDefinition);
    return viewId;
  }

  private void createFunctionRepository() {
    final InMemoryFunctionRepository functions = new InMemoryFunctionRepository();
    final ComputationTarget target = createDummyTarget();
    functions.addFunction(MockFunction.getMockFunction(target, new ValueSpecification("Dummy", target.toSpecification(), ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get()), 42d));
    _env.setFunctionRepository(functions);
  }

  private ViewResultListener createResultListener() {
    return new ViewResultListener() {

      @Override
      public UserPrincipal getUser() {
        return UserPrincipal.getTestUser();
      }

      @Override
      public void viewDefinitionCompiled(final CompiledViewDefinition compiledViewDefinition, final boolean hasMarketDataPermissions) {
        s_logger.info("View definition compiled");
        postResult("COMPILED");
      }

      @Override
      public void viewDefinitionCompilationFailed(final Instant valuationTime, final Exception exception) {
        s_logger.warn("View compilation failed", exception);
        postResult(exception);
      }

      @Override
      public void cycleStarted(ViewCycleMetadata cycleMetadata) {
        s_logger.info("Cycle started");
        postResult("STARTED");
      }

      @Override
      public void cycleFragmentCompleted(ViewComputationResultModel fullFragment, ViewDeltaResultModel deltaFragment) {
        s_logger.info("Cycle fragment completed");
        postJobResult(fullFragment);
      }

      @Override
      public void cycleCompleted(final ViewComputationResultModel fullResult, final ViewDeltaResultModel deltaResult) {
        s_logger.info("Cycle completed");
        postResult(fullResult);
      }

      @Override
      public void cycleExecutionFailed(final ViewCycleExecutionOptions executionOptions, final Exception exception) {
        s_logger.warn("Cycle execution failed", exception);
        postResult(exception);
      }

      @Override
      public void processCompleted() {
        s_logger.info("Process completed");
        postResult("COMPLETED");
      }

      @Override
      public void processTerminated(final boolean executionInterrupted) {
        s_logger.info("Process terminated");
        postResult("TERMINATED");
      }

      @Override
      public void clientShutdown(Exception e) {
        s_logger.info("Client shutdown");
        postResult("SHUTDOWN");
      }

    };
  }

  private void postResult(final Object result) {
    s_logger.debug("Post result {}", result);
    _results.add(result);
  }

  private void postJobResult(final Object result) {
    s_logger.debug("Post job result {}", result);
    _jobResults.add(result);
  }

  private Object getResult() {
    try {
      s_logger.debug("Waiting for result");
      return _results.poll(s_timeout, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      s_logger.warn("Interrupted");
      return null;
    }
  }

  public void testHistoricalData() {
    final Instant firstValuationInstant = Instant.now().minus(90, DAYS);
    final Instant lastValuationInstant = firstValuationInstant.plus(30, DAYS);
    final UniqueId viewId = createRegressionView();
    createFunctionRepository();
    final ViewClientDescriptor viewClientDescriptor = ViewClientDescriptor.historicalMarketData(viewId, firstValuationInstant, lastValuationInstant);
    _env.init();
    final ViewClient viewClient = _env.getViewProcessor().createViewClient(UserPrincipal.getTestUser());
    try {
      viewClient.setResultListener(createResultListener());
      viewClient.attachToViewProcess(viewClientDescriptor.getViewId(), viewClientDescriptor.getExecutionOptions(), true);
      Instant valuationInstant = firstValuationInstant;
      boolean compiled = false;
      do {
        viewClient.triggerCycle();
        if (!compiled) {
          assertEquals(getResult(), "COMPILED");
          compiled = true;
        }
        final Object result = getResult();
        s_logger.debug("Got result {}", result);
        assertTrue(result instanceof ViewComputationResultModel);
        ViewComputationResultModel model = (ViewComputationResultModel) result;
        assertEquals(valuationInstant, model.getViewCycleExecutionOptions().getValuationTime());
        valuationInstant = valuationInstant.plus(1, DAYS);
      } while (!valuationInstant.isAfter(lastValuationInstant));
      viewClient.triggerCycle();
      assertEquals(getResult(), "COMPLETED");
    } finally {
      viewClient.shutdown();
    }
  }

}
