/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode.stats;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.engine.calcnode.msg.Invocations;
import com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration;
import com.opengamma.engine.calcnode.msg.Invocations.PerConfiguration.PerFunction;
import com.opengamma.engine.calcnode.msg.RemoteCalcNodeMessage;
import com.opengamma.engine.calcnode.msg.Scaling;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 * Receives statistics from a {@link FunctionInvocationStatisticsSender}.
 * <p>
 * This is run centrally and receives statistics from each node.
 * The statistics are aggregated into the {@code FunctionCost} instance.
 */
public class FunctionInvocationStatisticsReceiver implements FudgeMessageReceiver {

  /**
   * The underlying function cost implementation.
   */
  private final FunctionCosts _underlying;

  /**
   * Creates an instance wrapping an underlying function cost instance.
   * 
   * @param underlying  the underlying function cost, not null
   */
  public FunctionInvocationStatisticsReceiver(final FunctionCosts underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying function cost.
   * 
   * @return the function cost, not null
   */
  public FunctionCosts getUnderlying() {
    return _underlying;
  }

  //-------------------------------------------------------------------------
  /**
   * Handle a message.
   * 
   * @param underlying  the underlying function cost, not null
   * @param invocations  the invocations to handle, not null
   * @return the scaling factor, null if no remote invocation cost
   */
  public static Scaling messageReceived(final FunctionCosts underlying, final Invocations invocations) {
    double remoteInvocationCost = 0;
    double localInvocationCost = 0;
    for (PerConfiguration configuration : invocations.getConfiguration()) {
      final FunctionCostsPerConfiguration configurationStats = underlying.getStatistics(configuration.getConfiguration());
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
    final FudgeDeserializer deserializer = new FudgeDeserializer(fudgeContext);
    final RemoteCalcNodeMessage message = deserializer.fudgeMsgToObject(RemoteCalcNodeMessage.class, msgEnvelope.getMessage());
    if (message instanceof Invocations) {
      messageReceived(getUnderlying(), (Invocations) message);
    } else {
      throw new UnsupportedOperationException("Unexpected message - " + message);
    }
  }

}
