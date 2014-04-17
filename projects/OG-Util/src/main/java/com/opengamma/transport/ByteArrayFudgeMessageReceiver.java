/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.wire.FudgeMsgReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A message receiver that receives byte array messages and handles them using a
 * {@link BatchFudgeMessageReceiver}.
 */
public class ByteArrayFudgeMessageReceiver implements ByteArrayMessageReceiver {
  private static final Logger s_logger = LoggerFactory.getLogger(ByteArrayFudgeMessageReceiver.class);

  /**
   * The underlying Fudge receiver.
   */
  private final FudgeMessageReceiver _underlying;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;
  /**
   * Whether to turn on compression on reading messages.
   */
  private final boolean _compress;

  /**
   * Creates a receiver based on an underlying Fudge receiver.
   * @param underlying  the underlying receiver, not null
   */
  public ByteArrayFudgeMessageReceiver(FudgeMessageReceiver underlying) {
    this(underlying, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates a receiver based on an underlying Fudge receiver.
   * @param underlying  the underlying receiver, not null
   * @param fudgeContext  the context to use, not null
   */
  public ByteArrayFudgeMessageReceiver(FudgeMessageReceiver underlying, FudgeContext fudgeContext) {
    this(underlying, fudgeContext, false);
  }
  
  /**
   * Creates a receiver based on an underlying Fudge receiver.
   * @param underlying  the underlying receiver, not null
   * @param fudgeContext  the context to use, not null
   * @param compress whether input data is gzip compressed
   */
  public ByteArrayFudgeMessageReceiver(FudgeMessageReceiver underlying, FudgeContext fudgeContext, boolean compress) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
    _compress = compress;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying Fudge receiver.
   * @return the underlying Fudge receiver, not null
   */
  public FudgeMessageReceiver getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the Fudge context.
   * @return the fudge context, not null
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the compress setting.
   * @return whether messages are compressed
   */
  public boolean isCompress() {
    return _compress;
  }

  //-------------------------------------------------------------------------
  /**
   * Receives the byte array message and processes it using the underlying Fudge receiver.
   * @param message  the byte array message, not null
   */
  @Override
  public void messageReceived(byte[] message) {
    FudgeMsgEnvelope msgEnvelope = null;
    if (isCompress()) {
      try {
        ByteArrayInputStream bais = new ByteArrayInputStream(message);
        GZIPInputStream gzip = new GZIPInputStream(bais);
        FudgeMsgReader msgReader = getFudgeContext().createMessageReader(gzip);
        msgEnvelope = msgReader.nextMessageEnvelope();
      } catch (IOException ioe) {
        throw new OpenGammaRuntimeException("IOException should not be able to be thrown in this context", ioe);
      }
    } else {
      msgEnvelope = getFudgeContext().deserialize(message);
    }
    s_logger.debug("Msg of size {} had {} fields", message.length, msgEnvelope.getMessage().getNumFields());
    getUnderlying().messageReceived(getFudgeContext(), msgEnvelope);
  }

}
