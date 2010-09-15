/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode.stats;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;

import com.opengamma.engine.view.calcnode.msg.Invocations;
import com.opengamma.engine.view.calcnode.msg.RemoteCalcNodeMessage;
import com.opengamma.engine.view.calcnode.msg.Scaling;
import com.opengamma.engine.view.calcnode.msg.Invocations.PerConfiguration;
import com.opengamma.engine.view.calcnode.msg.Invocations.PerConfiguration.PerFunction;
import com.opengamma.transport.FudgeMessageReceiver;

/**
 * Receives statistics from a {@link FunctionInvocationStatisticsSender}.
 */
public class FunctionInvocationStatisticsReceiver implements FudgeMessageReceiver {

  private final FunctionCost _underlying;

  public FunctionInvocationStatisticsReceiver(final FunctionCost underlying) {
    _underlying = underlying;
  }

  public FunctionCost getUnderlying() {
    return _underlying;
  }

  public static Scaling messageReceived(final FunctionCost underlying, final Invocations invocations) {
    double remoteInvocationCost = 0;
    double localInvocationCost = 0;
    for (PerConfiguration configuration : invocations.getConfiguration()) {
      final FunctionCost.ForConfiguration configurationStats = underlying.getStatistics(configuration.getConfiguration());
      for (PerFunction function : configuration.getFunction()) {
        final FunctionInvocationStatistics statistics = configurationStats.getStatistics(function.getIdentifier());
        localInvocationCost += statistics.getInvocationCost();
        statistics.recordInvocation(function.getCount(), function.getInvocation(), function.getDataInput(), function.getDataOutput());
        remoteInvocationCost += function.getInvocation() / function.getCount();
      }
    }
    if (remoteInvocationCost > 0) {
      return new Scaling(localInvocationCost / remoteInvocationCost);
    } else {
      return null;
    }
  }

  @Override
  public void messageReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope msgEnvelope) {
    final FudgeDeserializationContext context = new FudgeDeserializationContext(fudgeContext);
    final RemoteCalcNodeMessage message = context.fudgeMsgToObject(RemoteCalcNodeMessage.class, msgEnvelope.getMessage());
    if (message instanceof Invocations) {
      messageReceived(getUnderlying(), (Invocations) message);
    } else {
      throw new UnsupportedOperationException("Unexpected message - " + message);
    }
  }

}
