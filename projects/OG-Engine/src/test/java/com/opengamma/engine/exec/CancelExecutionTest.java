/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.exec;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.threeten.bp.Instant;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.engine.cache.InMemoryViewComputationCacheSource;
import com.opengamma.engine.cache.ViewComputationCacheSource;
import com.opengamma.engine.calcnode.CalculationNodeLogEventListener;
import com.opengamma.engine.calcnode.JobDispatcher;
import com.opengamma.engine.calcnode.LocalNodeJobInvoker;
import com.opengamma.engine.calcnode.SimpleCalculationNode;
import com.opengamma.engine.calcnode.stats.DiscardingInvocationStatisticsGatherer;
import com.opengamma.engine.calcnode.stats.FunctionInvocationStatisticsGatherer;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyGraphBuilderFactory;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.impl.DependencyGraphImpl;
import com.opengamma.engine.depgraph.impl.DependencyNodeFunctionImpl;
import com.opengamma.engine.depgraph.impl.DependencyNodeImpl;
import com.opengamma.engine.exec.stats.DiscardingGraphStatisticsGathererProvider;
import com.opengamma.engine.exec.stats.GraphExecutorStatisticsGathererProvider;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.DummyOverrideOperationCompiler;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.SingletonMarketDataProviderFactory;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.resolver.SingleMarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.resource.EngineResourceManagerImpl;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.test.MockConfigSource;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfiguration;
import com.opengamma.engine.view.compilation.CompiledViewCalculationConfigurationImpl;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.cycle.SingleComputationCycle;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.impl.ViewProcessContext;
import com.opengamma.engine.view.listener.ComputationResultListener;
import com.opengamma.engine.view.permission.DefaultViewPermissionProvider;
import com.opengamma.engine.view.permission.DefaultViewPortfolioPermissionProvider;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.engine.view.worker.SingleThreadViewProcessWorkerFactory;
import com.opengamma.engine.view.worker.cache.InMemoryViewExecutionCache;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.id.VersionedUniqueIdSupplier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.log.ThreadLocalLogEventListener;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;

/**
 * Test.
 */
@Test(groups = TestGroup.INTEGRATION)
public class CancelExecutionTest {

  private static final int JOB_SIZE = 100;
  private static final int JOB_FINISH_TIME = (int) Timeout.standardTimeoutMillis();
  private static final int SLEEP_TIME = JOB_FINISH_TIME / 10;
  private static final Logger s_logger = LoggerFactory.getLogger(CancelExecutionTest.class);

  @DataProvider(name = "executors")
  Object[][] data_executors() {
    return new Object[][] { {multipleNodeExecutorFactoryManyJobs() }, {multipleNodeExecutorFactoryOneJob() }, {new SingleNodeExecutorFactory() }, };
  }

  //-------------------------------------------------------------------------
  private static MultipleNodeExecutorFactory multipleNodeExecutorFactoryOneJob() {
    final MultipleNodeExecutorFactory factory = new MultipleNodeExecutorFactory();
    factory.afterPropertiesSet();
    return factory;
  }

  private static MultipleNodeExecutorFactory multipleNodeExecutorFactoryManyJobs() {
    final MultipleNodeExecutorFactory factory = multipleNodeExecutorFactoryOneJob();
    factory.setMaximumJobItems(JOB_SIZE / 10);
    return factory;
  }

  private final AtomicInteger _functionCount = new AtomicInteger();

  private void sleep() {
    try {
      Thread.sleep(SLEEP_TIME);
    } catch (final InterruptedException e) {
    }
  }

