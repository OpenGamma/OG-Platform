/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class InMemoryByteArrayRequestConduit implements ByteArrayRequestSender {
  private final ByteArrayRequestReceiver _receiver;
  
  public InMemoryByteArrayRequestConduit(ByteArrayRequestReceiver receiver) {
    ArgumentChecker.checkNotNull(receiver, "Receiver");
    _receiver = receiver;
  }

  /**
   * @return the receiver
   */
  public ByteArrayRequestReceiver getReceiver() {
    return _receiver;
  }

  @Override
  public void sendRequest(byte[] request,
      ByteArrayMessageReceiver responseReceiver) {
    byte[] responseBytes = getReceiver().requestReceived(request);
    responseReceiver.messageReceived(responseBytes);
  }

}
