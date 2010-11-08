/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import java.util.Collection;
import java.util.Collections;

import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * Creates {@link FudgeSender}'s.
 */
public class FudgeSenderFactory implements MarketDataSenderFactory {
  
  private final FudgeMessageSender _fudgeMessageSender;
  
  public FudgeSenderFactory(FudgeMessageSender fudgeMessageSender) {
    ArgumentChecker.notNull(fudgeMessageSender, "Fudge message sender");
    _fudgeMessageSender = fudgeMessageSender;
  }
  
  @Override
  public Collection<MarketDataSender> create(MarketDataDistributor distributor) {
    return Collections.<MarketDataSender>singleton(new FudgeSender(_fudgeMessageSender, distributor));
  }

}
