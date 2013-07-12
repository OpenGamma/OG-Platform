/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphs;
import com.opengamma.engine.view.cycle.ViewCycle;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.InfiniteViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;

/**
 * Tests the {@link SequencePartitioningViewProcessWorker} class
 */
@Test(groups = TestGroup.UNIT)
public class SequencePartitioningViewProcessWorkerTest {

  private class ViewProcessWorkerMock implements ViewProcessWorker, Runnable {

    private final Thread _thread;
    private final ViewProcessWorkerContext _context;
    private final ViewExecutionOptions _options;
    private final int _delay;
    private volatile ViewDefinition _viewDefinition;
    private volatile boolean _terminated;

    public ViewProcessWorkerMock(final ViewProcessWorkerContext context, final ViewExecutionOptions executionOptions, final ViewDefinition viewDefinition, final int delay) {
      _context = context;
      _options = executionOptions;
      _viewDefinition = viewDefinition;
      _delay = delay;
      _thread = new Thread(this);
      _thread.start();
    }

    // ViewProcessWorker

    @Override
    public boolean triggerCycle() {
      fail();
      return false;
    }

    @Override
    public boolean requestCycle() {
      fail();
      return false;
    }

    @Override
    public void updateViewDefinition(ViewDefinition viewDefinition) {
      _viewDefinition = viewDefinition;
    }

    @Override
    public void terminate() {
      _terminated = true;
      _thread.interrupt();
    }

    @Override
    public void join() throws InterruptedException {
      _thread.join();
    }

    @Override
    public boolean join(long timeout) throws InterruptedException {
      _thread.join(timeout);
      return !_thread.isAlive();
    }

    @Override
    public boolean isTerminated() {
      return !_thread.isAlive();
    }

    @Override
    public void forceGraphRebuild() {
      // do nothing
    }

    // Runnable

    @Override
    public void run() {
      boolean compiled = false;
      do {
        ViewCycleExecutionOptions options = _options.getExecutionSequence().poll(_options.getDefaultExecutionOptions());
        if (options == null) {
          _context.workerCompleted();
          return;
        }
        _context.cycleStarted(Mockito.mock(ViewCycleMetadata.class));
        if (!compiled) {
          compiled = true;
          _context.viewDefinitionCompiled(Mockito.mock(ViewExecutionDataProvider.class), Mockito.mock(CompiledViewDefinitionWithGraphs.class));
        }
        _context.cycleFragmentCompleted(Mockito.mock(ViewComputationResultModel.class), _viewDefinition);
        if (_delay > 0) {
          try {
            Thread.sleep(_delay);
          } catch (InterruptedException e) {
            break;
          }
        }
        _context.cycleFragmentCompleted(Mockito.mock(ViewComputationResultModel.class), _viewDefinition);
        _context.cycleCompleted(Mockito.mock(ViewCycle.class));
      } while (!_terminated);
      _context.workerCompleted();
    }

  }

  private class ViewProcessWorkerFactoryMock implements ViewProcessWorkerFactory {

    private final int _delay;

    public ViewProcessWorkerFactoryMock(final int delay) {
      _delay = delay;
    }

    @Override
    public ViewProcessWorker createWorker(ViewProcessWorkerContext context, ViewExecutionOptions executionOptions, ViewDefinition viewDefinition) {
      return new ViewProcessWorkerMock(context, executionOptions, viewDefinition, _delay);
    }

  }

  public void testFiniteSequence() throws InterruptedException {
    final ViewProcessWorkerFactoryMock underlying = new ViewProcessWorkerFactoryMock(0);
    final Instant t = Instant.now();
    final List<ViewCycleExecutionOptions> cycles = new ArrayList<ViewCycleExecutionOptions>(20);
    for (int i = 0; i < 37; i++) {
      cycles.add(ViewCycleExecutionOptions.builder().setValuationTime(t.plusSeconds(i)).create());
    }
    final ViewCycleExecutionSequence sequence = new ArbitraryViewCycleExecutionSequence(cycles);
    final ViewExecutionOptions options = ExecutionOptions.of(sequence, EnumSet.of(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE));
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final SequencePartitioningViewProcessWorker worker = new SequencePartitioningViewProcessWorker(underlying, context, options, Mockito.mock(ViewDefinition.class), 10, 2);
    assertTrue(worker.join(4 * Timeout.standardTimeoutMillis()));
    assertTrue(worker.isTerminated());
    Mockito.verify(context, Mockito.times(cycles.size())).cycleStarted(Mockito.any(ViewCycleMetadata.class));
    // Total of four workers spawned
    Mockito.verify(context, Mockito.times(4)).viewDefinitionCompiled(Mockito.any(ViewExecutionDataProvider.class), Mockito.any(CompiledViewDefinitionWithGraphs.class));
    Mockito.verify(context, Mockito.times(2 * cycles.size())).cycleFragmentCompleted(Mockito.any(ViewComputationResultModel.class), Mockito.any(ViewDefinition.class));
    Mockito.verify(context, Mockito.times(cycles.size())).cycleCompleted(Mockito.any(ViewCycle.class));
    // Only one completion acknowledgement from the top level worker
    Mockito.verify(context, Mockito.times(1)).workerCompleted();
    Mockito.verify(context, Mockito.times(0)).cycleExecutionFailed(Mockito.any(ViewCycleExecutionOptions.class), Mockito.any(Exception.class));
    Mockito.verify(context, Mockito.times(0)).viewDefinitionCompilationFailed(Mockito.any(Instant.class), Mockito.any(Exception.class));
  }

  public void testInfiniteSequence() throws InterruptedException {
    final ViewProcessWorkerFactoryMock underlying = new ViewProcessWorkerFactoryMock((int) (Timeout.standardTimeoutMillis() / 32));
    final ViewExecutionOptions options = ExecutionOptions.of(new InfiniteViewCycleExecutionSequence(), EnumSet.of(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE));
    final ViewProcessWorkerContext context = Mockito.mock(ViewProcessWorkerContext.class);
    final SequencePartitioningViewProcessWorker worker = new SequencePartitioningViewProcessWorker(underlying, context, options, Mockito.mock(ViewDefinition.class), 10, 2);
    assertFalse(worker.join(Timeout.standardTimeoutMillis()));
    assertFalse(worker.isTerminated());
    worker.terminate();
    assertTrue(worker.join(4 * Timeout.standardTimeoutMillis()));
    assertTrue(worker.isTerminated());
    Mockito.verify(context, Mockito.atLeast(2)).viewDefinitionCompiled(Mockito.any(ViewExecutionDataProvider.class), Mockito.any(CompiledViewDefinitionWithGraphs.class));
  }

}
