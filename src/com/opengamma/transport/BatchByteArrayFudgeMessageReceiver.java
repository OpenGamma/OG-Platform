/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.ArrayList;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;

import com.opengamma.util.ArgumentChecker;

/**
 * A dispatcher which will take batches of byte arrays, deserialize them to Fudge
 * messages, and then pass to a {@link BatchFudgeMessageReceiver}.
 *
 * @author kirk
 */
public class BatchByteArrayFudgeMessageReceiver implements BatchByteArrayMessageReceiver {
  private final BatchFudgeMessageReceiver _underlying;
  private final FudgeContext _fudgeContext;
  
  public BatchByteArrayFudgeMessageReceiver(BatchFudgeMessageReceiver underlying) {
    this(underlying, new FudgeContext());
  }
  
  public BatchByteArrayFudgeMessageReceiver(BatchFudgeMessageReceiver underlying, FudgeContext fudgeContext) {
    ArgumentChecker.checkNotNull(underlying, "Underlying FudgeMessageReceiver");
    ArgumentChecker.checkNotNull(fudgeContext, "Fudge Context");
    _underlying = underlying;
    _fudgeContext = fudgeContext;
  }

  /**
   * @return the underlying
   */
  public BatchFudgeMessageReceiver getUnderlying() {
    return _underlying;
  }

  /**
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public void messagesReceived(List<byte[]> messages) {
    List<FudgeMsgEnvelope> fudgeMessages = new ArrayList<FudgeMsgEnvelope>(messages.size());
    for(byte[] bytes: messages) {
      FudgeMsgEnvelope msgEnvelope = getFudgeContext().deserialize(bytes);
      fudgeMessages.add(msgEnvelope);
    }
    getUnderlying().messagesReceived(getFudgeContext(), fudgeMessages);
  }

}
