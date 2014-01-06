/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.concurrent.Executors;

import org.springframework.context.Lifecycle;

import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.security.impl.test.MockSecuritySource;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.cache.InMemoryViewComputationCacheSource;
import com.opengamma.engine.calcnode.CalculationNodeLogEventListener;
import com.opengamma.engine.calcnode.SimpleCalculationNode;
import com.opengamma.engine.calcnode.stats.DiscardingInvocationStatisticsGatherer;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.function.config.FunctionRepositoryFactory;
import com.opengamma.util.InetAddressUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.log.ThreadLocalLogEventListener;

public class TestCalculationNode extends SimpleCalculationNode implements Lifecycle {

  private static FunctionCompilationContext compilationContext() {
    final FunctionCompilationContext context = new FunctionCompilationContext();
    context.setSecuritySource(new MockSecuritySource());
    context.setRawComputationTargetResolver(new DefaultComputationTargetResolver(context.getSecuritySource(), new MockPositionSource()));
    return context;
  }

  private static CompiledFunctionService initializedCFS() {
    final InMemoryFunctionRepository repository = new InMemoryFunctionRepository();
    final CompiledFunctionService cfs = new CompiledFunctionService(FunctionRepositoryFactory.constructRepositoryFactory(repository), new CachingFunctionRepositoryCompiler(),
        compilationContext());
    cfs.initialize();
    return cfs;
  }

  public TestCalculationNode() {
    this(new ThreadLocalLogEventListener());
  }

  public TestCalculationNode(final ThreadLocalLogEventListener logEventListener) {
    super(new InMemoryViewComputationCacheSource(OpenGammaFudgeContext.getInstance()), initializedCFS(), new FunctionExecutionContext(), InetAddressUtils.getLocalHostName(), Executors
        .newCachedThreadPool(), new DiscardingInvocationStatisticsGatherer(), new CalculationNodeLogEventListener(logEventListener));
  }

  // Lifecycle

  @Override
  public void start() {
    getFunctionCompilationService().start();
  }

  @Override
  public void stop() {
    getFunctionCompilationService().stop();
    getExecutorService().shutdown();
  }

  @Override
  public boolean isRunning() {
    return getFunctionCompilationService().isRunning();
  }

}
