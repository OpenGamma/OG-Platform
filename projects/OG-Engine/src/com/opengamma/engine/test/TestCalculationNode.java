/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.concurrent.Executors;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.view.cache.InMemoryViewComputationCacheSource;
import com.opengamma.engine.view.calcnode.SimpleCalculationNode;
import com.opengamma.engine.view.calcnode.ViewProcessorQuerySender;
import com.opengamma.engine.view.calcnode.stats.DiscardingInvocationStatisticsGatherer;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.InetAddressUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

public class TestCalculationNode extends SimpleCalculationNode {

  private static FunctionCompilationContext compilationContext() {
    final FunctionCompilationContext context = new FunctionCompilationContext();
    context.setSecuritySource(new MockSecuritySource());
    context.setRawComputationTargetResolver(new DefaultComputationTargetResolver(context.getSecuritySource(), new MockPositionSource()));
    return context;
  }

  private static CompiledFunctionService initializedCFS() {
    final CompiledFunctionService cfs = new CompiledFunctionService(new InMemoryFunctionRepository(), new CachingFunctionRepositoryCompiler(), compilationContext());
    cfs.initialize();
    return cfs;
  }

  public TestCalculationNode() {
    super(new InMemoryViewComputationCacheSource(OpenGammaFudgeContext.getInstance()), initializedCFS(), new FunctionExecutionContext(), new ViewProcessorQuerySender(
        new FudgeRequestSender() {

          @Override
          public FudgeContext getFudgeContext() {
            return FudgeContext.GLOBAL_DEFAULT;
          }

          @Override
          public void sendRequest(FudgeMsg request, FudgeMessageReceiver responseReceiver) {
            // No-op
          }

        }), InetAddressUtils.getLocalHostName(), Executors.newCachedThreadPool(), new DiscardingInvocationStatisticsGatherer());
  }
}
