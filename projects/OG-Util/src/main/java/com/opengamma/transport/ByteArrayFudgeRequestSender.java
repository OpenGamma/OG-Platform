/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A sender of Fudge messages.
 */
public class ByteArrayFudgeRequestSender implements FudgeRequestSender {

  /**
   * The underlying sender.
   */
  private ByteArrayRequestSender _underlying;
  /**
   * The Fudge context.
   */
  private FudgeContext _fudgeContext;

  /**
   * Creates an instance based on an underlying sender.
   * 
   * @param underlying  the underlying sender, not null
   */
  public ByteArrayFudgeRequestSender(ByteArrayRequestSender underlying) {
    this(underlying, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance based on an underlying sender.
   * 
   * @param underlying  the underlying sender, not null
   * @param fudgeContext  the Fudge context, not null
   */
  public ByteArrayFudgeRequestSender(ByteArrayRequestSender underlying, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
    _underlying = underlying;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying sender.
   * 
   * @return the underlying sender, not null
   */
  public ByteArrayRequestSender getUnderlying() {
    return _underlying;
  }

  @Override
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  //-------------------------------------------------------------------------
  @Override
  public void sendRequest(FudgeMsg request, FudgeMessageReceiver responseReceiver) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(responseReceiver, "responseReceiver");
    byte[] bytes = getFudgeContext().toByteArray(request);
    ByteArrayMessageReceiver receiver = new ByteArrayFudgeMessageReceiver(responseReceiver, getFudgeContext());
    _underlying.sendRequest(bytes, receiver);
  }

}
