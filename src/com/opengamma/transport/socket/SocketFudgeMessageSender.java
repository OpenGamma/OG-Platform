/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;

// REVIEW kirk 2010-05-12 -- Enhancements here:
// - Keepalive on connection failing
// - Multiple remote endpoints

/**
 * Accepts messages and dispatches them to a remote {@link ServerSocketFudgeMessageReceiver}.
 * On lifecycle startup this class will open a remote connection to the other side,
 * and maintain that until {@link #stop()} is invoked to terminate the socket.
 *
 */
public class SocketFudgeMessageSender implements FudgeMessageSender, Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(SocketFudgeMessageSender.class);
  private final FudgeContext _fudgeContext;
  private InetAddress _inetAddress;
  private int _portNumber;
  
  private boolean _started = false;
  private Socket _socket;
  private FudgeMsgWriter _msgWriter;
  
  public SocketFudgeMessageSender() {
    this(FudgeContext.GLOBAL_DEFAULT);
  }
  
  public SocketFudgeMessageSender(FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "Fudge context");
    _fudgeContext = fudgeContext;
  }

  /**
   * @return the inetAddress
   */
  public InetAddress getInetAddress() {
    return _inetAddress;
  }

  /**
   * @param inetAddress the inetAddress to set
   */
  public void setInetAddress(InetAddress inetAddress) {
    _inetAddress = inetAddress;
  }

  /**
   * @return the portNumber
   */
  public int getPortNumber() {
    return _portNumber;
  }

  /**
   * @param portNumber the portNumber to set
   */
  public void setPortNumber(int portNumber) {
    _portNumber = portNumber;
  }

  @Override
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public synchronized void send(FudgeFieldContainer message) {
    if(!isRunning()) {
      s_logger.info("Manually calling start() as it wasn't called before first message sent.");
      start();
    }
    s_logger.info("Sending message with {} fields", message.getNumFields());
    _msgWriter.writeMessage(message);
  }

  @Override
  public synchronized boolean isRunning() {
    return _started;
  }

  @Override
  public synchronized void start() {
    ArgumentChecker.notNullInjected(getInetAddress(), "Remote InetAddress");
    Validate.isTrue(getPortNumber() > 0, "Must specify valid portNumber property");
    
    openRemoteConnection();
    
    _started = true;
  }
  
  protected synchronized void openRemoteConnection() {
    s_logger.info("Opening remote connection to {}:{}", getInetAddress(), getPortNumber());
    OutputStream os = null;
    try {
      _socket = new Socket(getInetAddress(), getPortNumber());
      os = _socket.getOutputStream();
    } catch (IOException ioe) {
      throw new OpenGammaRuntimeException("Unable to open remote connection to " + getInetAddress() +":" + getPortNumber(), ioe);
    }
    //os = new BufferedOutputStream(os);
    _msgWriter = getFudgeContext().createMessageWriter(os);
    // TODO kirk 2010-05-12 -- Make these injected parameters
    _msgWriter.setDefaultMessageProcessingDirectives(0);
    _msgWriter.setDefaultMessageVersion(0);
    _msgWriter.setDefaultTaxonomyId(0);
  }

  @Override
  public synchronized void stop() {
    if(_socket.isConnected()) {
      try {
        _socket.close();
      } catch (IOException e) {
        s_logger.warn("Unable to close connected socket to {}", new Object[]{_socket.getRemoteSocketAddress()}, e);
      }
    }
    
    _socket = null;
    _msgWriter = null;
    _started = false;
  }

}
