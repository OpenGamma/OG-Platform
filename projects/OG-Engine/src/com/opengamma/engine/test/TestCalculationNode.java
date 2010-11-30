/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import java.util.concurrent.Executors;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.security.impl.MockSecuritySource;
import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.CachingFunctionRepositoryCompiler;
import com.opengamma.engine.function.CompiledFunctionService;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.view.cache.InMemoryViewComputationCacheSource;
import com.opengamma.engine.view.calcnode.AbstractCalculationNode;
import com.opengamma.engine.view.calcnode.ViewProcessorQuerySender;
import com.opengamma.engine.view.calcnode.stats.DiscardingInvocationStatisticsGatherer;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.InetAddressUtils;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

public class TestCalculationNode extends AbstractCalculationNode {

  public TestCalculationNode() {
    super(new InMemoryViewComputationCacheSource(OpenGammaFudgeContext.getInstance()), new CompiledFunctionService(new InMemoryFunctionRepository(), new CachingFunctionRepositoryCompiler(), new FunctionCompilationContext()), new FunctionExecutionContext(), new DefaultComputationTargetResolver(new MockSecuritySource(), new MockPositionSource()), new ViewProcessorQuerySender(
        new FudgeRequestSender() {

          @Override
          public FudgeContext getFudgeContext() {
            return FudgeContext.GLOBAL_DEFAULT;
          }

          @Override
          public void sendRequest(FudgeFieldContainer request, FudgeMessageReceiver responseReceiver) {
            // No-op
          }

        }), InetAddressUtils.getLocalHostName(), Executors.newCachedThreadPool(), new DiscardingInvocationStatisticsGatherer());
  }
}
