/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import static org.testng.Assert.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.position.Portfolio;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.builder.TestDependencyGraphBuilder;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfigurationImpl;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.impl.ViewProcessContext;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.lambdava.functions.Function2;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ParallelRecompilationViewProcessWorker} inner classes when running against an infinite sequence at resolver LATEST/LATEST.
 */
@Test(groups = TestGroup.UNIT_SLOW)
public class ParallelRecompilationInfiniteLatestTest extends AbstractParallelRecompilationTest {

  private static final Logger s_logger = LoggerFactory.getLogger(ParallelRecompilationInfiniteLatestTest.class);

  private CompiledViewDefinitionWithGraphs compiledViewDefinition(final ViewDefinition viewDefinition, final Map<ComputationTargetReference, UniqueId> resolutions) {
    final VersionCorrection versionCorrection = VersionCorrection.of(Instant.now(), Instant.now());
    final DependencyGraph graph = new TestDependencyGraphBuilder("Default").buildGraph();
    final Portfolio portfolio = Mockito.mock(Portfolio.class);
    return new CompiledViewDefinitionWithGraphsImpl(versionCorrection, "view-id", viewDefinition, Collections.singleton(graph), new HashMap<ComputationTargetReference, UniqueId>(resolutions),
        portfolio, 0, Collections.<CompiledViewCalculationConfiguration>singleton(CompiledViewCalculationConfigurationImpl.of(graph)), null, null);
  }

  private ViewProcessWorkerFactory workerFactory(final ExecutorService executor, final Map<ComputationTargetReference, UniqueId> resolutions) {
    final Random rand = new Random();
    return new ViewProcessWorkerFactory() {
      @Override
      public ViewProcessWorker createWorker(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
        final ViewProcessWorker worker = Mockito.mock(ViewProcessWorker.class);
        final AtomicBoolean terminated = new AtomicBoolean();
        Mockito.doAnswer(new Answer<Void>() {
          @Override
          public Void answer(InvocationOnMock invocation) throws Throwable {
            terminated.set(true);
            return null;
          }
        }).when(worker).terminate();
        executor.submit(new Runnable() {
          @Override
          public void run() {
            try {
              Thread.sleep(rand.nextInt(500) + 350);
              context.viewDefinitionCompiled(Mockito.mock(ViewExecutionDataProvider.class), compiledViewDefinition(viewDefinition, resolutions));
              while (!terminated.get()) {
                context.cycleStarted(Mockito.mock(ViewCycleMetadata.class));
                Thread.sleep(rand.nextInt(300) + 50);
                context.cycleFragmentCompleted(Mockito.mock(ViewComputationResultModel.class), viewDefinition);
                Thread.sleep(rand.nextInt(300) + 50);
                context.cycleCompleted(Mockito.mock(ViewCycle.class));
                Thread.sleep(rand.nextInt(300) + 50);
              }
            } catch (InterruptedException e) {
              s_logger.debug("Interrupted", e);
            } catch (RuntimeException e) {
              s_logger.error("Caught exception", e);
            }
          }
        });
        return worker;
      }
    };
  }

  private static class MockContext implements ViewProcessWorkerContext {

    private final ViewProcessContext _context;
    private final LinkedBlockingQueue<String> _events = new LinkedBlockingQueue<String>();

    public MockContext(final ViewProcessContext context) {
      _context = context;
    }

    public String event() throws InterruptedException {
      return _events.poll(5000, TimeUnit.MILLISECONDS);
    }

    @Override
    public ViewProcessContext getProcessContext() {
      return _context;
    }

    @Override
    public void viewDefinitionCompiled(ViewExecutionDataProvider dataProvider, CompiledViewDefinitionWithGraphs compiled) {
      _events.add("view definition compiled");
    }

    @Override
    public void viewDefinitionCompilationFailed(Instant compilationTime, Exception exception) {
      _events.add("view definition compilation failed");
    }

