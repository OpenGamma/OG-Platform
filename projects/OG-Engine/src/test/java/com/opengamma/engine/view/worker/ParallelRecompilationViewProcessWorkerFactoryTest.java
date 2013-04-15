/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionFlags.ParallelRecompilationMode;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.InfiniteViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link ParallelRecompilationViewProcessWorkerFactory} class.
 */
@Test(groups = TestGroup.UNIT)
public class ParallelRecompilationViewProcessWorkerFactoryTest {

  public void testIgnoreValidity() {
    final ViewProcessWorkerFactory underlying = Mockito.mock(ViewProcessWorkerFactory.class);
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ParallelRecompilationViewProcessWorkerFactory factory = new ParallelRecompilationViewProcessWorkerFactory(underlying);
    factory.setDelegate(factory);
    final ViewExecutionOptions options = new ExecutionOptions(new InfiniteViewCycleExecutionSequence(), ExecutionFlags.triggersEnabled().awaitMarketData().ignoreCompilationValidity().get());
    factory.createWorker(context, options, viewDefinition);
    Mockito.verify(underlying, Mockito.only()).createWorker(context, options, viewDefinition);
  }

  public void testPassthrough() {
    final ViewProcessWorkerFactory underlying = Mockito.mock(ViewProcessWorkerFactory.class);
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ParallelRecompilationViewProcessWorkerFactory factory = new ParallelRecompilationViewProcessWorkerFactory(underlying);
    factory.setDelegate(factory);
    final ViewExecutionOptions options = new ExecutionOptions(new InfiniteViewCycleExecutionSequence(), ExecutionFlags.triggersEnabled().awaitMarketData().get());
    factory.createWorker(context, options, viewDefinition);
    Mockito.verify(underlying, Mockito.only()).createWorker(context, options, viewDefinition);
  }

  public void testParallel() {
    final ViewProcessWorkerFactory underlying = Mockito.mock(ViewProcessWorkerFactory.class);
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = new ExecutionOptions(new InfiniteViewCycleExecutionSequence(), ExecutionFlags.triggersEnabled().waitForInitialTrigger()
        .parallelCompilation(ParallelRecompilationMode.PARALLEL_EXECUTION).get());
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ViewProcessWorker delegateWorker = Mockito.mock(ViewProcessWorker.class);
    Mockito.when(underlying.createWorker(Mockito.<ViewProcessWorkerContext>anyObject(), Mockito.<ViewExecutionOptions>anyObject(), Mockito.<ViewDefinition>anyObject())).then(
        new Answer<ViewProcessWorker>() {
          @Override
          public ViewProcessWorker answer(InvocationOnMock invocation) throws Throwable {
            assertTrue(invocation.getArguments()[0] instanceof ParallelRecompilationViewProcessWorker.ParallelExecutionContext);
            final ViewExecutionOptions delegateOptions = (ViewExecutionOptions) invocation.getArguments()[1];
            assertEquals(delegateOptions.getExecutionSequence(), options.getExecutionSequence());
            assertEquals(delegateOptions.getFlags(), ExecutionFlags.triggersEnabled().waitForInitialTrigger().ignoreCompilationValidity().get());
            assertSame(invocation.getArguments()[2], viewDefinition);
            return delegateWorker;
          }
        });
    final ParallelRecompilationViewProcessWorkerFactory factory = new ParallelRecompilationViewProcessWorkerFactory(underlying);
    factory.setDelegate(factory);
    final ParallelRecompilationViewProcessWorker worker = (ParallelRecompilationViewProcessWorker) factory.createWorker(context, options, viewDefinition);
    assertSame(worker.getContext(), context);
    assertEquals(worker.getFlags(), ExecutionFlags.triggersEnabled().ignoreCompilationValidity().get());
  }

  public void testDeferred() {
    final ViewProcessWorkerFactory underlying = Mockito.mock(ViewProcessWorkerFactory.class);
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = new ExecutionOptions(new InfiniteViewCycleExecutionSequence(), ExecutionFlags.none().awaitMarketData()
        .parallelCompilation(ParallelRecompilationMode.DEFERRED_EXECUTION).get());
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ViewProcessWorker delegateWorker = Mockito.mock(ViewProcessWorker.class);
    Mockito.when(underlying.createWorker(Mockito.<ViewProcessWorkerContext>anyObject(), Mockito.<ViewExecutionOptions>anyObject(), Mockito.<ViewDefinition>anyObject())).then(
        new Answer<ViewProcessWorker>() {
          @Override
          public ViewProcessWorker answer(InvocationOnMock invocation) throws Throwable {
            assertTrue(invocation.getArguments()[0] instanceof ParallelRecompilationViewProcessWorker.DeferredExecutionContext);
            final ViewExecutionOptions delegateOptions = (ViewExecutionOptions) invocation.getArguments()[1];
            assertEquals(delegateOptions.getExecutionSequence(), options.getExecutionSequence());
            assertEquals(delegateOptions.getFlags(), ExecutionFlags.none().awaitMarketData().ignoreCompilationValidity().get());
            assertSame(invocation.getArguments()[2], viewDefinition);
            return delegateWorker;
          }
        });
    final ParallelRecompilationViewProcessWorkerFactory factory = new ParallelRecompilationViewProcessWorkerFactory(underlying);
    factory.setDelegate(factory);
    final ParallelRecompilationViewProcessWorker worker = (ParallelRecompilationViewProcessWorker) factory.createWorker(context, options, viewDefinition);
    assertSame(worker.getContext(), context);
  }

  public void testImmediate() {
    final ViewProcessWorkerFactory underlying = Mockito.mock(ViewProcessWorkerFactory.class);
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final ViewExecutionOptions options = new ExecutionOptions(new InfiniteViewCycleExecutionSequence(), ExecutionFlags.none().parallelCompilation(ParallelRecompilationMode.IMMEDIATE_EXECUTION).get());
    final ViewDefinition viewDefinition = Mockito.mock(ViewDefinition.class);
    final ViewProcessWorker delegateWorker = Mockito.mock(ViewProcessWorker.class);
    Mockito.when(underlying.createWorker(Mockito.<ViewProcessWorkerContext>anyObject(), Mockito.<ViewExecutionOptions>anyObject(), Mockito.<ViewDefinition>anyObject())).then(
        new Answer<ViewProcessWorker>() {
          @Override
          public ViewProcessWorker answer(InvocationOnMock invocation) throws Throwable {
            assertTrue(invocation.getArguments()[0] instanceof ParallelRecompilationViewProcessWorker.ImmediateExecutionContext);
            final ViewExecutionOptions delegateOptions = (ViewExecutionOptions) invocation.getArguments()[1];
            assertEquals(delegateOptions.getExecutionSequence(), options.getExecutionSequence());
            assertEquals(delegateOptions.getFlags(), ExecutionFlags.none().ignoreCompilationValidity().get());
            assertSame(invocation.getArguments()[2], viewDefinition);
            return delegateWorker;
          }
        });
    final ParallelRecompilationViewProcessWorkerFactory factory = new ParallelRecompilationViewProcessWorkerFactory(underlying);
    factory.setDelegate(factory);
    final ParallelRecompilationViewProcessWorker worker = (ParallelRecompilationViewProcessWorker) factory.createWorker(context, options, viewDefinition);
    assertSame(worker.getContext(), context);
  }

}
