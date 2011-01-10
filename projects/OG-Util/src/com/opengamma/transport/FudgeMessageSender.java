/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;

/**
 * 
 *
 * @author kirk
 */
public interface FudgeMessageSender {

  void send(FudgeFieldContainer message);
  
  FudgeContext getFudgeContext();
}
