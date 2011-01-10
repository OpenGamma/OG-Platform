/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializationContext;

import com.opengamma.util.ArgumentChecker;

/**
 * Receives byte array messages and dispatches them to a {@link FudgeRequestReceiver}.
 */
public class FudgeRequestDispatcher implements ByteArrayRequestReceiver {
  private final FudgeRequestReceiver _underlying;
  private final FudgeContext _fudgeContext;
  
  public FudgeRequestDispatcher(FudgeRequestReceiver underlying) {
    this(underlying, new FudgeContext());
  }
  
  public FudgeRequestDispatcher(FudgeRequestReceiver underlying, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  /**
   * @return the underlying
   */
  public FudgeRequestReceiver getUnderlying() {
    return _underlying;
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public byte[] requestReceived(byte[] message) {
    FudgeMsgEnvelope requestEnvelope = getFudgeContext().deserialize(message);
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(getFudgeContext()); 
    FudgeFieldContainer responseContainer = getUnderlying().requestReceived(deserializationContext, requestEnvelope);
    if (!(responseContainer instanceof FudgeMsg)) {
      throw new IllegalArgumentException("FudgeMsgRequestDispatcher can only currently handle FudgeMsg");
    }
    FudgeMsg responseMsg = (FudgeMsg) responseContainer;
    byte[] responseBytes = getFudgeContext().toByteArray(responseMsg);
    return responseBytes;
  }

}
