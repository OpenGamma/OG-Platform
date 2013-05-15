/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.language.connector.debug;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.language.connector.ClientContext;
import com.opengamma.language.connector.ClientFactory;
import com.opengamma.language.connector.LanguageSpringContext;
import com.opengamma.language.context.SessionContext;
import com.opengamma.language.context.SessionContextFactory;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionReceiver;
import com.opengamma.transport.FudgeConnectionStateListener;
import com.opengamma.transport.socket.ServerSocketFudgeConnectionReceiver;
import com.opengamma.util.tuple.Pair;

/**
 * An entry point which starts the Language stack in debug mode, allowing it to serve arbitrary, possibly remote, processes.
 */
public class DebugService {

  private static final Logger s_logger = LoggerFactory.getLogger(DebugService.class);
  
  private static LanguageSpringContext s_springContext;
  private static ClientContext s_clientContext;
  private static SessionContextFactory s_sessionContextFactory;
  
  /**
   * The default listener port
   */
  /*package*/ static final int DEFAULT_PORT = 9090;
  
  private static ServerSocketFudgeConnectionReceiver s_serverSocketConnectionReceiver;
  
  //CSOFF: main method
  public static void main(String[] args) {
    if (args.length < 1 || args.length > 2) {
      usage();
      return;
    }

    s_springContext = new LanguageSpringContext();
    String languageId = args[0];
    Pair<ClientFactory, SessionContextFactory> languageFactories = s_springContext.getLanguageFactories(languageId);
    s_clientContext = languageFactories.getFirst().getClientContext();
    s_sessionContextFactory = languageFactories.getSecond();
    
    int port = args.length == 2 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
    s_serverSocketConnectionReceiver = new ServerSocketFudgeConnectionReceiver(s_clientContext.getFudgeContext(), new DebugConnectionReceiver());
    s_serverSocketConnectionReceiver.setPortNumber(port);
    s_serverSocketConnectionReceiver.setDaemon(false);
    s_serverSocketConnectionReceiver.start();
    
    System.out.println("================================== READY TO SERVE " + languageId.toUpperCase() + " =======================================");
  }
  // CSON: main method

  private static void usage() {
    System.err.println("Starts the Language stack in debug mode, allowing it to serve arbitrary, possibly remote, processes.");
    System.err.println();
    System.err.println("Usage: " + DebugService.class.getName() + " LANGUAGE_ID [PORT]");
    System.err.println("  LANGUAGE_ID is the language ID defining the contexts to use");
    System.err.println("  PORT is the port number on which to listen, defaults to 9090");
  }
  
  private static class DebugConnectionReceiver implements FudgeConnectionReceiver, FudgeConnectionStateListener  {

    private final Map<FudgeConnection, DebugClientResource> _connections = new ConcurrentHashMap<FudgeConnection, DebugClientResource>();
    
    @Override
    public void connectionReceived(FudgeContext fudgeContext, FudgeMsgEnvelope env, FudgeConnection connection) {
      s_logger.info("Connection received: {}", connection);
      SessionContext sessionContext = s_sessionContextFactory.createSessionContext(System.getProperty("user.name"), true);
      DebugClientResource clientConnection = new DebugClientResource(connection, s_clientContext, sessionContext);
      _connections.put(connection, clientConnection);
      connection.setFudgeMessageReceiver(clientConnection);
      connection.setConnectionStateListener(DebugConnectionReceiver.this);
    }

    @Override
    public void connectionReset(FudgeConnection connection) {
      s_logger.info("Connection reset: " + connection);
    }

    @Override
    public void connectionFailed(FudgeConnection connection, Exception cause) {
      _connections.remove(connection);
      s_logger.warn("Connection terminated: " + connection + ". " + _connections.size() + " connections remaining.", cause);
    }
    
  }

}
