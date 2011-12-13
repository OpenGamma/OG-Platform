/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.distribution;

import java.io.IOException;

import org.apache.activemq.transport.TransportListener;

import com.opengamma.util.ArgumentChecker;

/**
 * Links the ActiveMQ JMS messages for transport interrupted/resumed to live data.
 * <p>
 * This disables sending JMS messages if ActiveMQ disconnection event is received.
 */
public class ActiveMQTransportListener implements TransportListener {

  /**
   * The JMS sender factory.
   */
  private JmsSenderFactory _senderFactory;

  /**
   * Creates an instance.
   * 
   * @param senderFactory  the sender factory to use, not null
   */
  public ActiveMQTransportListener(JmsSenderFactory senderFactory) {
    ArgumentChecker.notNull(senderFactory, "JMS Sender factory");
    _senderFactory = senderFactory;
  }

  //-------------------------------------------------------------------------
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
