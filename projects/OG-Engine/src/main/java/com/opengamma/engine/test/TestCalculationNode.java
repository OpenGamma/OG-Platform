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
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.view.cache.InMemoryViewComputationCacheSource;
import com.opengamma.engine.view.calcnode.CalculationNodeLogEventListener;
import com.opengamma.engine.view.calcnode.SimpleCalculationNode;
import com.opengamma.engine.view.calcnode.ViewProcessorQuerySender;
import com.opengamma.engine.view.calcnode.stats.DiscardingInvocationStatisticsGatherer;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.InetAddressUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.log.ThreadLocalLogEventListener;

public class TestCalculationNode extends SimpleCalculationNode {
  
  private static CompiledFunctionService initializedCFS() {
    final CompiledFunctionService cfs = new CompiledFunctionService(new InMemoryFunctionRepository(), new CachingFunctionRepositoryCompiler(), new FunctionCompilationContext());
    cfs.initialize();
    return cfs;
  }

  public TestCalculationNode() {
    this(new ThreadLocalLogEventListener());
  }
  
  public TestCalculationNode(ThreadLocalLogEventListener logEventListener) {
    super(new InMemoryViewComputationCacheSource(OpenGammaFudgeContext.getInstance()), initializedCFS(), new FunctionExecutionContext(), new DefaultComputationTargetResolver(new InMemorySecuritySource(),
        new MockPositionSource()), new ViewProcessorQuerySender(
        new FudgeRequestSender() {

          @Override
          public FudgeContext getFudgeContext() {
            return FudgeContext.GLOBAL_DEFAULT;
          }

          @Override
          public void sendRequest(FudgeMsg request, FudgeMessageReceiver responseReceiver) {
            // No-op
          }

        }), InetAddressUtils.getLocalHostName(), Executors.newCachedThreadPool(), new DiscardingInvocationStatisticsGatherer(), new CalculationNodeLogEventListener(logEventListener));    
  }

}
