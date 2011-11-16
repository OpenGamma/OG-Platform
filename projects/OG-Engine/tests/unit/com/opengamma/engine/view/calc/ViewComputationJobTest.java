/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeType;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.LiveMarketDataProvider;
import com.opengamma.engine.marketdata.LiveMarketDataSnapshot;
import com.opengamma.engine.marketdata.MarketDataListener;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.marketdata.MarketDataSnapshot;
import com.opengamma.engine.marketdata.availability.MarketDataAvailability;
import com.opengamma.engine.marketdata.availability.MarketDataAvailabilityProvider;
import com.opengamma.engine.marketdata.permission.MarketDataPermissionProvider;
import com.opengamma.engine.marketdata.permission.PermissiveMarketDataPermissionProvider;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketData;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.test.MockSecuritySource;
import com.opengamma.engine.test.TestViewResultListener;
import com.opengamma.engine.test.ViewProcessorTestEnvironment;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewProcessImpl;
import com.opengamma.engine.view.ViewProcessorImpl;
import com.opengamma.engine.view.ViewTargetResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.client.ViewResultMode;
import com.opengamma.engine.view.execution.ArbitraryViewCycleExecutionSequence;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.engine.view.listener.JobResultReceivedCall;
import com.opengamma.id.UniqueId;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.test.Timeout;
import org.testng.annotations.Test;

import javax.time.Duration;
import javax.time.Instant;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

/**
 * Tests {@link ViewComputationJob}
 */
public class ViewComputationJobTest {

  private static final long TIMEOUT = 5L * Timeout.standardTimeoutMillis();
  
