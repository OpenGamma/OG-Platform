/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

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
  
  public FudgeJobRequestSender(FudgeRequestSender underlying) {
    ArgumentChecker.notNull(underlying, "Underlying FudgeRequestSender");
    _underlying = underlying;
  }

  /**
   * @return the underlying
   */
  public FudgeRequestSender getUnderlying() {
    return _underlying;
  }

  @Override
  public void sendRequest(CalculationJob job, 
      final JobResultReceiver resultReceiver) {

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