  private DependencyGraphExecutionFuture executeTestJob(final DependencyGraphExecutorFactory factory) {
    final InMemoryLKVMarketDataProvider marketDataProvider = new InMemoryLKVMarketDataProvider();
    final MarketDataProviderResolver marketDataProviderResolver = new SingleMarketDataProviderResolver(new SingletonMarketDataProviderFactory(marketDataProvider));
    final InMemoryFunctionRepository functionRepository = new InMemoryFunctionRepository();
    _functionCount.set(0);
    final MockFunction mockFunction = new MockFunction(ComputationTarget.NULL) {
      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) {
        try {
          Thread.sleep(JOB_FINISH_TIME / (JOB_SIZE * 2));
        } catch (final InterruptedException e) {
          throw new OpenGammaRuntimeException("Function interrupted", e);
        }
        _functionCount.incrementAndGet();
        return super.execute(executionContext, inputs, target, desiredValues);
      }
    };
    functionRepository.addFunction(mockFunction);
    final FunctionCompilationContext compilationContext = new FunctionCompilationContext();
    final CompiledFunctionService compilationService = new CompiledFunctionService(functionRepository, new CachingFunctionRepositoryCompiler(), compilationContext);
    compilationService.initialize();
    final FunctionResolver functionResolver = new DefaultFunctionResolver(compilationService);
    final InMemorySecuritySource securitySource = new InMemorySecuritySource();
    final MockPositionSource positionSource = new MockPositionSource();
    compilationContext.setRawComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource));
    final ViewComputationCacheSource computationCacheSource = new InMemoryViewComputationCacheSource(FudgeContext.GLOBAL_DEFAULT);
    final FunctionInvocationStatisticsGatherer functionInvocationStatistics = new DiscardingInvocationStatisticsGatherer();
    final FunctionExecutionContext executionContext = new FunctionExecutionContext();
    final JobDispatcher jobDispatcher = new JobDispatcher(new LocalNodeJobInvoker(new SimpleCalculationNode(computationCacheSource, compilationService, executionContext, "node",
        Executors.newCachedThreadPool(), functionInvocationStatistics, new CalculationNodeLogEventListener(new ThreadLocalLogEventListener()))));
    final ViewPermissionProvider viewPermissionProvider = new DefaultViewPermissionProvider();
    final GraphExecutorStatisticsGathererProvider graphExecutorStatisticsProvider = new DiscardingGraphStatisticsGathererProvider();
    final ViewDefinition viewDefinition = new ViewDefinition("TestView", UserPrincipal.getTestUser());
    viewDefinition.addViewCalculationConfiguration(new ViewCalculationConfiguration(viewDefinition, "default"));
    final MockConfigSource configSource = new MockConfigSource();
    configSource.put(viewDefinition);
    final ViewProcessContext vpc = new ViewProcessContext(UniqueId.of("Process", "Test"), configSource, viewPermissionProvider, new DefaultViewPortfolioPermissionProvider(),
        marketDataProviderResolver, compilationService, functionResolver, computationCacheSource, jobDispatcher, new SingleThreadViewProcessWorkerFactory(),
        new DependencyGraphBuilderFactory(), factory, graphExecutorStatisticsProvider, new DummyOverrideOperationCompiler(), new EngineResourceManagerImpl<SingleComputationCycle>(),
        new VersionedUniqueIdSupplier("Test", "1"), new InMemoryViewExecutionCache());
    DependencyNode previousNode = null;
    ValueSpecification previousValue = null;
    for (int i = 0; i < JOB_SIZE; i++) {
      final Map<ValueSpecification, DependencyNode> inputs;
      if (previousNode != null) {
        inputs = Collections.singletonMap(previousValue, previousNode);
      } else {
        inputs = Collections.emptyMap();
      }
      previousValue = new ValueSpecification(Integer.toString(i), ComputationTargetSpecification.NULL, ValueProperties.with(ValuePropertyNames.FUNCTION, "Mock").get());
      mockFunction.addResult(new ComputedValue(previousValue, "Mock"));
      final DependencyNode node = new DependencyNodeImpl(DependencyNodeFunctionImpl.of(mockFunction), ComputationTargetSpecification.NULL, Collections.singleton(previousValue), inputs);
      previousNode = node;
    }
    final DependencyGraph graph = new DependencyGraphImpl("Test", Collections.singleton(previousNode), JOB_SIZE, Collections.<ValueSpecification, Set<ValueRequirement>>emptyMap());
    final Instant now = Instant.now();
    final CompiledViewDefinitionWithGraphsImpl viewEvaluationModel = new CompiledViewDefinitionWithGraphsImpl(VersionCorrection.of(now, now), "", viewDefinition,
        Collections.singleton(graph), Collections.<ComputationTargetReference, UniqueId>emptyMap(), new SimplePortfolio("Test Portfolio"), 0,
        Collections.<CompiledViewCalculationConfiguration>singleton(CompiledViewCalculationConfigurationImpl.of(graph)), null, null);
    final ViewCycleExecutionOptions cycleOptions = ViewCycleExecutionOptions.builder().setValuationTime(Instant.ofEpochMilli(1))
        .setMarketDataSpecification(LiveMarketDataSpecification.LIVE_SPEC).create();
    final SingleComputationCycle cycle = new SingleComputationCycle(UniqueId.of("Test", "Cycle1"), "", new ComputationResultListener() {
      @Override
      public void resultAvailable(final ViewComputationResultModel result) {
        //ignore
      }
    }, vpc, viewEvaluationModel, cycleOptions, VersionCorrection.of(Instant.ofEpochMilli(1), Instant.ofEpochMilli(1)));
    return factory.createExecutor(cycle).execute(graph, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification, FunctionParameters>emptyMap());
  }

  private boolean jobFinished() {
    return _functionCount.get() == JOB_SIZE;
  }

  /**
   * Allow the job to finish, then call {@link Future#cancel}.
   */
  @Test(dataProvider = "executors")
  public void testJobFinish(final DependencyGraphExecutorFactory factory) throws Exception {
    s_logger.info("testJobFinish");
    final Future<?> job = executeTestJob(factory);
    assertNotNull(job);
    for (int i = 0; i < JOB_FINISH_TIME / SLEEP_TIME; i++) {
      if (jobFinished()) {
        job.get(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
        assertFalse(job.isCancelled());
        assertTrue(job.isDone());
        s_logger.info("Job finished in {}", i);
        return;
      }
      sleep();
    }
    Assert.fail("Job didn't finish in time available");
  }

  /**
   * Call {@link Future#cancel} before the job finishes, with interrupt enabled.
   */
  @Test(dataProvider = "executors")
  public void testJobCancelWithInterrupt(final DependencyGraphExecutorFactory factory) {
    s_logger.info("testJobCancelWithInterrupt");
    final Future<?> job = executeTestJob(factory);
    assertNotNull(job);
    job.cancel(true);
    for (int i = 0; i < JOB_FINISH_TIME / SLEEP_TIME; i++) {
      if (jobFinished()) {
        assertTrue(job.isCancelled());
        assertTrue(job.isDone());
        s_logger.info("Job finished in {}", i);
        Assert.fail("Job finished normally despite cancel");
        return;
      }
      sleep();
    }
  }

  /**
   * Call {@link Future#cancel} before the job finishes, with no interrupt.
   */
  @Test(dataProvider = "executors")
  public void testJobCancelWithoutInterrupt(final DependencyGraphExecutorFactory factory) {
    s_logger.info("testJobCancelWithoutInterrupt");
    final Future<?> job = executeTestJob(factory);
    assertNotNull(job);
    job.cancel(false);
    for (int i = 0; i < JOB_FINISH_TIME / SLEEP_TIME; i++) {
      if (jobFinished()) {
        assertTrue(job.isCancelled());
        assertTrue(job.isDone());
        s_logger.info("Job finished in {}", i);
        Assert.fail("Job finished normally despite cancel");
        return;
      }
      sleep();
    }
  }

}
