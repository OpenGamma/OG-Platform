/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport;

/**
 * Bi-directional and asymmetric connection for Fudge messages. Comprises a FudgeMessageReceiver and FudgeMessageSender.
 */
public interface FudgeConnection {

  void setFudgeMessageReceiver(FudgeMessageReceiver receiver);

  FudgeMessageSender getFudgeMessageSender();

  void setConnectionStateListener(FudgeConnectionStateListener listener);
  
  // REVIEW kirk 2012-07-23 -- This needs a way to tear down the connection.
  // See http://jira.opengamma.com/browse/PLAT-2458

}
