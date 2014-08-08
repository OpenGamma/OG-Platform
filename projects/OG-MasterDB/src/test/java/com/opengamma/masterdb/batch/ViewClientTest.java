/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.io.Serializable;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Iterables;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityFilter;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.value.ComputedValueResult;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.AggregatedExecutionLog;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.ViewProcessState;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.cycle.ViewCycleMetadata;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.impl.ViewProcessImpl;
import com.opengamma.engine.view.impl.ViewProcessorImpl;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.engine.view.listener.ViewResultListenerFactory;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.TestLifecycle;

@Test(groups = TestGroup.UNIT)
public class ViewClientTest {

  @Mock
  private ViewResultListenerFactory viewResultListenerFactoryStub;
  @Mock
  private ViewResultListener viewResultListenerMock;

  @BeforeMethod(groups = TestGroup.UNIT)
  public void setUp() throws Exception {
    initMocks(this);
    when(viewResultListenerFactoryStub.createViewResultListener(ViewProcessorTestEnvironment.TEST_USER)).thenReturn(viewResultListenerMock);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testListenerNotifications() throws InterruptedException {
    TestLifecycle.begin();
    try {
      final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
      final SynchronousInMemoryLKVSnapshotProvider marketDataProvider = new SynchronousInMemoryLKVSnapshotProvider();
      marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 0);
      marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 0);
      env.setMarketDataProvider(marketDataProvider);

      env.setViewResultListenerFactory(viewResultListenerFactoryStub);
      env.init();

      final ViewProcessorImpl vp = env.getViewProcessor();

      vp.start();

      final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
      client.setFragmentResultMode(ViewResultMode.FULL_ONLY);

      marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), (byte) 1);
      marketDataProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), (byte) 2);

      final ViewExecutionOptions executionOptions = ExecutionOptions.batch(null, MarketData.live(), null);

      client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);

      final ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
      assertTrue(viewProcess.getState() == ViewProcessState.RUNNING);
      client.waitForCompletion();
      //
      final ArgumentCaptor<ViewCycleMetadata> argument = ArgumentCaptor.forClass(ViewCycleMetadata.class);
      verify(viewResultListenerMock).cycleStarted(argument.capture());

      assertEquals("boo~far", argument.getValue().getViewDefinitionId().toString());
      assertEquals(1, argument.getValue().getAllCalculationConfigurationNames().size());
      assertEquals("Test Calc Config", Iterables.getFirst(argument.getValue().getAllCalculationConfigurationNames(), null));

      final ArgumentCaptor<ViewComputationResultModel> fullFragment = ArgumentCaptor.forClass(ViewComputationResultModel.class);
      final ArgumentCaptor<ViewDeltaResultModel> deltaFragment = ArgumentCaptor.forClass(ViewDeltaResultModel.class);
      verify(viewResultListenerMock, times(2)).cycleFragmentCompleted(fullFragment.capture(), deltaFragment.capture());

      ViewComputationResultModel resultModel = fullFragment.getAllValues().get(0);
      assertEquals(UniqueId.of("ViewProcess", client.getUniqueId().getValue()), resultModel.getViewProcessId());
      assertEquals(UniqueId.of("ViewCycle", client.getUniqueId().getValue(), "1"), resultModel.getViewCycleId());

      assertEquals(
          newHashSet(
              new ComputedValueResult(new ValueSpecification("Value2", ComputationTargetSpecification.of(UniqueId.of("Scheme", "PrimitiveValue")), ValueProperties.with("Function",
                  newHashSet("MarketDataSourcingFunction")).get()), (byte) 2, AggregatedExecutionLog.EMPTY), new ComputedValueResult(new ValueSpecification("Value1",
                  ComputationTargetSpecification.of(UniqueId.of("Scheme", "PrimitiveValue")), ValueProperties.with("Function", newHashSet("MarketDataSourcingFunction")).get()), (byte) 1,
                  AggregatedExecutionLog.EMPTY)), resultModel.getAllMarketData());
    } finally {
      TestLifecycle.end();
    }
  }

  /**
   * Avoids the ConcurrentHashMap-based implementation of InMemoryLKVSnapshotProvider, where the LKV map can appear to lag behind if accessed from a different thread immediately after a change.
   */
  private static class SynchronousInMemoryLKVSnapshotProvider extends InMemoryLKVMarketDataProvider {

    @Override
    public synchronized InMemoryLKVMarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
      return super.snapshot(marketDataSpec);
    }

    @Override
    public synchronized void addValue(final ValueSpecification valueSpecification, final Object value) {
      super.addValue(valueSpecification, value);
    }

    @Override
    public synchronized void removeValue(final ValueSpecification valueSpecification) {
      super.removeValue(valueSpecification);
    }

    @Override
    public MarketDataAvailabilityProvider getAvailabilityProvider(final MarketDataSpecification marketDataSpec) {
      final MarketDataAvailabilityProvider underlying = super.getAvailabilityProvider(marketDataSpec);
      return new MarketDataAvailabilityProvider() {

        @Override
        public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
          synchronized (SynchronousInMemoryLKVSnapshotProvider.this) {
            return underlying.getAvailability(targetSpec, target, desiredValue);
          }
        }

        @Override
        public MarketDataAvailabilityFilter getAvailabilityFilter() {
          throw new UnsupportedOperationException();
        }

        @Override
        public Serializable getAvailabilityHintKey() {
          return underlying.getAvailabilityHintKey();
        }

      };
    }

  }

}
