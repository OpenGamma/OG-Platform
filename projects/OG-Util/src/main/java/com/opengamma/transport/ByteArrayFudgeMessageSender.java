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
 * 
 *
 * @author kirk
 */
public class ByteArrayFudgeMessageSender implements FudgeMessageSender {
  private final ByteArrayMessageSender _underlying;
  private final FudgeContext _fudgeContext;
  
  public ByteArrayFudgeMessageSender(ByteArrayMessageSender underlying) {
    this(underlying, OpenGammaFudgeContext.getInstance());
  }
  
  public ByteArrayFudgeMessageSender(ByteArrayMessageSender underlying, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  /**
   * @return the underlying
   */
  public ByteArrayMessageSender getUnderlying() {
    return _underlying;
  }

  /**
   * @return the fudgeContext
   */
  @Override
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public void send(FudgeMsg message) {
    byte[] bytes = getFudgeContext().toByteArray(message);
    getUnderlying().send(bytes);
  }

}
