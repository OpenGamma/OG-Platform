/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.livedata;

import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Session;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.livedata.server.AbstractEventDispatcher;
import com.opengamma.util.ArgumentChecker;

/**
 * This is the job which actually will dispatch messages from Bloomberg to the various
 * internal consumers.
 *
 * @author kirk
 */
public class BloombergEventDispatcher extends AbstractEventDispatcher {
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergEventDispatcher.class);
  private final Session _bloombergSession;
  
  public BloombergEventDispatcher(BloombergLiveDataServer server, Session bloombergSession) {
    super(server);
    
    ArgumentChecker.notNull(bloombergSession, "Bloomberg Session");
    _bloombergSession = bloombergSession;
  }

  /**
   * @return the bloombergSession
   */
  public Session getBloombergSession() {
    return _bloombergSession;
  }

  @Override
  protected void preStart() {
    super.preStart();
  }

  @Override
  protected void dispatch(long maxWaitMilliseconds) {
    Event event = null;
    try {
      event = getBloombergSession().nextEvent(1000L);
    } catch (InterruptedException e) {
      Thread.interrupted();
    }
    if (event == null) {
      return;
    }
    MessageIterator msgIter = event.messageIterator();
    while (msgIter.hasNext()) {
      Message msg = msgIter.next();
      String bbgUniqueId = msg.topicName();
      
      if (event.eventType() == Event.EventType.SUBSCRIPTION_DATA) {
        
        FudgeMsg eventAsFudgeMsg = BloombergDataUtils.parseElement(msg.asElement());
        getServer().liveDataReceived(bbgUniqueId, eventAsFudgeMsg);
        return;
        
      } 
        
      s_logger.info("Got event {} {} {}", new Object[] {event.eventType(), bbgUniqueId, msg.asElement()});
        
      if (event.eventType() == Event.EventType.SESSION_STATUS) {
        
        if (msg.messageType().toString().equals("SessionTerminated")) {
          disconnected();
        } 
      }
    }
  }

}
