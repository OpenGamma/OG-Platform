/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.wire.FudgeMsgWriter;

import com.opengamma.OpenGammaRuntimeException;
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
  private final boolean _compress;
  
  public ByteArrayFudgeMessageSender(ByteArrayMessageSender underlying) {
    this(underlying, OpenGammaFudgeContext.getInstance());
  }
  
  public ByteArrayFudgeMessageSender(ByteArrayMessageSender underlying, FudgeContext fudgeContext) {
    this(underlying, fudgeContext, false);
  }
  
  public ByteArrayFudgeMessageSender(ByteArrayMessageSender underlying, FudgeContext fudgeContext, boolean compress) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
    _compress = compress;
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

  /**
   * @return whether to compress
   */
  public boolean isCompress() {
    return _compress;
  }

  @Override
  public void send(FudgeMsg message) {
    byte[] bytes = null;
    if (isCompress()) {
      try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(baos);
        FudgeMsgWriter msgWriter = getFudgeContext().createMessageWriter(gzip);
        msgWriter.writeMessage(message);
        msgWriter.flush();
        gzip.close();
        bytes = baos.toByteArray();
      } catch (IOException ioe) {
        throw new OpenGammaRuntimeException("IOException should not be able to be thrown in this context", ioe);
      }
    } else {
      bytes = getFudgeContext().toByteArray(message);
    }
    getUnderlying().send(bytes);
  }

}
