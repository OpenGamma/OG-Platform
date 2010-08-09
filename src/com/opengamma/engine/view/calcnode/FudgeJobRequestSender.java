/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.ArgumentChecker;

/**
 * An implementation of {@link JobRequestSender} which uses an underlying
 * {@link FudgeRequestSender} to send Fudge-serialized requests and responses.
 */
public class FudgeJobRequestSender implements JobRequestSender {
  
  private final FudgeRequestSender _underlying;
  private final ResultWriterFactory _resultWriterFactory;
  
  /** May be null, which means that the compute node is not known */
  private final String _computeNodeId;
  
  // useful in tests
  public FudgeJobRequestSender(FudgeRequestSender underlying) {
    this(underlying, new DummyResultWriterFactory(), null);
  }
  
  public FudgeJobRequestSender(FudgeRequestSender underlying,
      ResultWriterFactory resultWriterFactory,
      String computeNodeId) {
    ArgumentChecker.notNull(underlying, "Underlying FudgeRequestSender");
    ArgumentChecker.notNull(resultWriterFactory, "Result writer factory");

    _underlying = underlying;
    _resultWriterFactory = resultWriterFactory;
    _computeNodeId = computeNodeId;
  }

  /**
   * @return the underlying
   */
  public FudgeRequestSender getUnderlying() {
    return _underlying;
  }

  @Override
  public void sendRequest(CalculationJobSpecification jobSpec, 
      List<CalculationJobItem> items, 
      final JobResultReceiver resultReceiver) {

    ResultWriter resultWriter = _resultWriterFactory.create(jobSpec, items, _computeNodeId);
    CalculationJob job = new CalculationJob(jobSpec, items, resultWriter);
    
    FudgeFieldContainer jobMsg = job.toFudgeMsg(new FudgeSerializationContext(getUnderlying().getFudgeContext()));
    getUnderlying().sendRequest(jobMsg, new FudgeMessageReceiver() {
      @Override
      public void messageReceived(
          FudgeContext fudgeContext,
          FudgeMsgEnvelope msgEnvelope) {
        CalculationJobResult jobResult = CalculationJobResult.fromFudgeMsg(new 
            FudgeDeserializationContext(fudgeContext), 
            msgEnvelope.getMessage());
        resultReceiver.resultReceived(jobResult);
      }
      
    });
  }

}