  private static final String SOURCE_1_NAME = "source1";
  private static final String SOURCE_2_NAME = "source2";
  
  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void testAttachToUnknownView() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    client.attachToViewProcess(UniqueId.of("not", "here"), ExecutionOptions.infinite(MarketData.live(), ExecutionFlags.none().get()));
  }
  
  @Test
  public void testInterruptJobBetweenCycles() throws InterruptedException {
    // Due to all the dependencies between components for execution to take place, it's easiest to test it in a
    // realistic environment. In its default configuration, only live data can trigger a computation cycle (after the
    // initial cycle).
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), ExecutionOptions.infinite(MarketData.live()));
    
    // Consume the initial result
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleCompleted(TIMEOUT); 
    
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    Thread recalcThread = env.getCurrentComputationThread(viewProcess);
    assertThreadReachesState(recalcThread, Thread.State.TIMED_WAITING);
    
    // We're now 'between cycles', waiting for the arrival of live data.
    // Interrupting should terminate the job gracefully
    ViewComputationJob job = env.getCurrentComputationJob(viewProcess);
    job.terminate();
    recalcThread.interrupt();
    
    recalcThread.join(TIMEOUT);
    assertEquals(Thread.State.TERMINATED, recalcThread.getState());
  }
  
  @Test
  public void testWaitForMarketData() throws InterruptedException {
    final ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    InMemoryLKVMarketDataProvider underlyingProvider = new InMemoryLKVMarketDataProvider();
    MarketDataProvider marketDataProvider = new TestLiveMarketDataProvider("source", underlyingProvider);
    env.setMarketDataProvider(marketDataProvider);
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    ViewCycleExecutionOptions cycleExecutionOptions = new ViewCycleExecutionOptions(Instant.now(), MarketData.live());
    EnumSet<ViewExecutionFlags> flags = ExecutionFlags.none().awaitMarketData().get();
    ViewExecutionOptions executionOptions = ExecutionOptions.of(ArbitraryViewCycleExecutionSequence.single(cycleExecutionOptions), flags);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);
    
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    
    ViewProcessImpl viewProcess = env.getViewProcess(vp, client.getUniqueId());
    Thread recalcThread = env.getCurrentComputationThread(viewProcess);
    assertThreadReachesState(recalcThread, Thread.State.TIMED_WAITING);
    
    underlyingProvider.addValue(ViewProcessorTestEnvironment.getPrimitive1(), 123d);
    underlyingProvider.addValue(ViewProcessorTestEnvironment.getPrimitive2(), 456d);
    recalcThread.join();
    resultListener.assertCycleCompleted(TIMEOUT);
    
    Map<String, Object> resultValues = new HashMap<String, Object>();
    ViewComputationResultModel result = client.getLatestResult();
    ViewTargetResultModel targetResult = result.getTargetResult(ViewProcessorTestEnvironment.getPrimitive1().getTargetSpecification());
    for (ComputedValue computedValue : targetResult.getAllValues(ViewProcessorTestEnvironment.TEST_CALC_CONFIG_NAME)) {
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
    InMemoryLKVMarketDataProvider underlyingProvider = new InMemoryLKVMarketDataProvider();
    MarketDataProvider marketDataProvider = new TestLiveMarketDataProvider("source", underlyingProvider);
    env.setMarketDataProvider(marketDataProvider);
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    ViewCycleExecutionOptions cycleExecutionOptions = new ViewCycleExecutionOptions(Instant.now(), MarketData.live());
    EnumSet<ViewExecutionFlags> flags = ExecutionFlags.none().get();
    ViewExecutionOptions executionOptions = ExecutionOptions.of(ArbitraryViewCycleExecutionSequence.single(cycleExecutionOptions), flags);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);
    
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleCompleted(TIMEOUT);
    resultListener.assertProcessCompleted(TIMEOUT);
    
    ViewComputationResultModel result = client.getLatestResult();
    ViewTargetResultModel targetResult = result.getTargetResult(ViewProcessorTestEnvironment.getPrimitive1().getTargetSpecification());
    assertNull(targetResult);
  }
  
  @Test
  public void testChangeMarketDataProviderBetweenCycles() throws InterruptedException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    InMemoryLKVMarketDataProvider underlyingProvider1 = new InMemoryLKVMarketDataProvider();
    MarketDataProvider provider1 = new TestLiveMarketDataProvider(SOURCE_1_NAME, underlyingProvider1);
    InMemoryLKVMarketDataProvider underlyingProvider2 = new InMemoryLKVMarketDataProvider();
    MarketDataProvider provider2 = new TestLiveMarketDataProvider(SOURCE_2_NAME, underlyingProvider2);
    env.setMarketDataProviderResolver(new DualLiveMarketDataProviderResolver(SOURCE_1_NAME, provider1, SOURCE_2_NAME, provider2));
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    Instant valuationTime = Instant.now();
    ViewCycleExecutionOptions cycle1 = new ViewCycleExecutionOptions(valuationTime, MarketData.live(SOURCE_1_NAME));
    ViewCycleExecutionOptions cycle2 = new ViewCycleExecutionOptions(valuationTime, MarketData.live(SOURCE_2_NAME));
    EnumSet<ViewExecutionFlags> flags = ExecutionFlags.none().runAsFastAsPossible().get();
    ViewExecutionOptions executionOptions = ExecutionOptions.of(ArbitraryViewCycleExecutionSequence.of(cycle1, cycle2), flags);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), executionOptions);
    
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleCompleted(TIMEOUT);
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    // Change of market data provider should cause a further compilation
    resultListener.assertCycleCompleted(TIMEOUT);
    resultListener.assertProcessCompleted(TIMEOUT);
  }
  
  
  @Test
  public void testChangeMarketDataProviderBetweenCyclesWithJobResultReceivedCalls() throws InterruptedException {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    InMemoryLKVMarketDataProvider underlyingProvider1 = new InMemoryLKVMarketDataProvider();
    MarketDataProvider provider1 = new TestLiveMarketDataProvider(SOURCE_1_NAME, underlyingProvider1);
    InMemoryLKVMarketDataProvider underlyingProvider2 = new InMemoryLKVMarketDataProvider();
    MarketDataProvider provider2 = new TestLiveMarketDataProvider(SOURCE_2_NAME, underlyingProvider2);
    env.setMarketDataProviderResolver(new DualLiveMarketDataProviderResolver(SOURCE_1_NAME, provider1, SOURCE_2_NAME, provider2));
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    Instant valuationTime = Instant.now();
    ViewCycleExecutionOptions cycle1 = new ViewCycleExecutionOptions(valuationTime, MarketData.live(SOURCE_1_NAME));
    ViewCycleExecutionOptions cycle2 = new ViewCycleExecutionOptions(valuationTime, MarketData.live(SOURCE_2_NAME));
    EnumSet<ViewExecutionFlags> flags = ExecutionFlags.none().runAsFastAsPossible().get();
    ViewExecutionOptions executionOptions = ExecutionOptions.of(ArbitraryViewCycleExecutionSequence.of(cycle1, cycle2), flags);
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
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    EnumSet<ViewExecutionFlags> flags = ExecutionFlags.none().get();
    ViewExecutionOptions viewExecutionOptions = ExecutionOptions.infinite(MarketData.live(), flags);
    client.attachToViewProcess(env.getViewDefinition().getUniqueId(), viewExecutionOptions);
    
    ViewComputationJob computationJob = env.getCurrentComputationJob(env.getViewProcess(vp, client.getUniqueId()));
    
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleCompleted(TIMEOUT);
    computationJob.triggerCycle();
    resultListener.assertCycleCompleted(TIMEOUT);
    
    client.shutdown();
  }

  
  @Test
  public void testUpdateViewDefinitionCausesRecompile() {
    ViewProcessorTestEnvironment env = new ViewProcessorTestEnvironment();
    env.init();
    
    ViewProcessorImpl vp = env.getViewProcessor();
    vp.start();
    
    ViewClient client = vp.createViewClient(ViewProcessorTestEnvironment.TEST_USER);
    TestViewResultListener resultListener = new TestViewResultListener();
    client.setResultListener(resultListener);
    EnumSet<ViewExecutionFlags> flags = ExecutionFlags.none().get();
    ViewExecutionOptions viewExecutionOptions = ExecutionOptions.infinite(MarketData.live(), flags);
    final UniqueId viewDefinitionId = env.getViewDefinition().getUniqueId();
    client.attachToViewProcess(viewDefinitionId, viewExecutionOptions);
    
    ViewComputationJob computationJob = env.getCurrentComputationJob(env.getViewProcess(vp, client.getUniqueId()));
    
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleCompleted(TIMEOUT);
    computationJob.triggerCycle();
    resultListener.assertCycleCompleted(TIMEOUT);
    env.getViewDefinitionRepository().changeManager().entityChanged(ChangeType.UPDATED, viewDefinitionId, viewDefinitionId, Instant.now());
    computationJob.triggerCycle();
    resultListener.assertViewDefinitionCompiled(TIMEOUT);
    resultListener.assertCycleCompleted(TIMEOUT);
    computationJob.triggerCycle();
    resultListener.assertCycleCompleted(TIMEOUT);
    
    client.shutdown();
  }
  
  private void assertThreadReachesState(Thread recalcThread, Thread.State state) throws InterruptedException {
    long startTime = System.currentTimeMillis();
    while (recalcThread.getState() != state) {
      Thread.sleep(50);
      if (System.currentTimeMillis() - startTime > TIMEOUT) {
        throw new OpenGammaRuntimeException("Waited longer than " + TIMEOUT + " ms for the recalc thread to reach state " + state); 
      }
    }
  }
  
  private static class TestLiveMarketDataProvider implements MarketDataProvider, MarketDataAvailabilityProvider {

    private final String _sourceName;
    private final InMemoryLKVMarketDataProvider _underlyingProvider;
    private final LiveDataClient _dummyLiveDataClient = new LiveDataClient() {

      @Override
      public Map<LiveDataSpecification, Boolean> isEntitled(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications) {
        return null;
      }

      @Override
      public boolean isEntitled(UserPrincipal user, LiveDataSpecification requestedSpecification) {
        return false;
      }

      @Override
      public void unsubscribe(UserPrincipal user, Collection<LiveDataSpecification> fullyQualifiedSpecifications, LiveDataListener listener) {
      }

      @Override
      public void unsubscribe(UserPrincipal user, LiveDataSpecification fullyQualifiedSpecification, LiveDataListener listener) {
      }

      @Override
      public void subscribe(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications, LiveDataListener listener) {
      }

      @Override
      public void subscribe(UserPrincipal user, LiveDataSpecification requestedSpecification, LiveDataListener listener) {
      }

      @Override
      public Collection<LiveDataSubscriptionResponse> snapshot(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications, long timeout) {
        return null;
      }

      @Override
      public LiveDataSubscriptionResponse snapshot(UserPrincipal user, LiveDataSpecification requestedSpecification, long timeout) {
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
    
    public TestLiveMarketDataProvider(String sourceName, InMemoryLKVMarketDataProvider underlyingProvider) {
      ArgumentChecker.notNull(sourceName, "sourceName");
      _sourceName = sourceName;
      _underlyingProvider = underlyingProvider;
    }
    
    @Override
    public void addListener(MarketDataListener listener) {
      _underlyingProvider.addListener(listener);
    }

    @Override
    public void removeListener(MarketDataListener listener) {
      _underlyingProvider.removeListener(listener);
    }
    
    @Override
    public void subscribe(UserPrincipal user, ValueRequirement valueRequirement) {
      _underlyingProvider.subscribe(user, valueRequirement);
    }

    @Override
    public void subscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
      _underlyingProvider.subscribe(user, valueRequirements);
    }

    @Override
    public void unsubscribe(UserPrincipal user, ValueRequirement valueRequirement) {
      _underlyingProvider.unsubscribe(user, valueRequirement);
    }

    @Override
    public void unsubscribe(UserPrincipal user, Set<ValueRequirement> valueRequirements) {
      _underlyingProvider.unsubscribe(user, valueRequirements);
    }

    @Override
    public MarketDataAvailabilityProvider getAvailabilityProvider() {
      return this;
    }

    @Override
    public MarketDataPermissionProvider getPermissionProvider() {
      return new PermissiveMarketDataPermissionProvider();
    }

    @Override
    public boolean isCompatible(MarketDataSpecification marketDataSpec) {
      if (!(marketDataSpec instanceof LiveMarketDataSpecification)) {
        return false;
      }
      LiveMarketDataSpecification liveMarketDataSpec = (LiveMarketDataSpecification) marketDataSpec;
      return _sourceName.equals(liveMarketDataSpec.getDataSource());
    }

    @Override
    public MarketDataSnapshot snapshot(MarketDataSpecification marketDataSpec) {
      final SecuritySource dummySecuritySource = new MockSecuritySource();
      return new LiveMarketDataSnapshot(_underlyingProvider.snapshot(marketDataSpec), new LiveMarketDataProvider(_dummyLiveDataClient, dummySecuritySource, getAvailabilityProvider()));
    }

    @Override
    public MarketDataAvailability getAvailability(ValueRequirement requirement) {
      // Want the market data provider to indicate that data is available even before it's really available
      return (requirement.equals(ViewProcessorTestEnvironment.getPrimitive1()) || requirement.equals(ViewProcessorTestEnvironment.getPrimitive2())) ? MarketDataAvailability.AVAILABLE
          : MarketDataAvailability.NOT_AVAILABLE;
    }

    @Override
    public Duration getRealTimeDuration(Instant fromInstant, Instant toInstant) {
      return Duration.between(fromInstant, toInstant);
    }
    
  }
  
  private static class DualLiveMarketDataProviderResolver implements MarketDataProviderResolver {

    private final String _provider1SourceName;
    private final MarketDataProvider _provider1;
    private final String _provider2SourceName;
    private final MarketDataProvider _provider2;
    
    public DualLiveMarketDataProviderResolver(String provider1SourceName, MarketDataProvider provider1, String provider2SourceName, MarketDataProvider provider2) {
      _provider1SourceName = provider1SourceName;
      _provider1 = provider1;
      _provider2SourceName = provider2SourceName;
      _provider2 = provider2;
    }

    @Override
    public MarketDataProvider resolve(MarketDataSpecification snapshotSpec) {
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
