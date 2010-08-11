/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.DefaultFunctionResolver;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.livedata.FixedLiveDataAvailabilityProvider;
import com.opengamma.engine.livedata.InMemoryLKVSnapshotProvider;
import com.opengamma.engine.position.MockPositionSource;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.security.MockSecuritySource;
import com.opengamma.engine.view.cache.MapViewComputationCacheSource;
import com.opengamma.engine.view.calc.SingleNodeExecutorFactory;
import com.opengamma.engine.view.calcnode.DummyResultWriterFactory;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.LocalCalculationNode;
import com.opengamma.engine.view.calcnode.LocalNodeJobInvoker;
import com.opengamma.engine.view.calcnode.ViewProcessorQueryReceiver;
import com.opengamma.engine.view.calcnode.ViewProcessorQuerySender;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.livedata.client.TestLiveDataClient;
import com.opengamma.transport.InMemoryRequestConduit;
import com.opengamma.util.NamedThreadPoolFactory;

/**
 * Utility to setup a View for testing.
 */
public class ViewTestUtils {
  
  public static View getMockView() {
    UniqueIdentifier portfolioId = UniqueIdentifier.of("foo", "bar");
    
    InMemoryFunctionRepository functionRepo = new InMemoryFunctionRepository();
    
    MockSecuritySource securitySource = new MockSecuritySource();

    MockPositionSource positionSource = new MockPositionSource();
    positionSource.addPortfolio(new PortfolioImpl(portfolioId, "test_portfolio"));
    
    DefaultComputationTargetResolver targetResolver = new DefaultComputationTargetResolver(securitySource, positionSource);
    
    MapViewComputationCacheSource cacheFactory = new MapViewComputationCacheSource(FudgeContext.GLOBAL_DEFAULT);
    
    FunctionExecutionContext executionContext = new FunctionExecutionContext();
    
    ViewProcessorQueryReceiver viewProcessorQueryReceiver = new ViewProcessorQueryReceiver();
    ViewProcessorQuerySender viewProcessorQuerySender = new ViewProcessorQuerySender(InMemoryRequestConduit.create(viewProcessorQueryReceiver));
    LocalCalculationNode localNode = new LocalCalculationNode(cacheFactory, functionRepo, executionContext, targetResolver, viewProcessorQuerySender);
    JobDispatcher jobDispatcher = new JobDispatcher (new LocalNodeJobInvoker (localNode));
    
    ThreadFactory threadFactory = new NamedThreadPoolFactory("ViewTestUtils-" + System.currentTimeMillis(), true);
    ThreadPoolExecutor executor = new ThreadPoolExecutor(0, 1, 5l, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
    
    ViewProcessingContext vpc = new ViewProcessingContext(
        new TestLiveDataClient(), 
        new FixedLiveDataAvailabilityProvider(), 
        new InMemoryLKVSnapshotProvider(), 
        functionRepo, 
        new DefaultFunctionResolver(functionRepo),
        positionSource, 
        securitySource, 
        cacheFactory, 
        jobDispatcher, 
        viewProcessorQueryReceiver,
        new FunctionCompilationContext(), 
        executor,
        new SingleNodeExecutorFactory());
    
    ViewDefinition viewDefinition = new ViewDefinition("mock_view", portfolioId, "ViewTestUser");

    View viewImpl = new View(viewDefinition, vpc);
    viewImpl.init();
    
    return viewImpl;
  }

}
