/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

import static com.opengamma.bbg.BloombergConstants.CATEGORY;
import static com.opengamma.bbg.BloombergConstants.DESCRIPTION;
import static com.opengamma.bbg.BloombergConstants.EXCEPTIONS;
import static com.opengamma.bbg.BloombergConstants.FIELD_ID;
import static com.opengamma.bbg.BloombergConstants.REASON;
import static com.opengamma.bbg.replay.BloombergTick.FIELDS_KEY;
import static com.opengamma.bbg.replay.BloombergTick.RECEIVED_TS_KEY;
import static com.opengamma.bbg.replay.BloombergTick.SECURITY_KEY;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.BlockingQueue;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Clock;
import org.threeten.bp.Instant;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.EventHandler;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Session;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class BloombergTickCollectorHandler implements EventHandler {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergTickCollectorHandler.class);
  private static final FudgeContext s_fudgeContext = new FudgeContext();
  
  private SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
  private BlockingQueue<FudgeMsg> _allTicksQueue;
  private final BloombergTicksCollector _ticksCollector;
  
  /**
   * @param allTicksQueue  the queue of ticks, not null
   * @param ticksCollector the ticks collector, not null
   */
  public BloombergTickCollectorHandler(BlockingQueue<FudgeMsg> allTicksQueue, BloombergTicksCollector ticksCollector) {
    ArgumentChecker.notNull(allTicksQueue, "allTicksQueue");
    ArgumentChecker.notNull(allTicksQueue, "ticksCollector");
    _allTicksQueue = allTicksQueue;
    _ticksCollector = ticksCollector;
  }

  public void processEvent(Event event, Session session) {
    try {
      switch (event.eventType().intValue()) {
        case Event.EventType.Constants.SUBSCRIPTION_DATA:
          processSubscriptionDataEvent(event, session);
          break;
        case Event.EventType.Constants.SUBSCRIPTION_STATUS:
          processSubscriptionStatus(event, session);
          break;
        default:
          processMiscEvents(event, session);
          break;
      }
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Unable to process subscription event", e);
    }
  }

  private void processSubscriptionStatus(Event event, Session session) throws Exception {
    s_logger.debug("Processing SUBSCRIPTION_STATUS");
    MessageIterator msgIter = event.messageIterator();
    while (msgIter.hasNext()) {
      Message msg = msgIter.next();
      String topic = (String) msg.correlationID().object();
      s_logger.debug("{}: {} - {}", new Object[]{_dateFormat
          .format(Calendar.getInstance().getTime()), topic, msg.messageType()});
      if (msg.messageType().equals("SubscriptionTerminated")) {
        s_logger.warn("SubscriptionTerminated for {}", msg.correlationID().object());
        s_logger.warn("msg = {}", msg.toString());
      }

      if (msg.hasElement(REASON)) {
        // This can occur on SubscriptionFailure.
        Element reason = msg.getElement(REASON);
        s_logger.warn("{}: security={} category={} description={}", 
            new Object[]{_dateFormat.format(Calendar.getInstance().getTime()), topic, reason.getElement(CATEGORY).getValueAsString(), reason.getElement(DESCRIPTION).getValueAsString()});
      }

      if (msg.hasElement(EXCEPTIONS)) {
        // This can occur on SubscriptionStarted if at least
        // one field is good while the rest are bad.
        Element exceptions = msg.getElement(EXCEPTIONS);
        for (int i = 0; i < exceptions.numValues(); ++i) {
          Element exInfo = exceptions.getValueAsElement(i);
          Element fieldId = exInfo.getElement(FIELD_ID);
          Element reason = exInfo.getElement(REASON);
          s_logger.warn("{}: security={} field={} category={}", 
              new Object[]{_dateFormat.format(Calendar.getInstance().getTime()), topic, fieldId.getValueAsString(), reason.getElement(CATEGORY).getValueAsString()});
        }
      }
      s_logger.debug("");
    }
  }

  private void processSubscriptionDataEvent(Event event, Session session) throws Exception {
    s_logger.debug("Processing SUBSCRIPTION_DATA");
    if (tickWriterIsAlive()) {
      MessageIterator msgIter = event.messageIterator();
      while (msgIter.hasNext()) {
        Message msg = msgIter.next();
        if (isValidMessage(msg)) {
          String securityDes = (String) msg.correlationID().object();
          MutableFudgeMsg tickMsg = s_fudgeContext.newMessage();
          Instant instant = Clock.systemUTC().instant();
          long epochMillis = instant.toEpochMilli();
          tickMsg.add(RECEIVED_TS_KEY, epochMillis);
          tickMsg.add(SECURITY_KEY, securityDes);
          tickMsg.add(FIELDS_KEY, BloombergDataUtils.parseElement(msg.asElement()));
          s_logger.debug("{}: {} - {}", new Object[]{_dateFormat
              .format(Calendar.getInstance().getTime()), securityDes, msg.messageType()});
          s_logger.debug("{}", msg.asElement());
          _allTicksQueue.put(tickMsg);
          s_logger.debug("singleQueueSize {}", _allTicksQueue.size());
        }
      }
    } else {
      stopTickCollection();
    }
  }

  private boolean isValidMessage(Message msg) {
    return msg != null && msg.correlationID() != null;
  }

  private void stopTickCollection() {
    _ticksCollector.stop();
  }

  private boolean tickWriterIsAlive() {
    return _ticksCollector.isTickWriterAlive();
  }

  private void processMiscEvents(Event event, Session session) throws Exception {
    s_logger.info("Processing {}", event.eventType());
    MessageIterator msgIter = event.messageIterator();
    while (msgIter.hasNext()) {
      Message msg = msgIter.next();
      s_logger.debug("{}: {}\n", _dateFormat
          .format(Calendar.getInstance().getTime()), msg.messageType());
    }
  } 
}
