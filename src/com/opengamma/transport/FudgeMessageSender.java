/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

/**
 * 
 *
 * @author kirk
 */
public interface FudgeMessageSender {

  void send(FudgeMsg message);
  
  FudgeContext getFudgeContext();
}
