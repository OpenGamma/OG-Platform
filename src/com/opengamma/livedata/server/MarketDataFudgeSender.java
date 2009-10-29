/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsg;

import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * Sends Fudge-encoded market data.
 *
 * @author kirk
 */
public class MarketDataFudgeSender implements MarketDataFieldReceiver {
  private final FudgeMessageSender _fudgeMessageSender;
  
  public MarketDataFudgeSender(FudgeMessageSender fudgeMessageSender) {
    ArgumentChecker.checkNotNull(fudgeMessageSender, "Fudge Message Sender");
    _fudgeMessageSender = fudgeMessageSender;
  }

  /**
   * @return the fudgeMessageSender
   */
  public FudgeMessageSender getFudgeMessageSender() {
    return _fudgeMessageSender;
  }

  @Override
  public void marketDataReceived(LiveDataSpecification specification,
      FudgeFieldContainer fields) {
    LiveDataValueUpdateBean liveDataValueUpdateBean = new LiveDataValueUpdateBean(System.currentTimeMillis(), specification, fields);
    FudgeMsg fudgeMsg = liveDataValueUpdateBean.toFudgeMsg(getFudgeMessageSender().getFudgeContext());
    getFudgeMessageSender().send(fudgeMsg);
  }

}
