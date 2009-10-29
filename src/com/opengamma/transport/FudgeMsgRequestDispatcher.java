/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;

import com.opengamma.util.ArgumentChecker;

/**
 * Receives byte array messages and dispatches them to a {@link FudgeMsgRequestReceiver}.
 *
 * @author kirk
 */
public class FudgeMsgRequestDispatcher implements ByteArrayRequestReceiver {
  private final FudgeMsgRequestReceiver _underlying;
  private final FudgeContext _fudgeContext;
  
  public FudgeMsgRequestDispatcher(FudgeMsgRequestReceiver underlying, FudgeContext fudgeContext) {
    ArgumentChecker.checkNotNull(underlying, "Underlying FudgeMsgRequestReceiver");
    ArgumentChecker.checkNotNull(fudgeContext, "Fudge Context");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  /**
   * @return the underlying
   */
  public FudgeMsgRequestReceiver getUnderlying() {
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
    FudgeFieldContainer responseContainer = getUnderlying().requestReceived(getFudgeContext(), requestEnvelope);
    if(!(responseContainer instanceof FudgeMsg)) {
      throw new IllegalArgumentException("FudgeMsgRequestDispatcher can only currently handle FudgeMsg.");
    }
    FudgeMsg responseMsg = (FudgeMsg) responseContainer;
    byte[] responseBytes = getFudgeContext().toByteArray(responseMsg);
    return responseBytes;
  }

}
