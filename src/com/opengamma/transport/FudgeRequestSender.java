/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;

/**
 * 
 *
 * @author yomi
 */
public interface FudgeRequestSender {
  FudgeContext getFudgeContext();
  void sendRequest(FudgeFieldContainer request, FudgeMessageReceiver responseReceiver);
}
