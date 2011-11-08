/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.time.Instant;

import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.calcnode.CalculationJobResult;
import com.opengamma.engine.view.listener.ComputationCycleResultListener;
import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.depgraph.DependencyGraph;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.function.resolver.FunctionResolver;
import com.opengamma.engine.marketdata.InMemoryLKVMarketDataProvider;
import com.opengamma.engine.marketdata.SingletonMarketDataProviderFactory;
import com.opengamma.engine.marketdata.resolver.MarketDataProviderResolver;
import com.opengamma.engine.marketdata.resolver.SingleMarketDataProviderResolver;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.test.MockSecuritySource;
import com.opengamma.engine.test.MockViewDefinitionRepository;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.view.ViewCalculationConfiguration;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewProcessContext;
import com.opengamma.engine.view.cache.InMemoryViewComputationCacheSource;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.engine.view.calc.stats.DiscardingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.calc.stats.GraphExecutorStatisticsGathererProvider;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.LocalCalculationNode;
import com.opengamma.engine.view.calcnode.LocalNodeJobInvoker;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.calcnode.ViewProcessorQuerySender;
import com.opengamma.engine.view.calcnode.stats.DiscardingInvocationStatisticsGatherer;
import com.opengamma.engine.view.calcnode.stats.FunctionInvocationStatisticsGatherer;
import com.opengamma.engine.view.compilation.CompiledViewDefinitionWithGraphsImpl;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.permission.DefaultViewPermissionProvider;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.transport.InMemoryRequestConduit;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.Timeout;

public class CancelExecutionTest {

  private static final int JOB_SIZE = 100;
  private static final int JOB_FINISH_TIME = (int)Timeout.standardTimeoutMillis();
  private static final int SLEEP_TIME = JOB_FINISH_TIME / 10;
  private static final Logger s_logger = LoggerFactory.getLogger(CancelExecutionTest.class);

