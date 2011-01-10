/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsg;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 * 
 * @author yomi
 */
public class ByteArrayFudgeRequestSender implements FudgeRequestSender {

  private FudgeContext _fudgeContext;
  private ByteArrayRequestSender _underlying;

  public ByteArrayFudgeRequestSender(ByteArrayRequestSender underlying) {
    this(underlying, new FudgeContext());
  }

  public ByteArrayFudgeRequestSender(ByteArrayRequestSender underlying,
      FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(underlying, "underlying");
    _fudgeContext = fudgeContext;
    _underlying = underlying;
  }

  @Override
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
  public ByteArrayRequestSender getUnderlying() {
    return _underlying;
  }

  @Override
  public void sendRequest(FudgeFieldContainer request,
      FudgeMessageReceiver responseReceiver) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(responseReceiver, "responseReceiver");
    if (!(request instanceof FudgeMsg)) {
      throw new OpenGammaRuntimeException("request not a FudgeMsg Type");
    }
    FudgeMsg msg = (FudgeMsg) request;
    byte[] bytes = getFudgeContext().toByteArray(msg);
    
    ByteArrayMessageReceiver receiver = new ByteArrayFudgeMessageReceiver(responseReceiver, getFudgeContext());
    _underlying.sendRequest(bytes, receiver);
  }
  
}
