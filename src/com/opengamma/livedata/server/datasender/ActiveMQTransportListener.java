/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.server.datasender;

import java.io.IOException;

import org.apache.activemq.transport.TransportListener;

/**
 * Disables sending JMS messages if ActiveMQ disconnection event is received.
 */
public class ActiveMQTransportListener implements TransportListener {
  
  private MarketDataFudgeJmsSender _sender;
  
  public ActiveMQTransportListener() {
  }
  
  public MarketDataFudgeJmsSender getSender() {
    return _sender;
  }

  public void setSender(MarketDataFudgeJmsSender sender) {
    _sender = sender;
  }

  @Override
  public void onCommand(Object command) {
  }

  @Override
  public void onException(IOException error) {
  }

  @Override
  public void transportInterupted() {
    _sender.transportInterrupted();
  }

  @Override
  public void transportResumed() {
    _sender.transportResumed();
  }
  
}
