/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.worker;

import static org.testng.AssertJUnit.assertEquals;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeType;
import com.opengamma.engine.cache.MissingMarketDataSentinel;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.PermissiveMarketDataPermissionProvider;
import com.opengamma.engine.marketdata.availability.FixedMarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.live.LiveMarketDataProvider;
import com.opengamma.engine.marketdata.live.LiveMarketDataSnapshot;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.test.TestViewResultListener;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewTargetResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.impl.ViewProcessImpl;
import com.opengamma.engine.view.impl.ViewProcessorImpl;
import com.opengamma.engine.view.worker.SingleThreadViewComputationJob;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.Timeout;

/**
 * Tests {@link SingleThreadViewComputationJob}
 */
public class SingleThreadViewComputationJobTest {

  private static final long TIMEOUT = 5L * Timeout.standardTimeoutMillis();

  private static final String SOURCE_1_NAME = "source1";
  private static final String SOURCE_2_NAME = "source2";

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testAttachToUnknownView() {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    final TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    client.attachToViewProcess(UniqueId.of("not", "here"), ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().get()));
  }

  @Test
  public void testInterruptJobBetweenCycles() throws InterruptedException {
    // Due to all the dependencies between components for execution to take place, it's easiest to test it in a
    // realistic environment. In its default configuration, only live data can trigger a computation cycle (after the
    // initial cycle).
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();

    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    final TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));

    // Consume the initial result
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleCompleted(TIMEOUT);

    final ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    final SingleThreadViewComputationJob job = (SingleThreadViewComputationJob) env.getCurrentComputationJob(viewProcess);
    final Thread recalcThread = job.getThread();
    assertThreadReachesState(recalcThread, Thread.State.TIMED_WAITING);

    // We're now 'between cycles', waiting for the arrival of live data.
    // Interrupting should terminate the job gracefully
    job.getJob().terminate();
    recalcThread.interrupt();

    recalcThread.join(TIMEOUT);
    assertEquals(Thread.State.TERMINATED, recalcThread.getState());
  }

  @Test
  public void testWaitForMarketData() throws InterruptedException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    final InMemoryLKVMarketDataProvider underlyingProvider = new InMemoryLKVMarketDataProvider();
    final MarketDataProvider marketDataProvider = new TestLiveMarketDataProvider("source", underlyingProvider);
    env.setMarketDataProvider(marketDataProvider);
    env.init();

    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    final TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    final ViewCycleExecutionOptions cycleExecutionOptions = ViewCycleExecutionOptions.builder().setValuationTime(Instant.now()).setMarketDataSpecification(MarketData.live()).create();
    final EnumSet<ViewExecutionFlags> flags = ExecutionFlags.none().awaitMarketData().get();
    final ViewExecutionOptions executionOptions = ExecutionOptions.of(ArbitraryViewCycleExecutionSequence.single(cycleExecutionOptions), flags);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);

    resultListener.assertViewDefinitionCompiled(TIMEOUT);

    final ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    final Thread recalcThread = ((SingleThreadViewComputationJob) env.getCurrentComputationJob(viewProcess)).getThread();
    assertThreadReachesState(recalcThread, Thread.State.TIMED_WAITING);

    underlyingProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 123d);
    underlyingProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), 456d);
    recalcThread.join();
    resultListener.assertCycleCompleted(TIMEOUT);

    final Map<String, Object> resultValues = new HashMap<String, Object>();
    final ViewComputationResultModel result = client.getLatestResult();
    final ViewTargetResultModel targetResult = result.getTargetResult(ViewProcessorTestEnvironment.getPrimitiveTarget());
    for (final ComputedValue computedValue : targetResult.getAllValues(ViewProcessorTestEnvironment.TEST_CALC_CONFIG_NAME)) {
      resultValues.put(computedValue.getSpecification().getValueName(), computedValue.getValue());
    }

    assertEquals(123d, resultValues.get(ViewProcessorTestEnvironment.getPrimitive1().getValueName()));
    assertEquals(456d, resultValues.get(ViewProcessorTestEnvironment.getPrimitive2().getValueName()));

    resultListener.assertProcessCompleted(TIMEOUT);

    assertThreadReachesState(recalcThread, Thread.State.TERMINATED);
  }

  @Test
  public void testDoNotWaitForMarketData() throws InterruptedException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    final InMemoryLKVMarketDataProvider underlyingProvider = new InMemoryLKVMarketDataProvider();
    final MarketDataProvider marketDataProvider = new TestLiveMarketDataProvider("source", underlyingProvider);
    env.setMarketDataProvider(marketDataProvider);
    env.init();

    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    final TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    final ViewCycleExecutionOptions cycleExecutionOptions = ViewCycleExecutionOptions.builder().setValuationTime(Instant.now()).setMarketDataSpecification(MarketData.live()).create();
    final EnumSet<ViewExecutionFlags> flags = ExecutionFlags.none().get();
    final ViewExecutionOptions executionOptions = ExecutionOptions.of(ArbitraryViewCycleExecutionSequence.single(cycleExecutionOptions), flags);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);

    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleCompleted(TIMEOUT);
    resultListener.assertProcessCompleted(TIMEOUT);

    final Map<String, Object> resultValues = new HashMap<String, Object>();
    final ViewComputationResultModel result = client.getLatestResult();
    final ViewTargetResultModel targetResult = result.getTargetResult(ViewProcessorTestEnvironment.getPrimitiveTarget());
    for (final ComputedValue computedValue : targetResult.getAllValues(ViewProcessorTestEnvironment.TEST_CALC_CONFIG_NAME)) {
      resultValues.put(computedValue.getSpecification().getValueName(), computedValue.getValue());
    }
    assertEquals(MissingMarketDataSentinel.getInstance(), resultValues.get(ViewProcessorTestEnvironment.getPrimitive1().getValueName()));
    assertEquals(MissingMarketDataSentinel.getInstance(), resultValues.get(ViewProcessorTestEnvironment.getPrimitive2().getValueName()));
  }

  @Test
  public void testChangeMarketDataProviderBetweenCycles() throws InterruptedException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    final InMemoryLKVMarketDataProvider underlyingProvider1 = new InMemoryLKVMarketDataProvider();
    final MarketDataProvider provider1 = new TestLiveMarketDataProvider(SOURCE_1_NAME, underlyingProvider1);
    final InMemoryLKVMarketDataProvider underlyingProvider2 = new InMemoryLKVMarketDataProvider();
    final MarketDataProvider provider2 = new TestLiveMarketDataProvider(SOURCE_2_NAME, underlyingProvider2);
    env.setMarketDataProviderResolver(new DualLiveMarketDataProviderResolver(SOURCE_1_NAME, provider1, SOURCE_2_NAME, provider2));
    env.init();

    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    final TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    final Instant valuationTime = Instant.now();
    final ViewCycleExecutionOptions.Builder builder = ViewCycleExecutionOptions.builder().setValuationTime(valuationTime);
    final ViewCycleExecutionOptions cycle1 = builder.setMarketDataSpecification(MarketData.live(SOURCE_1_NAME)).create();
    final ViewCycleExecutionOptions cycle2 = builder.setMarketDataSpecification(MarketData.live(SOURCE_2_NAME)).create();
    final EnumSet<ViewExecutionFlags> flags = ExecutionFlags.none().runAsFastAsPossible().get();
    final ViewExecutionOptions executionOptions = ExecutionOptions.of(ArbitraryViewCycleExecutionSequence.of(cycle1, cycle2), flags);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);

    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleCompleted(TIMEOUT);
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    // Change of market data provider should cause a further compilation
    resultListener.assertCycleCompleted(TIMEOUT);
    resultListener.assertProcessCompleted(TIMEOUT);
  }

  @Test
  public void testChangeMarketDataProviderBetweenCyclesWithCycleFragmentCompletedCalls() throws InterruptedException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    final InMemoryLKVMarketDataProvider underlyingProvider1 = new InMemoryLKVMarketDataProvider();
    final MarketDataProvider provider1 = new TestLiveMarketDataProvider(SOURCE_1_NAME, underlyingProvider1);
    final InMemoryLKVMarketDataProvider underlyingProvider2 = new InMemoryLKVMarketDataProvider();
    final MarketDataProvider provider2 = new TestLiveMarketDataProvider(SOURCE_2_NAME, underlyingProvider2);
    env.setMarketDataProviderResolver(new DualLiveMarketDataProviderResolver(SOURCE_1_NAME, provider1, SOURCE_2_NAME, provider2));
    env.init();

    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    final TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    final Instant valuationTime = Instant.now();
    final ViewCycleExecutionOptions.Builder builder = ViewCycleExecutionOptions.builder().setValuationTime(valuationTime);
    final ViewCycleExecutionOptions cycle1 = builder.setMarketDataSpecification(MarketData.live(SOURCE_1_NAME)).create();
    final ViewCycleExecutionOptions cycle2 = builder.setMarketDataSpecification(MarketData.live(SOURCE_2_NAME)).create();
    final EnumSet<ViewExecutionFlags> flags = ExecutionFlags.none().runAsFastAsPossible().get();
    final ViewExecutionOptions executionOptions = ExecutionOptions.of(ArbitraryViewCycleExecutionSequence.of(cycle1, cycle2), flags);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);

    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleCompleted(TIMEOUT);
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    // Change of market data provider should cause a further compilation
    resultListener.assertCycleCompleted(TIMEOUT);
    resultListener.assertProcessCompleted(TIMEOUT);
  }

  @Test
  public void testTriggerCycle() {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();

    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    final TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    final EnumSet<ViewExecutionFlags> flags = ExecutionFlags.none().get();
    final ViewExecutionOptions viewExecutionOptions = ExecutionOptions.infinite(MarketData.live(), flags);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), viewExecutionOptions);

    final ViewComputationJob computationJob = env.getCurrentComputationJob(env.getViewProcess(vp, client.getUniqueId()));

    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleCompleted(TIMEOUT);
    computationJob.triggerCycle();
    resultListener.assertCycleCompleted(TIMEOUT);

    client.shutdown();
  }

  @Test
  public void testUpdateViewDefinitionCausesRecompile() {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();

    final ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();

    final ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    final TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    final EnumSet<ViewExecutionFlags> flags = ExecutionFlags.none().get();
    final ViewExecutionOptions viewExecutionOptions = ExecutionOptions.infinite(MarketData.live(), flags);
    final UniqueId viewDefinitionId = env.getViewDefinition().getUniqueId();
    client.attachToViewProcess(viewDefinitionId, viewExecutionOptions);

    final ViewComputationJob computationJob = env.getCurrentComputationJob(env.getViewProcess(vp, client.getUniqueId()));

    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleCompleted(TIMEOUT);
    computationJob.triggerCycle();
    resultListener.assertCycleCompleted(TIMEOUT);
    env.getConfigSource().changeManager().entityChanged(ChangeType.CHANGED, viewDefinitionId.getObjectId(), null, null, Instant.now());
    computationJob.triggerCycle();
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleCompleted(TIMEOUT);
    computationJob.triggerCycle();
    resultListener.assertCycleCompleted(TIMEOUT);

    client.shutdown();
  }

  private void assertThreadReachesState(final Thread recalcThread, final Thread.State state) throws InterruptedException {
    final long startTime = System.currentTimeMillis();
    while (recalcThread.getState() != state) {
      Thread.sleep(50);
      if (System.currentTimeMillis() - startTime > TIMEOUT) {
        throw new OpenGammaRuntimeException("Waited longer than " + TIMEOUT + " ms for the recalc thread to reach state " + state);
      }
    }
  }

  private static class TestLiveMarketDataProvider implements MarketDataProvider {

    private final String _sourceName;
    private final InMemoryLKVMarketDataProvider _underlyingProvider;
    private final FixedMarketDataAvailabilityProvider _availability;
    private final LiveDataClient _dummyLiveDataClient = new LiveDataClient() {

      @Override
      public Map<LiveDataSpecification, Boolean> isEntitled(final UserPrincipal user, final Collection<LiveDataSpecification> requestedSpecifications) {
        return null;
      }

      @Override
      public boolean isEntitled(final UserPrincipal user, final LiveDataSpecification requestedSpecification) {
        return false;
      }

      @Override
      public void unsubscribe(final UserPrincipal user, final Collection<LiveDataSpecification> fullyQualifiedSpecifications, final LiveDataListener listener) {
      }

      @Override
      public void unsubscribe(final UserPrincipal user, final LiveDataSpecification fullyQualifiedSpecification, final LiveDataListener listener) {
      }

      @Override
      public void subscribe(final UserPrincipal user, final Collection<LiveDataSpecification> requestedSpecifications, final LiveDataListener listener) {
      }

      @Override
      public void subscribe(final UserPrincipal user, final LiveDataSpecification requestedSpecification, final LiveDataListener listener) {
      }

      @Override
      public Collection<LiveDataSubscriptionResponse> snapshot(final UserPrincipal user, final Collection<LiveDataSpecification> requestedSpecifications, final long timeout) {
        return null;
      }

      @Override
      public LiveDataSubscriptionResponse snapshot(final UserPrincipal user, final LiveDataSpecification requestedSpecification, final long timeout) {
        return null;
      }

      @Override
      public String getDefaultNormalizationRuleSetId() {
        return null;
      }

      @Override
      public void close() {
      }

    };

    public TestLiveMarketDataProvider(final String sourceName, final InMemoryLKVMarketDataProvider underlyingProvider) {
      ArgumentChecker.notNull(sourceName, "sourceName");
      _sourceName = sourceName;
      _underlyingProvider = underlyingProvider;
      _availability = new FixedMarketDataAvailabilityProvider();
      _availability.addAvailableData(_availability.resolveRequirement(ViewProcessorTestEnvironment.getPrimitive1()));
      _availability.addAvailableData(_availability.resolveRequirement(ViewProcessorTestEnvironment.getPrimitive2()));
    }

    @Override
    public void addListener(final MarketDataListener listener) {
      _underlyingProvider.addListener(listener);
    }

    @Override
    public void removeListener(final MarketDataListener listener) {
      _underlyingProvider.removeListener(listener);
    }

    @Override
    public void subscribe(final ValueSpecification valueSpecification) {
      _underlyingProvider.subscribe(valueSpecification);
    }

    @Override
    public void subscribe(final Set<ValueSpecification> valueSpecifications) {
      _underlyingProvider.subscribe(valueSpecifications);
    }

    @Override
    public void unsubscribe(final ValueSpecification valueSpecification) {
      _underlyingProvider.unsubscribe(valueSpecification);
    }

    @Override
    public void unsubscribe(final Set<ValueSpecification> valueSpecifications) {
      _underlyingProvider.unsubscribe(valueSpecifications);
    }

    @Override
    public MarketDataAvailabilityProvider getAvailabilityProvider(final MarketDataSpecification marketDataSpec) {
      return _availability;
    }

    @Override
    public MarketDataPermissionProvider getPermissionProvider() {
      return new PermissiveMarketDataPermissionProvider();
    }

    @Override
    public boolean isCompatible(final MarketDataSpecification marketDataSpec) {
      if (!(marketDataSpec instanceof LiveMarketDataSpecification)) {
        return false;
      }
      final LiveMarketDataSpecification liveMarketDataSpec = (LiveMarketDataSpecification) marketDataSpec;
      return _sourceName.equals(liveMarketDataSpec.getDataSource());
    }

    @Override
    public MarketDataSnapshot snapshot(final MarketDataSpecification marketDataSpec) {
      return new LiveMarketDataSnapshot(_underlyingProvider.snapshot(marketDataSpec),
          new LiveMarketDataProvider(_dummyLiveDataClient, getAvailabilityProvider(marketDataSpec).getAvailabilityFilter(), UserPrincipal.getTestUser()));
    }

    @Override
    public Duration getRealTimeDuration(final Instant fromInstant, final Instant toInstant) {
      return Duration.between(fromInstant, toInstant);
    }

  }

  private static class DualLiveMarketDataProviderResolver implements MarketDataProviderResolver {

    private final String _provider1SourceName;
    private final MarketDataProvider _provider1;
    private final String _provider2SourceName;
    private final MarketDataProvider _provider2;

    public DualLiveMarketDataProviderResolver(final String provider1SourceName, final MarketDataProvider provider1, final String provider2SourceName, final MarketDataProvider provider2) {
      _provider1SourceName = provider1SourceName;
      _provider1 = provider1;
      _provider2SourceName = provider2SourceName;
      _provider2 = provider2;
    }

    @Override
    public MarketDataProvider resolve(final UserPrincipal user, final MarketDataSpecification snapshotSpec) {
      if (_provider1SourceName.equals(((LiveMarketDataSpecification) snapshotSpec).getDataSource())) {
        return _provider1;
      }
      if (_provider2SourceName.equals(((LiveMarketDataSpecification) snapshotSpec).getDataSource())) {
        return _provider2;
      }
      throw new IllegalArgumentException("Unknown data source name");
    }

  }

}
