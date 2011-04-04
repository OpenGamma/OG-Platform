/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
