/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import java.util.Collection;
import java.util.Collections;

import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory to create Fudge senders.
 */
public class FudgeSenderFactory implements MarketDataSenderFactory {

  /**
   * The base sender.
   */
  private final FudgeMessageSender _fudgeMessageSender;

  /**
   * Creates a sender.
   * 
   * @param fudgeMessageSender  the base sender, not null
   */
  public FudgeSenderFactory(FudgeMessageSender fudgeMessageSender) {
    ArgumentChecker.notNull(fudgeMessageSender, "fudgeMessageSender");
    _fudgeMessageSender = fudgeMessageSender;
  }

  //-------------------------------------------------------------------------
  @Override
  public Collection<MarketDataSender> create(MarketDataDistributor distributor) {
    return Collections.<MarketDataSender>singleton(new FudgeSender(_fudgeMessageSender, distributor));
  }

}
