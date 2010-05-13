/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.socket;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.FudgeMsgReader;
import org.fudgemsg.FudgeMsgWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.util.ArgumentChecker;

/**
 * Opens a raw socket with a remote site for RPC-style communications.
 */
public class SocketFudgeRequestSender extends AbstractSocketProcess implements FudgeRequestSender {
  private static final Logger s_logger = LoggerFactory.getLogger(SocketFudgeRequestSender.class);
  private final FudgeContext _fudgeContext;
  
  private FudgeMsgWriter _msgWriter;
  private FudgeMsgReader _msgReader;
  
  public SocketFudgeRequestSender() {
    this(FudgeContext.GLOBAL_DEFAULT);
  }
  
  public SocketFudgeRequestSender(FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "Fudge context");
    _fudgeContext = fudgeContext;
  }

  @Override
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @Override
  public void sendRequest(FudgeFieldContainer request, FudgeMessageReceiver responseReceiver) {
    startIfNecessary();
    s_logger.debug("Dispatching request with {} fields", request.getNumFields());
    _msgWriter.writeMessage(request);
    FudgeMsgEnvelope response = _msgReader.nextMessageEnvelope();
    if(response != null) {
      s_logger.debug("Got response with {} fields", response.getMessage().getNumFields());
      responseReceiver.messageReceived(getFudgeContext(), response);
    }
  }

  @Override
  protected void socketOpened(Socket socket, OutputStream os, InputStream is) {
    _msgWriter = getFudgeContext().createMessageWriter(os);
    _msgWriter.setDefaultMessageProcessingDirectives(0);
    _msgWriter.setDefaultMessageVersion(0);
    _msgWriter.setDefaultTaxonomyId(0);
    
    _msgReader = getFudgeContext().createMessageReader(is);
  }

  @Override
  protected void socketClosed() {
    super.socketClosed();
    _msgWriter = null;
    _msgReader = null;
  }
  
  

}