  @DataProvider(name = "executors")
  Object[][] data_executors() {
    return new Object[][] {
      {multipleNodeExecutorFactoryManyJobs()},
      {multipleNodeExecutorFactoryOneJob()},
      {new SingleNodeExecutorFactory()},
    };
  }

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
    } catch (InterruptedException e) {
    }
  }

  private ComputationCycleResultListener computationCycleResultListener = new ComputationCycleResultListener() {
    @Override
    public void jobResultReceived(ViewResultModel result) {
      //ignore
    }
  };

  private Future<?> executeTestJob(DependencyGraphExecutorFactory<?> factory) {
    final InMemoryLKVMarketDataProvider marketDataProvider = new InMemoryLKVMarketDataProvider();
    final MarketDataProviderResolver marketDataProviderResolver = new SingleMarketDataProviderResolver(new SingletonMarketDataProviderFactory(marketDataProvider));
    final InMemoryFunctionRepository functionRepository = new InMemoryFunctionRepository();
    _functionCount.set(0);
    final MockFunction mockFunction = new MockFunction(new ComputationTarget("Foo")) {

      @Override
      public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
        try {
          Thread.sleep(JOB_FINISH_TIME / (JOB_SIZE * 2));
        } catch (InterruptedException e) {
          throw new OpenGammaRuntimeException("Function interrupted", e);
        }
        _functionCount.incrementAndGet();
        return super.execute(executionContext, inputs, target, desiredValues);
      }

    };
    functionRepository.addFunction(mockFunction);
    final FunctionCompilationContext compilationContext = new FunctionCompilationContext();
    final CompiledFunctionService compilationService = new CompiledFunctionService(functionRepository, new CachingFunctionRepositoryCompiler(), compilationContext);
    compilationService.initialize ();
    final FunctionResolver functionResolver = new DefaultFunctionResolver(compilationService);
    final MockSecuritySource securitySource = new MockSecuritySource();
    final MockPositionSource positionSource = new MockPositionSource();
    final ViewComputationCacheSource computationCacheSource = new InMemoryViewComputationCacheSource(FudgeContext.GLOBAL_DEFAULT);
    final FunctionInvocationStatisticsGatherer functionInvocationStatistics = new DiscardingInvocationStatisticsGatherer();
    final ViewProcessorQueryReceiver viewProcessorQueryReceiver = new ViewProcessorQueryReceiver();
    final ViewProcessorQuerySender viewProcessorQuerySender = new ViewProcessorQuerySender(InMemoryRequestConduit.create(viewProcessorQueryReceiver));
    final FunctionExecutionContext executionContext = new FunctionExecutionContext();
    final ComputationTargetResolver targetResolver = new DefaultComputationTargetResolver(securitySource, positionSource);
    final JobDispatcher jobDispatcher = new JobDispatcher(new LocalNodeJobInvoker(new LocalCalculationNode(computationCacheSource, compilationService, executionContext, targetResolver,
        viewProcessorQuerySender, Executors.newCachedThreadPool(), functionInvocationStatistics)));
    final ViewPermissionProvider viewPermissionProvider = new DefaultViewPermissionProvider();
    final GraphExecutorStatisticsGathererProvider graphExecutorStatisticsProvider = new DiscardingGraphStatisticsGathererProvider();
    
    ViewDefinition viewDefinition = new ViewDefinition("TestView", UserPrincipal.getTestUser());
    viewDefinition.addViewCalculationConfiguration(new ViewCalculationConfiguration(viewDefinition, "default"));
    MockViewDefinitionRepository viewDefinitionRepository = new MockViewDefinitionRepository();
    viewDefinitionRepository.addDefinition(viewDefinition);
    
    final ViewProcessContext vpc = new ViewProcessContext(viewDefinitionRepository, viewPermissionProvider, marketDataProviderResolver, compilationService, functionResolver, positionSource, securitySource,
        new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource), EHCacheUtils.createCacheManager()), computationCacheSource, jobDispatcher,
        viewProcessorQueryReceiver, factory, graphExecutorStatisticsProvider);
    final DependencyGraph graph = new DependencyGraph("Default");
    DependencyNode previous = null;
    for (int i = 0; i < JOB_SIZE; i++) {
      DependencyNode node = new DependencyNode(new ComputationTarget("Foo"));
      node.setFunction(mockFunction);
      if (previous != null) {
        node.addInputNode(previous);
      }
      graph.addDependencyNode(node);
      previous = node;
    }
    final Map<String, DependencyGraph> graphs = new HashMap<String, DependencyGraph>();
    graphs.put(graph.getCalculationConfigurationName(), graph);
    CompiledViewDefinitionWithGraphsImpl viewEvaluationModel = new CompiledViewDefinitionWithGraphsImpl(viewDefinition, graphs, new SimplePortfolio("Test Portfolio"), 0);
    ViewCycleExecutionOptions cycleOptions = new ViewCycleExecutionOptions();
    cycleOptions.setValuationTime(Instant.ofEpochMillis(1));
    cycleOptions.setMarketDataSpecification(new MarketDataSpecification());
    final SingleComputationCycle cycle = new SingleComputationCycle(
        UniqueId.of("Test", "Cycle1"),
        UniqueId.of("Test", "ViewProcess1"),
        computationCycleResultListener,
        vpc, 
        viewEvaluationModel, 
        cycleOptions,
        VersionCorrection.of(Instant.ofEpochMillis(1), Instant.ofEpochMillis(1)));
    return cycle.getDependencyGraphExecutor().execute(graph, new LinkedBlockingQueue<CalculationJobResult>(), cycle.getStatisticsGatherer());
  }

  private boolean jobFinished() {
    return _functionCount.get() == JOB_SIZE;
  }

  /**
   * Allow the job to finish, then call {@link Future#cancel}.
   */
  @Test(dataProvider = "executors")
  public void testJobFinish(DependencyGraphExecutorFactory<?> factory) throws Exception {
    s_logger.info("testJobFinish");
    Future<?> job = executeTestJob(factory);
    assertNotNull(job);
    for (int i = 0; i < JOB_FINISH_TIME / SLEEP_TIME; i++) {
      if (jobFinished()) {
        job.get (Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
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
  public void testJobCancelWithInterrupt(DependencyGraphExecutorFactory<?> factory) {
    s_logger.info("testJobCancelWithInterrupt");
    Future<?> job = executeTestJob(factory);
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
  public void testJobCancelWithoutInterrupt(DependencyGraphExecutorFactory<?> factory) {
    s_logger.info("testJobCancelWithoutInterrupt");
    Future<?> job = executeTestJob(factory);
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
