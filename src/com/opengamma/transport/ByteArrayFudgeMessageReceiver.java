/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class ByteArrayFudgeMessageReceiver implements ByteArrayMessageReceiver {
  private final FudgeMessageReceiver _underlying;
  private final FudgeContext _fudgeContext;
  
  public ByteArrayFudgeMessageReceiver(FudgeMessageReceiver underlying) {
    this(underlying, new FudgeContext());
  }
  
  public ByteArrayFudgeMessageReceiver(FudgeMessageReceiver underlying, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "Underlying FudgeMessageReceiver");
    ArgumentChecker.notNull(fudgeContext, "Fudge Context");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  /**
   * @return the underlying
   */
  public FudgeMessageReceiver getUnderlying() {
    return _underlying;
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public void messageReceived(byte[] message) {
    FudgeMsgEnvelope msgEnvelope = getFudgeContext().deserialize(message);
    getUnderlying().messageReceived(getFudgeContext(), msgEnvelope);
  }

}
