/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;

/**
 * 
 *
 * @author kirk
 */
public interface FudgeMessageReceiver {

  void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope);
}
