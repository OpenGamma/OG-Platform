/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.rest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.JmsUtils;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.jms.JmsConnector;

/**
 * Publishes asynchronous results over JMS.
 * <p>
 * Always call {@link #stopPublishingResults()} when this result publisher is no longer required to ensure that associated resources are tidied up.
 */
public abstract class AbstractJmsResultPublisher {

  /** Logger */
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractJmsResultPublisher.class);
  private static final String SEQUENCE_NUMBER_FIELD_NAME = "#";

  private final FudgeContext _fudgeContext;
  private final FudgeSerializer _fudgeSerializationContext;
  private final JmsConnector _jmsConnector;
  private final ReentrantLock _lock = new ReentrantLock();
  private final AtomicLong _sequenceNumber = new AtomicLong();

  private final AtomicBoolean _isShutdown = new AtomicBoolean(false);
  private BlockingQueue<byte[]> _messageQueue = new LinkedBlockingQueue<byte[]>();

  private volatile Connection _connection;
  private volatile Session _session;
  private volatile MessageProducer _producer;

  /**
   * Creates an instance.
   * 
   * @param fudgeContext the Fudge context, not null
   * @param jmsConnector the JMS connector, may be null
   */
  public AbstractJmsResultPublisher(FudgeContext fudgeContext, JmsConnector jmsConnector) {
    _fudgeContext = fudgeContext;
    _fudgeSerializationContext = new FudgeSerializer(fudgeContext);
    _jmsConnector = jmsConnector;
  }

  //-------------------------------------------------------------------------
  /**
   * Stops listening to results from the underlying provider.
   */
  protected abstract void stopListener();

  /**
   * Begins listening to results from the underlying provider. When a result occurs, {@code #send(Object)} should be called to publish that result over JMS.
   */
  protected abstract void startListener();

  /**
   * Publishes a result over JMS.
   * <p>
   * This should only be called once results are required, as indicated by a call to {@link #startListener()}.
   * 
   * @param result the result, not null
   */
  protected void send(Object result) {
    s_logger.debug("Result received to forward over JMS: {}", result);
    MutableFudgeMsg resultMsg;
    synchronized (_fudgeSerializationContext) {
      resultMsg = _fudgeSerializationContext.objectToFudgeMsg(result);
    }
    FudgeSerializer.addClassHeader(resultMsg, result.getClass());
    long sequenceNumber = _sequenceNumber.getAndIncrement();
    resultMsg.add(SEQUENCE_NUMBER_FIELD_NAME, sequenceNumber);
    s_logger.debug("Sending result as fudge message with sequence number {}: {}", sequenceNumber, resultMsg);
    byte[] resultMsgByteArray = _fudgeContext.toByteArray(resultMsg);
    _messageQueue.add(resultMsgByteArray);
  }

  //-------------------------------------------------------------------------
  public void startPublishingResults(String destination) throws Exception {
    _lock.lock();
    try {
      startJmsIfRequired(destination);
      sendStartedSignal();
      startListener();
    } finally {
      _lock.unlock();
    }
  }

  private void startJmsIfRequired(String destination) throws Exception {
    if (_jmsConnector == null) {
      throw new OpenGammaRuntimeException("JMS not configured on server");
    }
    if (_producer == null) {
      try {
        _connection = _jmsConnector.getConnectionFactory().createConnection();
        _session = _connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        _producer = _session.createProducer(_session.createQueue(destination));
        _messageQueue.clear();
        startSenderThread();
        _connection.start();
      } catch (Exception e) {
        closeJms();
        throw e;
      }
    }
  }

  private void sendStartedSignal() {
    s_logger.debug("Sending started signal");

    // REVIEW jonathan 2012-02-03 -- until we have more than one control signal, it's sufficient to push through an
    // empty message.
    _messageQueue.add(_fudgeContext.toByteArray(_fudgeContext.newMessage()));
  }

  private void closeJms() {
    if (_connection != null) {
      //[PLAT-1809] Need to close all of these
      JmsUtils.closeMessageProducer(_producer);
      JmsUtils.closeSession(_session);
      JmsUtils.closeConnection(_connection);

      _connection = null;
      _session = null;
      _producer = null;

    }
  }

  private void startSenderThread() throws JMSException {
    Thread senderThread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
          byte[] nextMessage;
          try {
            nextMessage = _messageQueue.take();
            if (_isShutdown.get()) {
              break;
            }
            sendSync(nextMessage);
          } catch (Exception e) {
            s_logger.error("Failed to send message asynchronously", e);
          }
        }
      }
    }, String.format("JmsResultPublisher %s", _producer.getDestination()));
    senderThread.setDaemon(true);
    senderThread.start();
  }

  private void sendSync(byte[] buffer) {
    MessageProducer producer = _producer;
    if (producer == null) {
      s_logger.debug("Result received after publishing stopped");
      return;
    }
    try {
      BytesMessage msg = _session.createBytesMessage();
      msg.writeBytes(buffer);
      producer.send(msg);
    } catch (Exception e) {
      s_logger.error("Error while sending result over JMS. This result may never reach the client.", e);
    }
  }

  public void stopPublishingResults() throws JMSException {
    _lock.lock();
    try {
      s_logger.debug("Removing listener {}", this);
      stopListener();
      _isShutdown.set(true);
      _messageQueue.add(new byte[0]);
      closeJms();
    } finally {
      _lock.unlock();
    }
  }

}
