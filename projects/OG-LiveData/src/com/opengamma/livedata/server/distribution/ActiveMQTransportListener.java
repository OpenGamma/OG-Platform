/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import java.io.IOException;

import org.apache.activemq.transport.TransportListener;

import com.opengamma.util.ArgumentChecker;

/**
 * Disables sending JMS messages if ActiveMQ disconnection event is received.
 */
public class ActiveMQTransportListener implements TransportListener {
  
  private JmsSenderFactory _senderFactory;
  
  public ActiveMQTransportListener(JmsSenderFactory senderFactory) {
    ArgumentChecker.notNull(senderFactory, "JMS Sender factory");
    _senderFactory = senderFactory;
  }
  
  @Override
  public void onCommand(Object command) {
  }

  @Override
  public void onException(IOException error) {
  }

  @Override
  public void transportInterupted() {
    _senderFactory.transportInterrupted();
  }

  @Override
  public void transportResumed() {
    _senderFactory.transportResumed();
  }
  
}