    @Override
    public void cycleStarted(ViewCycleMetadata cycleMetadata) {
      _events.add("cycle started");
    }

    @Override
    public void cycleFragmentCompleted(ViewComputationResultModel result, ViewDefinition viewDefinition) {
      _events.add("cycle fragment completed");
    }

    @Override
    public void cycleCompleted(ViewCycle cycle) {
      _events.add("cycle completed");
    }

    @Override
    public void cycleExecutionFailed(ViewCycleExecutionOptions options, Exception exception) {
      _events.add("cycle execution failed");
    }

    @Override
    public void workerCompleted() {
      _events.add("worker completed");
    }

  }

  @Override
  protected void testImpl(final Function2<ParallelRecompilationViewProcessWorker, ViewExecutionOptions, Void> callback) throws InterruptedException {
    final ExecutorService executor = Executors.newCachedThreadPool();
    try {
      final Map<ComputationTargetReference, UniqueId> resolutions = new HashMap<ComputationTargetReference, UniqueId>();
      resolutions.put(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO, UniqueId.of("Test", "0")), UniqueId.of("Test", "0", "0"));
      final ChangeManager changeManager = new BasicChangeManager();
      final ComputationTargetResolver targetResolver = Mockito.mock(ComputationTargetResolver.class);
      Mockito.when(targetResolver.changeManager()).thenReturn(changeManager);
      final FunctionCompilationContext ctx = new FunctionCompilationContext();
      ctx.setRawComputationTargetResolver(targetResolver);
      final CompiledFunctionService cfs = Mockito.mock(CompiledFunctionService.class);
      Mockito.when(cfs.getFunctionCompilationContext()).thenReturn(ctx);
      final ViewProcessContext vpContext = Mockito.mock(ViewProcessContext.class);
      Mockito.when(vpContext.getFunctionCompilationService()).thenReturn(cfs);
      final MockContext context = new MockContext(vpContext);
      final ViewExecutionOptions options = ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().ignoreCompilationValidity().get());
      final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
      final ParallelRecompilationViewProcessWorker worker = new ParallelRecompilationViewProcessWorker(workerFactory(executor, resolutions), context, options, viewDefinition);
      callback.execute(worker, options);
      s_logger.debug("Waiting for initial compilation");
      assertEquals(context.event(), "view definition compiled"); // From primary worker
      for (int j = 0; j < 5; j++) {
        // Expect a sequence of operations
        for (int i = 0; i < 3; i++) {
          s_logger.debug("Waiting for cycle to start");
          assertEquals(context.event(), "cycle started"); // From primary worker
          s_logger.info("Cycle started");
          assertEquals(context.event(), "cycle fragment completed");
          s_logger.info("Cycle fragment completed");
          assertEquals(context.event(), "cycle completed");
          s_logger.info("Cycle completed");
        }
        // Signal change ...
        s_logger.debug("Signalling change");
        resolutions.put(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO, UniqueId.of("Test", "0")), UniqueId.of("Test", "0", Integer.toString(j + 1)));
        changeManager.entityChanged(ChangeType.CHANGED, ObjectId.of("Test", "0"), Instant.now(), Instant.now(), Instant.now());
        s_logger.info("Change signalled");
        // ... and expect a view definition compiled to interrupt the sequence 
        String event = context.event();
        for (int i = 0; i < 20; i++) {
          if (event.equals("cycle started")) {
            s_logger.info("Legacy cycle started");
            event = context.event();
            if (event.equals("cycle fragment completed")) {
              s_logger.info("Legacy fragment completed");
              event = context.event();
              if (event.equals("cycle completed")) {
                s_logger.info("Legacy cycle completed");
                event = context.event();
              } else {
                break;
              }
            } else {
              break;
            }
          } else {
            break;
          }
        }
        assertEquals(event, "view definition compiled");
        s_logger.info("New compilation");
      }
    } finally {
      executor.shutdownNow();
    }
  }

}
