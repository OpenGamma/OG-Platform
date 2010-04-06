/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server;

import org.fudgemsg.FudgeFieldContainer;

/**
 * 
 *
 * @author kirk
 */
public interface MarketDataFieldReceiver {

  void marketDataReceived(Subscription subscription, FudgeFieldContainer fields);
}
