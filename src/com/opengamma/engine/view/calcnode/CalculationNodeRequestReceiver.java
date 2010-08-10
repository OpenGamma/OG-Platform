/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.List;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.ComputationTargetResolver;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionRepository;
import com.opengamma.engine.view.cache.ViewComputationCacheSource;
import com.opengamma.transport.FudgeRequestReceiver;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.InetAddressUtils;

// TODO kirk 2010-04-06 -- I don't like that this is a JobRequestSender rather than something else.
// Have to come up with a better solution for this.

/**
 * Receives messages corresponding to {@link CalculationJob}, invokes them,
 * and then responds with messages corresponding to {@link CalculationJobResult}.
 *
 */
public class CalculationNodeRequestReceiver extends AbstractCalculationNode implements FudgeRequestReceiver, JobRequestSender {
  
  private final ResultWriterFactory _resultWriterFactory;
  
  // useful in tests
  public CalculationNodeRequestReceiver(
      ViewComputationCacheSource cacheSource,
      FunctionRepository functionRepository,
      FunctionExecutionContext functionExecutionContext,
      ComputationTargetResolver targetResolver, 
      ViewProcessorQuerySender calcNodeQuerySender) {
    this(cacheSource, 
        functionRepository, 
        functionExecutionContext,
        targetResolver,
        calcNodeQuerySender,
        new DummyResultWriterFactory(),
        InetAddressUtils.getLocalHostName());
  }

  public CalculationNodeRequestReceiver(
      ViewComputationCacheSource cacheSource,
      FunctionRepository functionRepository,
      FunctionExecutionContext functionExecutionContext,
      ComputationTargetResolver targetResolver, 
      ViewProcessorQuerySender calcNodeQuerySender,
      ResultWriterFactory resultWriterFactory,
      String nodeId) {
    super(cacheSource, functionRepository, functionExecutionContext, targetResolver, calcNodeQuerySender, nodeId);
    
    ArgumentChecker.notNull(resultWriterFactory, "Result writer factory");
    _resultWriterFactory = resultWriterFactory;
  }

  @Override
  public FudgeFieldContainer requestReceived(
      FudgeDeserializationContext context,
      FudgeMsgEnvelope requestEnvelope) {
    CalculationJob job = CalculationJob.fromFudgeMsg(context, requestEnvelope.getMessage());
    CalculationJobResult jobResult = executeJob(job);
    return jobResult.toFudgeMsg(new FudgeSerializationContext(context.getFudgeContext()));
  }
  
  @Override
  public void sendRequest(CalculationJobSpecification jobSpec, 
      List<CalculationJobItem> items, 
      JobResultReceiver resultReceiver) {
    
    ResultWriter resultWriter = _resultWriterFactory.create(jobSpec, items, getNodeId());
    CalculationJob job = new CalculationJob(jobSpec, items, resultWriter);
    CalculationJobResult jobResult = executeJob(job);
    resultReceiver.resultReceived(jobResult);

  }

}
