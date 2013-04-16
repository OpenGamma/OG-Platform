/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.InfiniteViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link SequencePartitioningViewProcessWorkerFactory} class
 */
@Test(groups = TestGroup.UNIT)
public class SequencePartitioningViewProcessWorkerFactoryTest {

  private class ViewProcessWorkerFactoryMock implements ViewProcessWorkerFactory {

    private final List<ViewExecutionOptions> _executionOptions = new LinkedList<ViewExecutionOptions>();

    @Override
    public ViewProcessWorker createWorker(ViewProcessWorkerContext context, ViewExecutionOptions executionOptions, ViewDefinition viewDefinition) {
      _executionOptions.add(executionOptions);
      return Mockito.mock(ViewProcessWorker.class);
    }

  }

  private SequencePartitioningViewProcessWorkerFactory createFactory(final ViewProcessWorkerFactory underlying) {
    final StaticSequencePartitioningViewProcessWorkerFactory test = new StaticSequencePartitioningViewProcessWorkerFactory(underlying);
    test.setSaturation(4);
    test.setMinimumCycles(8);
    test.setMaximumCycles(32);
    return test;
  }

  public void testPassthrough() {
    final ViewProcessWorkerFactoryMock underlying = new ViewProcessWorkerFactoryMock();
    final SequencePartitioningViewProcessWorkerFactory test = createFactory(underlying);
    final ViewExecutionOptions options = ExecutionOptions.infinite(MarketData.live());
    test.createWorker(Mockito.mock(ViewProcessWorkerContext.class), options, Mockito.mock(ViewDefinition.class));
    assertEquals(underlying._executionOptions.size(), 1);
    assertEquals(underlying._executionOptions.get(0), options);
  }

  public void testShortSequence() {
    final ViewProcessWorkerFactoryMock underlying = new ViewProcessWorkerFactoryMock();
    final SequencePartitioningViewProcessWorkerFactory test = createFactory(underlying);
    final Instant t = Instant.now();
    final ViewCycleExecutionSequence sequence = ArbitraryViewCycleExecutionSequence.of(t, t.plusSeconds(1), t.plusSeconds(2));
    final ViewExecutionOptions options = ExecutionOptions.of(sequence, EnumSet.of(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE));
    test.createWorker(Mockito.mock(ViewProcessWorkerContext.class), options, Mockito.mock(ViewDefinition.class));
    assertEquals(underlying._executionOptions.size(), 1);
    assertEquals(underlying._executionOptions.get(0), options);
  }

  public void testSequence() {
    final ViewProcessWorkerFactoryMock underlying = new ViewProcessWorkerFactoryMock();
    final SequencePartitioningViewProcessWorkerFactory test = createFactory(underlying);
    final Instant t = Instant.now();
    final List<ViewCycleExecutionOptions> cycles = new ArrayList<ViewCycleExecutionOptions>(20);
    for (int i = 0; i < 20; i++) {
      cycles.add(ViewCycleExecutionOptions.builder().setValuationTime(t.plusSeconds(i)).create());
    }
    final ViewCycleExecutionSequence sequence = new ArbitraryViewCycleExecutionSequence(cycles);
    final ViewExecutionOptions options = ExecutionOptions.of(sequence, EnumSet.of(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE));
    test.createWorker(Mockito.mock(ViewProcessWorkerContext.class), options, Mockito.mock(ViewDefinition.class));
    assertEquals(underlying._executionOptions.size(), 3);
    for (ViewExecutionOptions spawned : underlying._executionOptions) {
      assertEquals(spawned.getDefaultExecutionOptions(), options.getDefaultExecutionOptions());
      assertEquals(spawned.getFlags(), options.getFlags());
      assertEquals(spawned.getMaxSuccessiveDeltaCycles(), options.getMaxSuccessiveDeltaCycles());
    }
    assertEquals(underlying._executionOptions.get(0).getExecutionSequence().estimateRemaining(), 8);
    assertEquals(underlying._executionOptions.get(1).getExecutionSequence().estimateRemaining(), 8);
    assertEquals(underlying._executionOptions.get(2).getExecutionSequence().estimateRemaining(), 4);
  }

  public void testInfiniteSequence() {
    final ViewProcessWorkerFactoryMock underlying = new ViewProcessWorkerFactoryMock();
    final SequencePartitioningViewProcessWorkerFactory test = createFactory(underlying);
    final ViewCycleExecutionSequence sequence = new InfiniteViewCycleExecutionSequence();
    final ViewExecutionOptions options = ExecutionOptions.of(sequence, EnumSet.of(ViewExecutionFlags.RUN_AS_FAST_AS_POSSIBLE));
    test.createWorker(Mockito.mock(ViewProcessWorkerContext.class), options, Mockito.mock(ViewDefinition.class));
    assertEquals(underlying._executionOptions.size(), 4);
    for (ViewExecutionOptions spawned : underlying._executionOptions) {
      assertEquals(spawned.getDefaultExecutionOptions(), options.getDefaultExecutionOptions());
      assertEquals(spawned.getFlags(), options.getFlags());
      assertEquals(spawned.getMaxSuccessiveDeltaCycles(), options.getMaxSuccessiveDeltaCycles());
      assertEquals(spawned.getExecutionSequence().estimateRemaining(), 32);
    }
  }

}
