/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.worker.ParallelRecompilationViewProcessWorker.AbstractViewProcessWorkerContext;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ParallelRecompilationViewProcessWorker} class.
 */
@Test(groups = TestGroup.UNIT)
public class ParallelRecompilationViewProcessWorkerTest {

  @Test(expectedExceptions = IllegalStateException.class)
  public void testIllegalStart_alreadyRunning() {
    final ViewProcessWorkerFactory workerFactory = Mockito.mock(ViewProcessWorkerFactory.class);
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().ignoreCompilationValidity().get());
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ParallelRecompilationViewProcessWorker worker = new ParallelRecompilationViewProcessWorker(workerFactory, context, options, viewDefinition);
    worker.startParallel(options);
    worker.startParallel(options);
  }

  @Test(expectedExceptions = IllegalStateException.class)
  public void testIllegalStart_terminated() {
    final ViewProcessWorkerFactory workerFactory = Mockito.mock(ViewProcessWorkerFactory.class);
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().ignoreCompilationValidity().get());
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ParallelRecompilationViewProcessWorker worker = new ParallelRecompilationViewProcessWorker(workerFactory, context, options, viewDefinition);
    worker.startParallel(options);
    worker.terminate();
    worker.startParallel(options);
  }

  public void testTriggerCycle() {
    // Creates workers that will trigger on the first cycle, ignore the second, and then terminate on the third
    final ViewProcessWorkerFactory workerFactory = new ViewProcessWorkerFactory() {
      @Override
      public ViewProcessWorker createWorker(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
        final ViewProcessWorker worker = Mockito.mock(ViewProcessWorker.class);
        Mockito.when(worker.triggerCycle()).thenAnswer(new Answer<Boolean>() {

          private int _count = 0;

          @Override
          public Boolean answer(InvocationOnMock invocation) throws Throwable {
            _count++;
            if (_count == 3) {
              context.workerCompleted();
            }
            return (_count == 1);
          }

        });
        return worker;
      }
    };
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().ignoreCompilationValidity().get());
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ParallelRecompilationViewProcessWorker worker = new ParallelRecompilationViewProcessWorker(workerFactory, context, options, viewDefinition);
    // With no worker
    assertFalse(worker.triggerCycle());
    // With a primary worker that responds TRUE (first time)
    worker.startParallel(options);
    assertTrue(worker.triggerCycle());
    // With a primary worker that responds FALSE (second time)
    worker.startSecondaryWorker(worker.getPrimary(), options.getExecutionSequence());
    assertFalse(worker.triggerCycle());
    // With a primary worker that returns FALSE, causes secondary promotion, and that returns TRUE (first time)
    assertTrue(worker.triggerCycle());
    // With a primary worker that responds FALSE (second time)
    assertFalse(worker.triggerCycle());
    assertFalse(worker.isTerminated());
    // With a primary worker that returns FALSE because of termination
    assertFalse(worker.triggerCycle());
    assertTrue(worker.isTerminated());
    // After explicit termination
    worker.terminate();
    assertFalse(worker.triggerCycle());
  }

  public void testRequestCycle() {
    // Creates workers that will honor the request on the first cycle, ignore the second, and then terminate on the third
    final ViewProcessWorkerFactory workerFactory = new ViewProcessWorkerFactory() {
      @Override
      public ViewProcessWorker createWorker(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
        final ViewProcessWorker worker = Mockito.mock(ViewProcessWorker.class);
        Mockito.when(worker.requestCycle()).thenAnswer(new Answer<Boolean>() {

          private int _count = 0;

          @Override
          public Boolean answer(InvocationOnMock invocation) throws Throwable {
            _count++;
            if (_count == 3) {
              context.workerCompleted();
            }
            return (_count == 1);
          }

        });
        return worker;
      }
    };
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().ignoreCompilationValidity().get());
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ParallelRecompilationViewProcessWorker worker = new ParallelRecompilationViewProcessWorker(workerFactory, context, options, viewDefinition);
    // With no worker
    assertFalse(worker.requestCycle());
    // With a primary worker that responds TRUE (first time)
    worker.startParallel(options);
    assertTrue(worker.requestCycle());
    // With a primary worker that responds FALSE (second time)
    worker.startSecondaryWorker(worker.getPrimary(), options.getExecutionSequence());
    assertFalse(worker.requestCycle());
    // With a primary worker that returns FALSE, causes secondary promotion, and that returns TRUE (first time)
    assertTrue(worker.requestCycle());
    // With a primary worker that responds FALSE (second time)
    assertFalse(worker.requestCycle());
    assertFalse(worker.isTerminated());
    // With a primary worker that returns FALSE because of termination
    assertFalse(worker.requestCycle());
    assertTrue(worker.isTerminated());
    // After explicit termination
    worker.terminate();
    assertFalse(worker.requestCycle());
  }

  public void testUpdateViewDefinition() {
    final List<ViewDefinition> viewDefinitions = ImmutableList.of(Mockito.mock(ViewDefinition.class), Mockito.mock(ViewDefinition.class), Mockito.mock(ViewDefinition.class),
        Mockito.mock(ViewDefinition.class));
    final List<ViewProcessWorker> workers = new ArrayList<ViewProcessWorker>(3);
    final ViewProcessWorkerFactory workerFactory = new ViewProcessWorkerFactory() {
      @Override
      public ViewProcessWorker createWorker(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
        final ViewProcessWorker worker = Mockito.mock(ViewProcessWorker.class);
        workers.add(worker);
        assertSame(viewDefinition, viewDefinitions.get(workers.size()));
        return worker;
      }
    };
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().ignoreCompilationValidity().get());
    final ParallelRecompilationViewProcessWorker worker = new ParallelRecompilationViewProcessWorker(workerFactory, context, options, viewDefinitions.get(0));
    AbstractViewProcessWorkerContext primary, secondary;
    // With no current workers
    assertNull(worker.getPrimary());
    assertNull(worker.getSecondary());
    worker.updateViewDefinition(viewDefinitions.get(1));
    // With a primary worker only
    worker.startParallel(options);
    assertEquals(workers.size(), 1);
    primary = worker.getPrimary();
    assertNotNull(primary);
    assertNull(worker.getSecondary());
    worker.updateViewDefinition(viewDefinitions.get(2));
    assertEquals(workers.size(), 2);
    assertSame(worker.getPrimary(), primary);
    secondary = worker.getSecondary();
    assertNotNull(secondary);
    // With a secondary worker - should replace the previous
    worker.updateViewDefinition(viewDefinitions.get(3));
    assertEquals(workers.size(), 3);
    assertSame(worker.getPrimary(), primary);
    assertNotSame(worker.getSecondary(), secondary);
    Mockito.verify(workers.get(0), Mockito.never()).terminate();
    Mockito.verify(workers.get(1), Mockito.atLeastOnce()).terminate();
    Mockito.verify(workers.get(2), Mockito.never()).terminate();
  }

  public void testTerminate() {
    final List<ViewProcessWorkerContext> workerContexts = new ArrayList<ViewProcessWorkerContext>(2);
    final List<ViewProcessWorker> workers = new ArrayList<ViewProcessWorker>(2);
    final ViewProcessWorkerFactory workerFactory = new ViewProcessWorkerFactory() {
      @Override
      public ViewProcessWorker createWorker(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
        workerContexts.add(context);
        final ViewProcessWorker worker = Mockito.mock(ViewProcessWorker.class);
        workers.add(worker);
        return worker;
      }
    };
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().ignoreCompilationValidity().get());
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ParallelRecompilationViewProcessWorker worker = new ParallelRecompilationViewProcessWorker(workerFactory, context, options, viewDefinition);
    // Initially "terminated"
    assertTrue(worker.isTerminated());
    // Spawn workers
    worker.startParallel(options);
    worker.startSecondaryWorker(worker.getPrimary(), options.getExecutionSequence());
    assertFalse(worker.isTerminated());
    // Terminate
    worker.terminate();
    assertFalse(worker.isTerminated());
    Mockito.verify(workers.get(0), Mockito.atLeastOnce()).terminate();
    Mockito.verify(workers.get(1), Mockito.atLeastOnce()).terminate();
    workerContexts.get(0).workerCompleted();
    assertFalse(worker.isTerminated());
    workerContexts.get(1).workerCompleted();
    assertTrue(worker.isTerminated());
    // Already terminated
    worker.terminate();
    assertTrue(worker.isTerminated());
  }

  @Test(timeOut = 10000)
  public void testJoin_noWorkers() throws InterruptedException {
    final ViewProcessWorkerFactory workerFactory = Mockito.mock(ViewProcessWorkerFactory.class);
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().ignoreCompilationValidity().get());
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ParallelRecompilationViewProcessWorker worker = new ParallelRecompilationViewProcessWorker(workerFactory, context, options, viewDefinition);
    // No-op
    worker.join();
  }

  @Test(timeOut = 10000)
  public void testJoin_workersTerminateBefore() throws InterruptedException {
    final ViewProcessWorkerFactory workerFactory = new ViewProcessWorkerFactory() {
      @Override
      public ViewProcessWorker createWorker(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
        final ViewProcessWorker worker = Mockito.mock(ViewProcessWorker.class);
        try {
          Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
              context.workerCompleted();
              return null;
            }
          }).when(worker).join();
        } catch (InterruptedException e) {
          // Doesn't get thrown
        }
        return worker;
      }
    };
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().ignoreCompilationValidity().get());
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ParallelRecompilationViewProcessWorker worker = new ParallelRecompilationViewProcessWorker(workerFactory, context, options, viewDefinition);
    // Start workers
    worker.startParallel(options);
    worker.startSecondaryWorker(worker.getPrimary(), options.getExecutionSequence());
    // Join on the first worker will see the secondary worker promoted, that will join and this will complete 
    worker.join();
    assertNull(worker.getPrimary());
    assertNull(worker.getSecondary());
  }

  @Test(timeOut = 10000)
  public void testJoin_workersTerminateAfter() throws InterruptedException {
    final ViewProcessWorkerFactory workerFactory = new ViewProcessWorkerFactory() {
      @Override
      public ViewProcessWorker createWorker(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
        return Mockito.mock(ViewProcessWorker.class);
      }
    };
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().ignoreCompilationValidity().get());
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ParallelRecompilationViewProcessWorker worker = new ParallelRecompilationViewProcessWorker(workerFactory, context, options, viewDefinition);
    // Start workers
    worker.startParallel(options);
    worker.startSecondaryWorker(worker.getPrimary(), options.getExecutionSequence());
    // Join on the first worker will see the secondary worker promoted, that will join and this will complete 
    worker.join();
    assertNull(worker.getPrimary());
    assertNull(worker.getSecondary());
  }

  @Test(timeOut = 10000)
  public void testJoin_timeout_noWorkers() throws InterruptedException {
    final ViewProcessWorkerFactory workerFactory = new ViewProcessWorkerFactory() {
      @Override
      public ViewProcessWorker createWorker(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
        final ViewProcessWorker worker = Mockito.mock(ViewProcessWorker.class);
        try {
          Mockito.when(worker.join(Mockito.anyLong())).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
              assertTrue(((Long) invocation.getArguments()[0]) <= 20000);
              return Boolean.TRUE;
            }
          });
        } catch (InterruptedException e) {
          // Doesn't get thrown
        }
        return worker;
      }
    };
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().ignoreCompilationValidity().get());
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ParallelRecompilationViewProcessWorker worker = new ParallelRecompilationViewProcessWorker(workerFactory, context, options, viewDefinition);
    // No-op
    assertTrue(worker.join(20000));
  }

  @Test(timeOut = 10000)
  public void testJoin_timeout_workersTerminateBefore() throws InterruptedException {
    final ViewProcessWorkerFactory workerFactory = new ViewProcessWorkerFactory() {
      @Override
      public ViewProcessWorker createWorker(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
        final ViewProcessWorker worker = Mockito.mock(ViewProcessWorker.class);
        try {
          Mockito.doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
              assertTrue(((Long) invocation.getArguments()[0]) <= 20000);
              context.workerCompleted();
              return Boolean.TRUE;
            }
          }).when(worker).join(Mockito.anyLong());
        } catch (InterruptedException e) {
          // Doesn't get thrown
        }
        return worker;
      }
    };
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().ignoreCompilationValidity().get());
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ParallelRecompilationViewProcessWorker worker = new ParallelRecompilationViewProcessWorker(workerFactory, context, options, viewDefinition);
    // Start workers
    worker.startParallel(options);
    worker.startSecondaryWorker(worker.getPrimary(), options.getExecutionSequence());
    // Join on the first worker will see the secondary worker promoted, that will join and this will complete 
    assertTrue(worker.join(20000));
    assertNull(worker.getPrimary());
    assertNull(worker.getSecondary());
  }

  @Test(timeOut = 10000)
  public void testJoin_timeout_workersTerminateAfter() throws InterruptedException {
    final ViewProcessWorkerFactory workerFactory = new ViewProcessWorkerFactory() {
      @Override
      public ViewProcessWorker createWorker(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
        final ViewProcessWorker worker = Mockito.mock(ViewProcessWorker.class);
        try {
          Mockito.when(worker.join(Mockito.anyLong())).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
              assertTrue(((Long) invocation.getArguments()[0]) <= 20000);
              return Boolean.TRUE;
            }
          });
        } catch (InterruptedException e) {
          // Doesn't get thrown
        }
        return worker;
      }
    };
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().ignoreCompilationValidity().get());
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ParallelRecompilationViewProcessWorker worker = new ParallelRecompilationViewProcessWorker(workerFactory, context, options, viewDefinition);
    // Start workers
    worker.startParallel(options);
    worker.startSecondaryWorker(worker.getPrimary(), options.getExecutionSequence());
    // Join on the first worker will see the secondary worker promoted, that will join and this will complete 
    assertTrue(worker.join(20000));
    assertNull(worker.getPrimary());
    assertNull(worker.getSecondary());
  }

  @Test(timeOut = 10000)
  public void testJoin_timeout() throws InterruptedException {
    final ViewProcessWorkerFactory workerFactory = new ViewProcessWorkerFactory() {
      @Override
      public ViewProcessWorker createWorker(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition) {
        final ViewProcessWorker worker = Mockito.mock(ViewProcessWorker.class);
        try {
          Mockito.when(worker.join(Mockito.anyLong())).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
              assertTrue(((Long) invocation.getArguments()[0]) <= 20000);
              return Boolean.FALSE;
            }
          });
        } catch (InterruptedException e) {
          // Doesn't get thrown
        }
        return worker;
      }
    };
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().ignoreCompilationValidity().get());
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ParallelRecompilationViewProcessWorker worker = new ParallelRecompilationViewProcessWorker(workerFactory, context, options, viewDefinition);
    // Start workers
    worker.startParallel(options);
    worker.startSecondaryWorker(worker.getPrimary(), options.getExecutionSequence());
    // Join on the first worker will see the secondary worker promoted, that will join and this will complete 
    assertFalse(worker.join(20000));
    assertNotNull(worker.getPrimary());
    assertNotNull(worker.getSecondary());
    assertFalse(worker.join(0));
    assertNotNull(worker.getPrimary());
    assertNotNull(worker.getSecondary());
  }

}
