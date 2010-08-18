/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.test;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.DefaultComputationTargetResolver;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.InMemoryFunctionRepository;
import com.opengamma.engine.position.MockPositionSource;
import com.opengamma.engine.security.MockSecuritySource;
import com.opengamma.engine.view.cache.InMemoryViewComputationCacheSource;
import com.opengamma.engine.view.calcnode.AbstractCalculationNode;
import com.opengamma.engine.view.calcnode.ViewProcessorQuerySender;
import com.opengamma.util.InetAddressUtils;

public class TestCalculationNode extends AbstractCalculationNode {
  
  public TestCalculationNode() {
    super(new InMemoryViewComputationCacheSource(FudgeContext.GLOBAL_DEFAULT), 
        new FunctionExecutionContext(), 
        new DefaultComputationTargetResolver(new MockSecuritySource(), new MockPositionSource()), 
        new ViewProcessorQuerySender(null), 
        InetAddressUtils.getLocalHostName());
    setFunctionRepository (new InMemoryFunctionRepository ());
  }
}