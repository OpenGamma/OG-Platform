/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Receives byte array messages and dispatches them to a {@code FudgeRequestReceiver}.
 */
public class FudgeRequestDispatcher implements ByteArrayRequestReceiver {

  /**
   * The underlying receiver.
   */
  private final FudgeRequestReceiver _underlying;
  /**
   * The Fudge context.
   */
  private FudgeContext _fudgeContext;

  /**
   * Creates an instance based on an underlying receiver.
   * 
   * @param underlying  the underlying receiver, not null
   */
  public FudgeRequestDispatcher(FudgeRequestReceiver underlying) {
    this(underlying, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance based on an underlying receiver.
   * 
   * @param underlying  the underlying receiver, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public FudgeRequestDispatcher(FudgeRequestReceiver underlying, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying receiver.
   * 
   * @return the underlying receiver, not null
   */
  public FudgeRequestReceiver getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the Fudge context.
   * 
   * @return the Fudge context, not null
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  //-------------------------------------------------------------------------
  @Override
  public byte[] requestReceived(byte[] message) {
    FudgeMsgEnvelope requestEnvelope = getFudgeContext().deserialize(message);
    FudgeDeserializer deserializationContext = new FudgeDeserializer(getFudgeContext());
    FudgeMsg responseContainer = getUnderlying().requestReceived(deserializationContext, requestEnvelope);
    return getFudgeContext().toByteArray(responseContainer);
  }

}
