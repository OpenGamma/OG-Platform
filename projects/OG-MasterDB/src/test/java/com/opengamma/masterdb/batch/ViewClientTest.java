/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static com.opengamma.util.functional.Functional.first;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.marketdata.AbstractMarketDataProvider;
import com.opengamma.engine.marketdata.AbstractMarketDataSnapshot;
import com.opengamma.engine.marketdata.MarketDataInjector;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.MarketDataUtils;
import com.opengamma.engine.marketdata.PermissiveMarketDataPermissionProvider;
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
import com.opengamma.engine.view.ViewProcessImpl;
import com.opengamma.engine.view.ViewProcessState;
import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.engine.view.calc.ViewCycleMetadata;
import com.opengamma.engine.view.calc.ViewResultListenerFactory;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.id.UniqueId;

public class ViewClientTest {

  @Mock
  private ViewResultListenerFactory viewResultListenerFactoryStub;
  @Mock
  private ViewResultListener viewResultListenerMock;

  @BeforeMethod
  public void setUp() throws Exception {
    initMocks(this);
    when(viewResultListenerFactoryStub.createViewResultListener()).thenReturn(viewResultListenerMock);
  }

  @Test
  public void testListenerNotifications() throws InterruptedException {
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
    assertEquals("Test Calc Config", first(argument.getValue().getAllCalculationConfigurationNames()));

    final ArgumentCaptor<ViewComputationResultModel> fullFragment = ArgumentCaptor.forClass(ViewComputationResultModel.class);
    final ArgumentCaptor<ViewDeltaResultModel> deltaFragment = ArgumentCaptor.forClass(ViewDeltaResultModel.class);
    verify(viewResultListenerMock).cycleFragmentCompleted(fullFragment.capture(), deltaFragment.capture());

    assertEquals(UniqueId.of("ViewProcess", client.getUniqueId().getValue()), fullFragment.getValue().getViewProcessId());
    assertEquals(UniqueId.of("ViewCycle", client.getUniqueId().getValue(), "0"), fullFragment.getValue().getViewCycleId());

    assertEquals(
        newHashSet(
            new ComputedValueResult(
                new ValueSpecification(
                    "Value2",
                    ComputationTargetSpecification.of(UniqueId.of("Scheme", "PrimitiveValue")),
                    ValueProperties.with("Function", newHashSet("MarketDataSourcingFunction")).get()),
                    (byte) 2, AggregatedExecutionLog.EMPTY),
                    new ComputedValueResult(
                        new ValueSpecification(
                            "Value1",
                            ComputationTargetSpecification.of(UniqueId.of("Scheme", "PrimitiveValue")),
                            ValueProperties.with("Function", newHashSet("MarketDataSourcingFunction")).get()),
                            (byte) 1, AggregatedExecutionLog.EMPTY)
            ),
            fullFragment.getValue().getAllMarketData());

    assertEquals(newHashMap(),
        fullFragment.getValue().getRequirementToSpecificationMapping());
  }


  /**
   * Avoids the ConcurrentHashMap-based implementation of InMemoryLKVSnapshotProvider, where the LKV map can appear to
   * lag behind if accessed from a different thread immediately after a change.
   */
  private static class SynchronousInMemoryLKVSnapshotProvider extends AbstractMarketDataProvider implements MarketDataInjector,
  MarketDataAvailabilityProvider {

    private static final Logger s_logger = LoggerFactory.getLogger(SynchronousInMemoryLKVSnapshotProvider.class);

    private final Map<ValueSpecification, Object> _lastKnownValues = new HashMap<ValueSpecification, Object>();
    private final MarketDataPermissionProvider _permissionProvider = new PermissiveMarketDataPermissionProvider();

    @Override
    public void subscribe(final ValueSpecification valueSpecification) {
      subscribe(Collections.singleton(valueSpecification));
    }

    @Override
    public void subscribe(final Set<ValueSpecification> valueSpecifications) {
      // No actual subscription to make, but we still need to acknowledge it.
      subscriptionSucceeded(valueSpecifications);
    }

    @Override
    public void unsubscribe(final ValueSpecification valueSpecification) {
    }

    @Override
    public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
    }

    //-----------------------------------------------------------------------
    @Override
    public MarketDataAvailabilityProvider getAvailabilityProvider() {
      return this;
    }

    @Override
    public MarketDataPermissionProvider getPermissionProvider() {
      return _permissionProvider;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
      return true;
    }

    @Override
    public MarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
      synchronized (_lastKnownValues) {
        final Map<ValueSpecification, Object> snapshotValues = new HashMap<ValueSpecification, Object>(_lastKnownValues);
        return new SynchronousInMemoryLKVSnapshot(snapshotValues);
      }
    }

    //-----------------------------------------------------------------------
    @Override
    public void addValue(final ValueSpecification valueSpecification, final Object value) {
      s_logger.debug("Setting {} = {}", valueSpecification, value);
      synchronized (_lastKnownValues) {
        _lastKnownValues.put(valueSpecification, value);
      }
      // Don't notify listeners of the change - we'll kick off a computation cycle manually in the tests
    }

    @Override
    public void addValue(final ValueRequirement valueRequirement, final Object value) {
      addValue(MarketDataUtils.createMarketDataValue(valueRequirement, MarketDataUtils.DEFAULT_EXTERNAL_ID), value);
    }

    @Override
    public void removeValue(final ValueSpecification valueSpecification) {
      synchronized(_lastKnownValues) {
        _lastKnownValues.remove(valueSpecification);
      }
      // Don't notify listeners of the change - we'll kick off a computation cycle manually in the tests
    }

    @Override
    public void removeValue(final ValueRequirement valueRequirement) {
      removeValue(MarketDataUtils.createMarketDataValue(valueRequirement, MarketDataUtils.DEFAULT_EXTERNAL_ID));
    }

    //-----------------------------------------------------------------------
    @Override
    public ValueSpecification getAvailability(final ComputationTargetSpecification targetSpec, final Object target, final ValueRequirement desiredValue) {
      // [PLAT-3044] Do this properly
      synchronized (_lastKnownValues) {
        return _lastKnownValues.containsKey(desiredValue) ? MarketDataUtils.createMarketDataValue(desiredValue, MarketDataUtils.DEFAULT_EXTERNAL_ID) : null;
      }
    }

  }

  private static class SynchronousInMemoryLKVSnapshot extends AbstractMarketDataSnapshot {

    private final Map<ValueSpecification, Object> _snapshot;
    private final Instant _snapshotTime = Instant.now();

    public SynchronousInMemoryLKVSnapshot(final Map<ValueSpecification, Object> snapshot) {
      _snapshot = snapshot;
    }

    @Override
    public UniqueId getUniqueId() {
      return UniqueId.of(MARKET_DATA_SNAPSHOT_ID_SCHEME, "SynchronousInMemoryLKVSnapshot:"+getSnapshotTime());
    }

    @Override
    public Instant getSnapshotTimeIndication() {
      return _snapshotTime;
    }

    @Override
    public Instant getSnapshotTime() {
      return _snapshotTime;
    }

    @Override
    public Object query(final ValueSpecification specification) {
      return _snapshot.get(specification);
    }

  }

}
