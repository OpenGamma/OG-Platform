/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class ByteArrayFudgeMessageSender implements FudgeMessageSender {
  private final ByteArrayMessageSender _underlying;
  private final FudgeContext _fudgeContext;
  
  public ByteArrayFudgeMessageSender(ByteArrayMessageSender underlying) {
    this(underlying, new FudgeContext());
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
  public void send(FudgeFieldContainer message) {
    byte[] bytes = getFudgeContext().toByteArray(message);
    getUnderlying().send(bytes);
  }

}
