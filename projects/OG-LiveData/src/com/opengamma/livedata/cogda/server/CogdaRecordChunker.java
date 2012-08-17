/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.server;

import com.opengamma.transport.FudgeMessageSender;

/**
 * A class that is able to decode the raw stream of bytes for a fire hose provider
 * and produce them in a binary form for another purpose.
 * The canonical example is one which will read a socket-based stream, decode that
 * stream into discrete messages, and then distribute onto an MOM system for later
 * processing into a full {@code FireHoseLiveData} object.
 */
public class CogdaRecordChunker {
  
  /**
   * The destination for decoded messsages.
   */
  private FudgeMessageSender _fudgeSender;

  /**
   * Gets the fudgeSender.
   * @return the fudgeSender
   */
  public FudgeMessageSender getFudgeSender() {
    return _fudgeSender;
  }

  /**
   * Sets the fudgeSender.
   * @param fudgeSender  the fudgeSender
   */
  public void setFudgeSender(FudgeMessageSender fudgeSender) {
    _fudgeSender = fudgeSender;
  }

}
