/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.transport.FudgeRequestReceiver;

// TODO kirk 2010-04-06 -- I don't like that this is a JobRequestSender rather than something else.
// Have to come up with a better solution for this.

/**
 * Receives messages corresponding to {@link CalculationJob}, invokes them,
 * and then responds with messages corresponding to {@link CalculationJobResult}.
 *
 * @author kirk
 */
public class CalculationNodeRequestReceiver
extends AbstractCalculationNode
implements FudgeRequestReceiver, JobRequestSender {

  /**
   * @param cacheSource
   * @param functionRepository
   * @param calcNodeQuerySender 
   * @param securityMaster
   * @param jobSource
   * @param completionNotifier
   */
  public CalculationNodeRequestReceiver(
      ViewComputationCacheSource cacheSource,
      FunctionRepository functionRepository,
      FunctionExecutionContext functionExecutionContext,
      ComputationTargetResolver targetResolver, 
      ViewProcessorQuerySender calcNodeQuerySender) {
    super(cacheSource, functionRepository, functionExecutionContext, targetResolver, calcNodeQuerySender);
  }

  @Override
  public FudgeFieldContainer requestReceived(
      FudgeDeserializationContext context,
      FudgeMsgEnvelope requestEnvelope) {
    CalculationJob job = CalculationJob.fromFudgeMsg(context, requestEnvelope.getMessage ());
    CalculationJobResult jobResult = executeJob(job);
    return jobResult.toFudgeMsg(context.getFudgeContext());
  }

  @Override
  public void sendRequest(CalculationJob job, JobResultReceiver resultReceiver) {
    CalculationJobResult jobResult = executeJob(job);
    resultReceiver.resultReceived(jobResult);
  }

}
