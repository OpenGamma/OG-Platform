/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.calcnode.Capability;
import com.opengamma.engine.calcnode.JobDispatcher;
import com.opengamma.engine.calcnode.PlatformCapabilities;
import com.opengamma.engine.calcnode.stats.TotallingNodeStatisticsGatherer;
import com.opengamma.engine.exec.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link MultipleNodeExecutorTuner} class.
 */
@Test(groups = TestGroup.UNIT)
public class MultipleNodeExecutorTunerTest {

  public void testJobDispatcher() {
    final MultipleNodeExecutorFactory factory = Mockito.mock(MultipleNodeExecutorFactory.class);
    final MultipleNodeExecutorTuner tuner = new MultipleNodeExecutorTuner(factory);
    final JobDispatcher dispatcher = Mockito.mock(JobDispatcher.class);
    final Map<String, Collection<Capability>> capabilities = new HashMap<String, Collection<Capability>>();
    Mockito.when(dispatcher.getAllCapabilities()).thenReturn(capabilities);
    tuner.setJobDispatcher(dispatcher);
    tuner.run();
    Mockito.verifyZeroInteractions(factory);
    capabilities.put("A", Arrays.asList(Capability.instanceOf("Foo")));
    capabilities.put("B", Arrays.asList(Capability.instanceOf("Bar")));
    tuner.run();
    Mockito.verifyZeroInteractions(factory);
    capabilities.put("A", Arrays.asList(Capability.instanceOf("Foo"), Capability.parameterInstanceOf(PlatformCapabilities.NODE_COUNT, 10d)));
    capabilities.put("B", Arrays.asList(Capability.instanceOf("Bar"), Capability.parameterInstanceOf(PlatformCapabilities.NODE_COUNT, 4d)));
    tuner.run();
    Mockito.verify(factory, Mockito.times(1)).setMaximumConcurrency(7);
    Mockito.verify(factory, Mockito.times(1)).invalidateCache();
    Mockito.when(factory.getMaximumConcurrency()).thenReturn(7);
    tuner.run();
    Mockito.verify(factory, Mockito.times(1)).setMaximumConcurrency(7);
    Mockito.verify(factory, Mockito.times(1)).invalidateCache();
  }

  public void testStatisticsDecayRate() {
    final MultipleNodeExecutorTuner tuner = new MultipleNodeExecutorTuner(Mockito.mock(MultipleNodeExecutorFactory.class));
    assertEquals(tuner.getStatisticsDecayRate(), 0.1d);
    tuner.setStatisticsDecayRate(0.9d);
    assertEquals(tuner.getStatisticsDecayRate(), 0.9d);
  }

  public void testStatisticsKeepAlive() {
    final MultipleNodeExecutorTuner tuner = new MultipleNodeExecutorTuner(Mockito.mock(MultipleNodeExecutorFactory.class));
    assertEquals(tuner.getStatisticsKeepAlive(), 300);
    tuner.setStatisticsKeepAlive(60);
    assertEquals(tuner.getStatisticsKeepAlive(), 60);
  }

  public void testGraphExecutionStatistics() {
    final MultipleNodeExecutorFactory factory = Mockito.mock(MultipleNodeExecutorFactory.class);
    final MultipleNodeExecutorTuner tuner = new MultipleNodeExecutorTuner(factory);
    final AtomicLong age = new AtomicLong();
    final TotallingGraphStatisticsGathererProvider stats = new TotallingGraphStatisticsGathererProvider() {
      @Override
      public void dropStatisticsBefore(final Instant limit) {
        assertEquals(age.getAndSet(Instant.now().getEpochSecond() - limit.getEpochSecond()), 0);
      }
    };
    tuner.setGraphExecutionStatistics(stats);
    tuner.run();
    assertTrue(age.get() >= 300);
  }

  public void testJobDispatchStatistics() {
    final MultipleNodeExecutorFactory factory = Mockito.mock(MultipleNodeExecutorFactory.class);
    final MultipleNodeExecutorTuner tuner = new MultipleNodeExecutorTuner(factory);
    final AtomicLong age = new AtomicLong();
    final TotallingNodeStatisticsGatherer stats = new TotallingNodeStatisticsGatherer() {
      @Override
      public void dropStatisticsBefore(final Instant limit) {
        assertEquals(age.getAndSet(Instant.now().getEpochSecond() - limit.getEpochSecond()), 0);
      }
    };
    tuner.setJobDispatchStatistics(stats);
    tuner.run();
    assertTrue(age.get() >= 300);
  }

}
