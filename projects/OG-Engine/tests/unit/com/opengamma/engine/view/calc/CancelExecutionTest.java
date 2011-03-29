/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.time.Instant;

import org.fudgemsg.FudgeContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.position.impl.PortfolioImpl;
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
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.test.MockFunction;
import com.opengamma.engine.test.MockSecuritySource;
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
import com.opengamma.engine.view.compilation.ViewEvaluationModel;
import com.opengamma.engine.view.permission.DefaultViewPermissionProvider;
import com.opengamma.engine.view.permission.ViewPermissionProvider;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.entitlement.LiveDataEntitlementChecker;
import com.opengamma.livedata.entitlement.PermissiveLiveDataEntitlementChecker;
import com.opengamma.transport.InMemoryRequestConduit;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.Timeout;

@RunWith(Parameterized.class)
public class CancelExecutionTest {

  private static final int JOB_SIZE = 100;
  private static final int JOB_FINISH_TIME = (int)Timeout.standardTimeoutMillis();
  private static final int SLEEP_TIME = JOB_FINISH_TIME / 10;
  private static final Logger s_logger = LoggerFactory.getLogger(CancelExecutionTest.class);

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

  @Parameters
  public static Collection<Object[]> getParameters() {
    final List<Object[]> executors = new ArrayList<Object[]>();
    executors.add(new Object[] {multipleNodeExecutorFactoryManyJobs()});
    executors.add(new Object[] {multipleNodeExecutorFactoryOneJob()});
    executors.add(new Object[] {new SingleNodeExecutorFactory()});
    // The BatchExecutor doesn't support cancellation
    return executors;
  }

  private final DependencyGraphExecutorFactory<?> _factory;
  private final AtomicInteger _functionCount = new AtomicInteger();

  public CancelExecutionTest(final DependencyGraphExecutorFactory<?> factory) {
    _factory = factory;
    s_logger.info("CancelExecutionTest running with {}", factory);
  }

  private void sleep() {
    try {
      Thread.sleep(SLEEP_TIME);
    } catch (InterruptedException e) {
    }
  }

  private Future<?> executeTestJob() {
    final LiveDataEntitlementChecker liveDataEntitlementChecker = new PermissiveLiveDataEntitlementChecker();
    final InMemoryLKVSnapshotProvider liveData = new InMemoryLKVSnapshotProvider();
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
    final ViewPermissionProvider viewPermissionProvider = new DefaultViewPermissionProvider(securitySource, liveDataEntitlementChecker);
    final GraphExecutorStatisticsGathererProvider graphExecutorStatisticsProvider = new DiscardingGraphStatisticsGathererProvider();
    final ViewProcessContext vpc = new ViewProcessContext(viewPermissionProvider, liveData, liveData, compilationService, functionResolver, positionSource, securitySource,
        new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource), EHCacheUtils.createCacheManager()), computationCacheSource, jobDispatcher,
        viewProcessorQueryReceiver, _factory, graphExecutorStatisticsProvider);
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
    ViewDefinition viewDefinition = new ViewDefinition("TestView", UserPrincipal.getTestUser());
    viewDefinition.addViewCalculationConfiguration(new ViewCalculationConfiguration(viewDefinition, "default"));
    final Map<String, DependencyGraph> graphs = new HashMap<String, DependencyGraph>();
    graphs.put(graph.getCalcConfName(), graph);
    ViewEvaluationModel viewEvaluationModel = new ViewEvaluationModel(viewDefinition, graphs, new PortfolioImpl("Test Portfolio"), 0);
    final SingleComputationCycle cycle = new SingleComputationCycle(
        UniqueIdentifier.of("Test", "Cycle1"),
        UniqueIdentifier.of("Test", "ViewProcess1"),
        vpc, 
        viewEvaluationModel, 
        Instant.ofEpochMillis(1));
    return cycle.getDependencyGraphExecutor().execute(graph, cycle.getStatisticsGatherer());
  }

  private boolean jobFinished() {
    return _functionCount.get() == JOB_SIZE;
  }

  /**
   * Allow the job to finish, then call {@link Future#cancel}.
   */
  @Test
  public void testJobFinish() throws Exception {
    s_logger.info("testJobFinish");
    Future<?> job = executeTestJob();
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
    fail("Job didn't finish in time available");
  }

  /**
   * Call {@link Future#cancel} before the job finishes, with interrupt enabled.
   */
  @Test
  public void testJobCancelWithInterrupt() {
    s_logger.info("testJobCancelWithInterrupt");
    Future<?> job = executeTestJob();
    assertNotNull(job);
    job.cancel(true);
    for (int i = 0; i < JOB_FINISH_TIME / SLEEP_TIME; i++) {
      if (jobFinished()) {
        assertTrue(job.isCancelled());
        assertTrue(job.isDone());
        s_logger.info("Job finished in {}", i);
        fail("Job finished normally despite cancel");
        return;
      }
      sleep();
    }
  }

  /**
   * Call {@link Future#cancel} before the job finishes, with no interrupt.
   */
  @Test
  public void testJobCancelWithoutInterrupt() {
    s_logger.info("testJobCancelWithoutInterrupt");
    Future<?> job = executeTestJob();
    assertNotNull(job);
    job.cancel(false);
    for (int i = 0; i < JOB_FINISH_TIME / SLEEP_TIME; i++) {
      if (jobFinished()) {
        assertTrue(job.isCancelled());
        assertTrue(job.isDone());
        s_logger.info("Job finished in {}", i);
        fail("Job finished normally despite cancel");
        return;
      }
      sleep();
    }
  }

}
