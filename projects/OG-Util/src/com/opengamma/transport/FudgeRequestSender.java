/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

/**
 * Fudge message based RPC style calling interface. 
 */
public interface FudgeRequestSender {
  FudgeContext getFudgeContext();

  /**
   * Sends a request message and a future response message will be passed to the receiver callback object.
   * The implementation must support concurrent calls to sendRequest that do not corrupt the underlying
   * transport. In the case of concurrent calls, it is acceptable for the messages received to be out of
   * order; i.e.
   * 
   * Client sends A & B concurrently from threads Ta and Tb.
   * Server receives either A then B, or B then A. It responds with A' and B'.
   * Either:
   *  Ta (which sent A) will receive response A' and Tb (which sent B) will receive response B', or
   *  Ta (which sent A) will receive response B' and Tb (which sent A) will receive response A'
   * 
   * If this is a problem, serialize all calls external to this object, or wrap it in a FudgeSynchronousClient
   * which will pair responses to the original requests.
   * 
   * @param request message to send
   * @param responseReceiver callback for a received message
   */
  void sendRequest(FudgeMsg request, FudgeMessageReceiver responseReceiver);
}
