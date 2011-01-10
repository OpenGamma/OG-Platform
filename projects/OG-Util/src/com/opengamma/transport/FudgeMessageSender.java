/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
