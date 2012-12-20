/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A dispatcher which will take batches of byte arrays, deserialize them to Fudge
 * messages, and then pass to a {@link BatchFudgeMessageReceiver}.
 */
public class BatchByteArrayFudgeMessageReceiver implements BatchByteArrayMessageReceiver {

  /**
   * The underlying Fudge receiver.
   */
  private final BatchFudgeMessageReceiver _underlying;
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates a receiver based on an underlying Fudge receiver.
   * @param underlying  the underlying receiver, not null
   */
  public BatchByteArrayFudgeMessageReceiver(BatchFudgeMessageReceiver underlying) {
    this(underlying, OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates a receiver based on an underlying Fudge receiver.
   * @param underlying  the underlying receiver, not null
   * @param fudgeContext  the context to use, not null
   */
  public BatchByteArrayFudgeMessageReceiver(BatchFudgeMessageReceiver underlying, FudgeContext fudgeContext) {
    ArgumentChecker.notNull(underlying, "underlying");
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying receiver.
   * @return the underlying receiver, not null
   */
  public BatchFudgeMessageReceiver getUnderlying() {
    return _underlying;
  }

  /**
   * Gets the Fudge context.
   * @return the fudge context, not null
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Converts byte arrays to Fudge messages.
   * @param messages  the list of byte arrays to process, not null
   */
  @Override
  public void messagesReceived(List<byte[]> messages) {
    List<FudgeMsgEnvelope> fudgeMessages = new ArrayList<FudgeMsgEnvelope>(messages.size());
    for (byte[] bytes : messages) {
      FudgeMsgEnvelope msgEnvelope = getFudgeContext().deserialize(bytes);
      fudgeMessages.add(msgEnvelope);
    }
    getUnderlying().messagesReceived(getFudgeContext(), fudgeMessages);
  }

}
