/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.language.connector.debug;

import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.UnmodifiableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.language.connector.Client;
import com.opengamma.language.connector.ClientContext;
import com.opengamma.language.connector.UserMessage;
import com.opengamma.language.connector.UserMessagePayload;
import com.opengamma.language.context.MutableSessionContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.SessionContextInitializationEventHandler;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionStateListener;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.socket.SocketFudgeConnection;

/**
 * Debug client deferring calls to a remote process.
 */
public class DebugClient extends Client implements FudgeMessageReceiver, FudgeConnectionStateListener {
  
  /**
   * A system setting used to provide a connection string of the form host:port
   */
  private static final Logger s_logger = LoggerFactory.getLogger(DebugClient.class);
  private static final String DEBUG_CLIENT_CONNECTION_SETTING = "settings.debug.connection";
  private static final String SESSION_CONTEXT_STASH = "com.opengamma.language.connector.debug.StashMessage";
  private static final int CONNECTION_RETRY_DELAY = 3000;
  
  private final String _resourceHost;
  private final int _resourcePortNumber;
  
  private SocketFudgeConnection _connection;
  private AtomicBoolean _connectionFailed = new AtomicBoolean();

  protected DebugClient(final ClientContext clientContext, final String inputPipeName, final String outputPipeName,
      final SessionContext session) {
    super(clientContext, inputPipeName, outputPipeName, session);
    
    String connectionString = System.getProperty(DEBUG_CLIENT_CONNECTION_SETTING);
    if (StringUtils.isBlank(connectionString)) {
      throwUsageException();
    }
    String[] connectionBits = connectionString.split(":");
    if (connectionBits.length < 1 || connectionBits.length > 2) {
      throwUsageException();
    }
    _resourceHost = connectionBits[0];
    _resourcePortNumber = connectionBits.length == 2 ? Integer.parseInt(connectionBits[1]) : DebugService.DEFAULT_PORT;
  }
  
  //-------------------------------------------------------------------------

  @Override
  protected SessionContextInitializationEventHandler getSessionInitializer() {
    final SessionContextInitializationEventHandler superInitializer = super.getSessionInitializer();
    return new SessionContextInitializationEventHandler() {

      @Override
      public void initContext(final MutableSessionContext context) {
        superInitializer.initContext(context);
      }

      @Override
      public void initContextWithStash(final MutableSessionContext context, final FudgeMsg stash) {
        superInitializer.initContextWithStash(context, stash);
        context.setValue(SESSION_CONTEXT_STASH, new UnmodifiableFudgeMsg(FudgeContext.GLOBAL_DEFAULT, stash));
      }

    };
  }

  @Override
  protected FudgeMsg getStashMessage() {
    return getSessionContext().getValue(SESSION_CONTEXT_STASH);
  }
  
  @Override
  protected void setStashMessage(FudgeMsg stashMessage) {
  }

  @Override
  protected void doDispatchUserMessage(FudgeMsg msg) {
    if (_connectionFailed.get()) {
      // The connection has failed so no point attempting to execute
      respondFailure(msg);
      return;
    }
    try {
      getOrCreateConnection().getFudgeMessageSender().send(msg);
    } catch (Exception e) {
      // Just do the default failure response here. If the connection has failed, we'll be notified and a restart
      // sequence will begin, eventually causing this process to end in order that it is restarted by the service
      // wrapper.
      respondFailure(msg);
    }
  }
  
  private void respondFailure(FudgeMsg msg) {
    // At this point we need to inspect the message to understand its semantics, rather than simply acting as a
    // forwarder.
    UserMessage incomingMessage = getClientContext().getFudgeContext().fromFudgeMsg(UserMessage.class, msg);
    if (incomingMessage.getHandle() == null) {
      s_logger.info("Unable to forward message {} due to connection failure. No response expected so message will be dropped.", msg);
      return;
    }
    
    UserMessage fakeResponse = new UserMessage(incomingMessage.getHandle(), UserMessagePayload.EMPTY_PAYLOAD);
    s_logger.info("Unable to forward message {} due to connection failure. Faking the expected response with an empty payload.", msg);
    sendUserMessage(fakeResponse);
  }
  
