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
import com.opengamma.engine.view.calcnode.msg.Invocations.PerConfiguration;
import com.opengamma.engine.view.calcnode.msg.Invocations.PerConfiguration.PerFunction;
import com.opengamma.transport.FudgeMessageReceiver;

/**
 * Receives statistics from a {@link FunctionInvocationStatisticsSender}.
 */
public class FunctionInvocationStatisticsReceiver implements FudgeMessageReceiver {

  private final FunctionInvocationStatisticsGatherer _underlying;

  public FunctionInvocationStatisticsReceiver(final FunctionInvocationStatisticsGatherer underlying) {
    _underlying = underlying;
  }

  public FunctionInvocationStatisticsGatherer getUnderlying() {
    return _underlying;
  }

  public static void messageReceived(final FunctionInvocationStatisticsGatherer underlying, final Invocations invocations) {
    for (PerConfiguration configuration : invocations.getConfiguration()) {
      for (PerFunction function : configuration.getFunction()) {
        underlying.functionInvoked(configuration.getConfiguration(), function.getIdentifier(), function.getCount(), function.getInvocation(), function.getDataInput(), function.getDataOutput());
      }
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
