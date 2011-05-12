/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.time.Instant;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.CycleCompletedCall;
import com.opengamma.engine.view.listener.CycleExecutionFailedCall;
import com.opengamma.engine.view.listener.ProcessCompletedCall;
import com.opengamma.engine.view.listener.ProcessTerminatedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompilationFailedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompiledCall;
import com.opengamma.engine.view.listener.ViewResultListener;

/**
 * Publishes asynchronous results from a view client over JMS.
 * <p>
 * Always call {@link #stopPublishingResults()} when this result publisher is no longer required to ensure that
 * associated resources are tidied up.
 */
public class JmsResultPublisher implements ViewResultListener {

  private static final Logger s_logger = LoggerFactory.getLogger(JmsResultPublisher.class);
  private static final String SEQUENCE_NUMBER_FIELD_NAME = "#";
  
  private final ViewClient _viewClient;
  private final FudgeContext _fudgeContext;
  private final FudgeSerializationContext _fudgeSerializationContext;
  private final ConnectionFactory _connectionFactory;
  private final ReentrantLock _lock = new ReentrantLock();
  private final AtomicLong _sequenceNumber = new AtomicLong();

  private final AtomicBoolean _isShutdown = new AtomicBoolean(false);
  private BlockingQueue<byte[]> _messageQueue = new LinkedBlockingQueue<byte[]>();
  
  private volatile Connection _connection;
  private volatile Session _session;
  private volatile MessageProducer _producer;

  public JmsResultPublisher(ViewClient viewClient, FudgeContext fudgeContext, ConnectionFactory connectionFactory) {
    _viewClient = viewClient;
    _fudgeContext = fudgeContext;
    _fudgeSerializationContext = new FudgeSerializationContext(fudgeContext);
    _connectionFactory = connectionFactory;
  }
  
  public void startPublishingResults(String destination) throws Exception {
    _lock.lock();
    try {
      s_logger.debug("Setting listener {} on view client {}'s results", this, _viewClient);
      startJmsIfRequired(destination);
      _viewClient.setResultListener(this);
    } finally {
      _lock.unlock();
    }
  }

  private void startJmsIfRequired(String destination) throws Exception {
    if (_producer == null) {
      try {
        _connection = _connectionFactory.createConnection();
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
  
  private void closeJms() {
    if (_connection != null) {
      try {
        _connection.close();
      } catch (Exception e) {
        s_logger.error("Error closing JMS connection", e);
      } finally {
        _connection = null;
        _session = null;
        _producer = null;
      }
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
    }, "QueueingJmsByteArrayMessageSender %s" + _producer.getDestination());
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
      s_logger.debug("Removing listener {} from view client {}'s results", this, _viewClient);
      _viewClient.setResultListener(null);
      _isShutdown.set(true);
      _messageQueue.add(new byte[0]);
      closeJms();
    } finally {
      _lock.unlock();
    }
  }
  
  //-------------------------------------------------------------------------
  @Override
  public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition) {
    send(new ViewDefinitionCompiledCall(compiledViewDefinition));
  }  
  
  @Override
  public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
    send(new ViewDefinitionCompilationFailedCall(valuationTime, exception));
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    send(new CycleCompletedCall(fullResult, deltaResult));
  }

  @Override
  public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
    send(new CycleExecutionFailedCall(executionOptions, exception));
  }

  @Override
  public void processCompleted() {
    send(new ProcessCompletedCall());
  }

  @Override
  public void processTerminated(boolean executionInterrupted) {
    send(new ProcessTerminatedCall(executionInterrupted));
  }

  //-------------------------------------------------------------------------
  private void send(Object result) {
    s_logger.debug("Result received to forward over JMS: {}", result);    
    MutableFudgeMsg resultMsg = _fudgeSerializationContext.objectToFudgeMsg(result);
    FudgeSerializationContext.addClassHeader(resultMsg, result.getClass());
    long sequenceNumber = _sequenceNumber.getAndIncrement();
    resultMsg.add(SEQUENCE_NUMBER_FIELD_NAME, sequenceNumber);
    s_logger.debug("Sending result as fudge message with sequence number {}: {}", sequenceNumber, resultMsg);
    byte[] resultMsgByteArray = _fudgeContext.toByteArray(resultMsg);
    _messageQueue.add(resultMsgByteArray);
  }
  
}
