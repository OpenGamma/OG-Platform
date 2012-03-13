/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.model.ReferenceDataRequestMessage;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class RemoteReferenceDataProvider implements ReferenceDataProvider {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(RemoteReferenceDataProvider.class);

  private final FudgeRequestSender _fudgeRequestSender;
  private final FudgeContext _fudgeContext;

  /**
   * @param fudgeRequestSender  the Fudge request sender
   */
  public RemoteReferenceDataProvider(FudgeRequestSender fudgeRequestSender) {
    this(fudgeRequestSender, FudgeContext.GLOBAL_DEFAULT);
  }
  
  public RemoteReferenceDataProvider(FudgeRequestSender fudgeRequestSender, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeRequestSender, "Fudge Request Sender");
    ArgumentChecker.notNull(fudgeContext, "Fudge Context");
    _fudgeRequestSender = fudgeRequestSender;
    _fudgeContext = fudgeContext;
  }
  
  @Override
  public ReferenceDataResult getFields(Set<String> securities,
      Set<String> fields) {
    ArgumentChecker.notEmpty(securities, "Securities");
    ArgumentChecker.notEmpty(fields, "Field Names");
    
    FudgeContext context = getFudgeContext();
    FudgeMsg msg = composeRequestMessage(context, securities, fields);
    RemoteReferenceDataReceiver receiver = new RemoteReferenceDataReceiver();
    s_logger.debug("sending remote fudge message {}", msg);
    _fudgeRequestSender.sendRequest(msg, receiver);
    try {
      receiver.getLatch().await();
    } catch (InterruptedException e) {
      s_logger.info("InterruptedException, request cannot be serviced right now");
      throw new OpenGammaRuntimeException("Unable to get fields because of InterruptedException", e);
    }
    return receiver.getReferenceDataResult();
  }

  /**
   * @param context  the Fudge context, not null
   * @param securities  the set of securities, not null
   * @param fields  the set of fields, not null
   * @return the Fudge message request, not null
   */
  protected FudgeMsg composeRequestMessage(FudgeContext context, Set<String> securities, Set<String> fields) {
    ArgumentChecker.notNull(context, "FudgeContext");
    ReferenceDataRequestMessage message = new ReferenceDataRequestMessage();
    message.setSecurity(securities);
    message.setField(fields);
    return message.toFudgeMsg(new FudgeSerializer(context));
  }

  /**
   * @return the Fudge context, not null
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
  private class RemoteReferenceDataReceiver implements FudgeMessageReceiver {
    private final CountDownLatch _latch = new CountDownLatch(1);
    private ReferenceDataResult _refDataResult;
    
    @Override
    public synchronized void messageReceived(FudgeContext fudgeContext,
        FudgeMsgEnvelope msgEnvelope) {
      ArgumentChecker.notNull(msgEnvelope, "FudgeMsgEnvelope");
      _refDataResult = ReferenceDataResult.fromFudgeMsg(msgEnvelope.getMessage(), _fudgeContext);
      getLatch().countDown();
    }
    /**
     * @return the response
     */
    public ReferenceDataResult getReferenceDataResult() {
      return _refDataResult;
    }
    
    public CountDownLatch getLatch() {
      return _latch;
    }
  }

}
