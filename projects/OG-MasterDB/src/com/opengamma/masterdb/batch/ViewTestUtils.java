/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.masterdb.batch;

import java.util.Timer;
import java.util.concurrent.Executors;

import net.sf.ehcache.CacheManager;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.position.impl.PortfolioImpl;
import com.opengamma.engine.CachingComputationTargetResolver;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.resolver.DefaultFunctionResolver;
import com.opengamma.engine.livedata.FixedLiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.view.ViewDefinition;
import com.opengamma.engine.view.ViewImpl;
import com.opengamma.engine.view.ViewProcessingContext;
import com.opengamma.engine.view.cache.InMemoryViewComputationCacheSource;
import com.opengamma.engine.view.calc.SingleNodeExecutorFactory;
import com.opengamma.engine.view.calc.stats.DiscardingGraphStatisticsGathererProvider;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.LocalCalculationNode;
import com.opengamma.engine.view.calcnode.LocalNodeJobInvoker;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.calcnode.ViewProcessorQuerySender;
import com.opengamma.engine.view.calcnode.stats.DiscardingInvocationStatisticsGatherer;
import com.opengamma.engine.view.permission.DefaultViewPermissionProvider;
import com.opengamma.financial.security.MockFinancialSecuritySource;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.test.TestLiveDataClient;
import com.opengamma.transport.InMemoryRequestConduit;
import com.opengamma.util.ehcache.EHCacheUtils;

/**
 * Utility to setup a View for testing.
 */
public class ViewTestUtils {

  public static ViewImpl getMockView() {
    final CacheManager cacheManager = EHCacheUtils.createCacheManager();
    UniqueIdentifier portfolioId = UniqueIdentifier.of("foo", "bar");

    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();

    MockFinancialSecuritySource securitySource = new MockFinancialSecuritySource();

    MockPositionSource positionSource = new MockPositionSource();
    positionSource.addPortfolio(new PortfolioImpl(portfolioId, "test_portfolio"));

    CachingComputationTargetResolver targetResolver = new DefaultCachingComputationTargetResolver(new DefaultComputationTargetResolver(securitySource, positionSource), cacheManager);

    InMemoryViewComputationCacheSource computationCache = new InMemoryViewComputationCacheSource(FudgeContext.GLOBAL_DEFAULT);

    FunctionExecutionContext executionContext = new FunctionExecutionContext();
    CompiledFunctionService functionCompilation = new CompiledFunctionService(functionRepo, new CachingFunctionRepositoryCompiler(), new FunctionCompilationContext());
    functionCompilation.initialize();

    ViewProcessorQueryReceiver viewProcessorQueryReceiver = new ViewProcessorQueryReceiver();
    ViewProcessorQuerySender viewProcessorQuerySender = new ViewProcessorQuerySender(InMemoryRequestConduit.create(viewProcessorQueryReceiver));
    LocalCalculationNode localNode = new LocalCalculationNode(computationCache, functionCompilation, executionContext, targetResolver, viewProcessorQuerySender, Executors.newCachedThreadPool(),
        new DiscardingInvocationStatisticsGatherer());
    JobDispatcher jobDispatcher = new JobDispatcher(new LocalNodeJobInvoker(localNode));

//    ThreadFactory threadFactory = new NamedThreadPoolFactory("ViewTestUtils-" + System.currentTimeMillis(), true);
//    ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 1, 5l, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);

    ViewProcessingContext vpc = new ViewProcessingContext(new TestLiveDataClient(), new FixedLiveDataAvailabilityProvider(), new InMemoryLKVSnapshotProvider(), functionCompilation,
        new DefaultFunctionResolver(functionCompilation), positionSource, securitySource, targetResolver, computationCache, jobDispatcher, viewProcessorQueryReceiver, new SingleNodeExecutorFactory(),
        new DefaultViewPermissionProvider(), new DiscardingGraphStatisticsGathererProvider());

    ViewDefinition viewDefinition = new ViewDefinition("mock_view", portfolioId, "ViewTestUser");

    ViewImpl viewImpl = new ViewImpl(viewDefinition, vpc, new Timer("Test view timer"));
    viewImpl.init();

    return viewImpl;
  }

}