  @Override
  protected void doPoison() {
    super.doPoison();
    disconnect();
  }

  //-------------------------------------------------------------------------
  
  @Override
  public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
    sendUserMessage(msgEnvelope.getMessage());
  }

  //-------------------------------------------------------------------------

  @Override
  public void connectionReset(FudgeConnection connection) {
    s_logger.info("Connection reset: {}", connection);
  }

  @Override
  public void connectionFailed(FudgeConnection connection, Exception cause) {
    if (isPoisoned()) {
      return;
    }
    s_logger.warn("Connection terminated by remote host: " + connection, cause);
    _connectionFailed.set(true);
    
    // The remote host has most likely gone offline. Attempt to reconnect, then once we have established that it is
    // online, kill this process to cause the service wrapper to restart. In particular, this means that live data
    // subscriptions will be recreated.
    beginRestartSequence();
  }
  
  private void beginRestartSequence() {
    synchronized (this) {
      try {
        _connection.stop();
      } catch (Exception e) {
        s_logger.info("Error stopping failed connection", e);
      }
      _connection = null;
      try {
        // Confirm that the connection can be established again before restarting
        createConnection();
      } catch (InterruptedException e) {
        s_logger.warn("Interrupted while attempting to re-establish connection", e);
        // Might as well proceed to exit
      } finally {
        // Now exit, and cause the service wrapper to restart the process
        System.exit(1);
      }
    }
  }
  
  //-------------------------------------------------------------------------
  
  private FudgeConnection getOrCreateConnection() throws InterruptedException {
    if (_connection == null) {
      synchronized (this) {
        if (_connection == null) {
          _connection = createConnection();
        }
      }
    }
    return _connection;
  }
  
  private SocketFudgeConnection createConnection() throws InterruptedException {    
    SocketFudgeConnection connection = new SocketFudgeConnection(getClientContext().getFudgeContext());
    boolean initialised = false;
    while (!initialised) {
      try {
        connection.setAddress(_resourceHost);
      } catch (UnknownHostException e) {
        s_logger.error("Unable to set host to " + _resourceHost, e);
        throw new OpenGammaRuntimeException("Unable to set host to " + _resourceHost, e);
      }
      connection.setPortNumber(_resourcePortNumber);
      connection.setFudgeMessageReceiver(this);
      connection.setConnectionStateListener(this);
  
      while (!connection.isRunning() && !isPoisoned()) {
        try {
          connection.start();
        } catch (Exception e) {
          s_logger.error("Failed to connect to " + _resourceHost + ":" + _resourcePortNumber + ". Waiting " + CONNECTION_RETRY_DELAY + " ms for retry.", e);
          try {
            Thread.sleep(CONNECTION_RETRY_DELAY);
          } catch (InterruptedException ie) {
            s_logger.warn("Interrupted while waiting to retry connection", ie);
            throw ie;
          }
        }
      }
      if (isPoisoned()) {
        throw new OpenGammaRuntimeException("Refusing to create connection as client as been poisoned");
      }
      try {
        // Fudge-based connection is established with an initial message. Currently we have nothing to send in this message.
        connection.getFudgeMessageSender().send(FudgeContext.EMPTY_MESSAGE);
        initialised = true;
      } catch (Exception e) {
        s_logger.error("Connection established but error occurred during initialisation", e);
      }
    }
    return connection;
  }
  
  private void disconnect() {
    if (_connection == null) {
      return;
    }
    synchronized (this) {
      if (_connection == null) {
        return;
      }
      _connection.stop();
      _connection = null;
    }
  }

  private void throwUsageException() {
    throw new OpenGammaRuntimeException("System setting " + DEBUG_CLIENT_CONNECTION_SETTING + " must be of the form HOST[:PORT]");
  }

}
