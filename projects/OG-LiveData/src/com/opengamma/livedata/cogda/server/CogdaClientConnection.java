/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.cogda.msg.CogdaCommandResponseResult;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataCommandResponseMessage;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSnapshotRequestBuilder;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSnapshotRequestMessage;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSnapshotResponseMessage;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSubscriptionRequestBuilder;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSubscriptionRequestMessage;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSubscriptionResponseMessage;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataUpdateBuilder;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataUpdateMessage;
import com.opengamma.livedata.cogda.msg.CogdaMessageType;
import com.opengamma.livedata.cogda.msg.ConnectionRequestBuilder;
import com.opengamma.livedata.cogda.msg.ConnectionRequestMessage;
import com.opengamma.livedata.cogda.msg.ConnectionResponseBuilder;
import com.opengamma.livedata.cogda.msg.ConnectionResponseMessage;
import com.opengamma.livedata.cogda.msg.ConnectionResult;
import com.opengamma.livedata.server.LastKnownValueStore;
import com.opengamma.transport.FudgeConnection;
import com.opengamma.transport.FudgeConnectionStateListener;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.util.ArgumentChecker;

/**
 * The object which holds a particular connection to a Cogda client for
 * a particular {@link CogdaLiveDataServer}.
 */
public class CogdaClientConnection implements FudgeConnectionStateListener, FudgeMessageReceiver {
  private final FudgeContext _fudgeContext;
  private final CogdaLiveDataServer _server;
  private final FudgeMessageSender _messageSender;
  
  private final ConcurrentMap<LiveDataSpecification, Boolean> _subscriptions = new ConcurrentHashMap<LiveDataSpecification, Boolean>();
  private final ConcurrentMap<LiveDataSpecification, FudgeMsg> _valuesToSend = new ConcurrentHashMap<LiveDataSpecification, FudgeMsg>();
  private final Lock _writerLock = new ReentrantLock();
  private final Lock _valuesToSendLock = new ReentrantLock();
  
  private UserPrincipal _user; 
  
  public CogdaClientConnection(FudgeContext fudgeContext, CogdaLiveDataServer server, FudgeConnection connection) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(server, "server");
    ArgumentChecker.notNull(connection, "fudgeConnection");
    
    _fudgeContext = fudgeContext;
    _server = server;
    connection.setConnectionStateListener(this);
    connection.setFudgeMessageReceiver(this);
    _messageSender = connection.getFudgeMessageSender();
  }

  /**
   * Gets the server.
   * @return the server
   */
  public CogdaLiveDataServer getServer() {
    return _server;
  }

  /**
   * Gets the fudgeContext.
   * @return the fudgeContext
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the messageSender.
   * @return the messageSender
   */
  public FudgeMessageSender getMessageSender() {
    return _messageSender;
  }

  /**
   * Gets the user.
   * @return the user
   */
  public UserPrincipal getUser() {
    return _user;
  }

  @Override
  public void connectionReset(FudgeConnection connection) {
  }

  @Override
  public void connectionFailed(FudgeConnection connection, Exception cause) {
  }
  
  public void handshakeMessage(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
    // REVIEW kirk 2012-07-23 -- If there are multiple versions of the protocol have to check
    // the schema on the envelope.
    FudgeMsg msg = msgEnvelope.getMessage();
    if (CogdaMessageType.getFromMessage(msg) != CogdaMessageType.CONNECTION_REQUEST) {
      // On failure tear down the connection when http://jira.opengamma.com/browse/PLAT-2458 is done.
      throw new OpenGammaRuntimeException("Cannot handle any other message than connection request as first message in COGDA protocol.");
    }
    ConnectionRequestMessage request = ConnectionRequestBuilder.buildObjectStatic(new FudgeDeserializer(fudgeContext), msg);
    
    // Wrap this in synchronized to force the cache flush.
    synchronized (this) {
      _user = getServer().authenticate(request.getUserName());
    }
    
    if (getUser() == null) {
      ConnectionResponseMessage response = new ConnectionResponseMessage();
      response.setResult(ConnectionResult.NOT_AUTHORIZED);
      sendMessage(ConnectionResponseBuilder.buildMessageStatic(new FudgeSerializer(fudgeContext), response));
      // On failure tear down the connection when http://jira.opengamma.com/browse/PLAT-2458 is done.
      getServer().removeClient(this);
    } else {
      ConnectionResponseMessage response = new ConnectionResponseMessage();
      response.setResult(ConnectionResult.NEW_CONNECTION_SUCCESS);
      response.setAvailableServers(getServer().getAvailableServers());
      response.applyCapabilities(getServer().getCapabilities());
      sendMessage(ConnectionResponseBuilder.buildMessageStatic(new FudgeSerializer(fudgeContext), response));
    }
  }

  @Override
  public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
    if (getUser() == null) {
      throw new OpenGammaRuntimeException("Cannot operate, failed user authentication.");
    }
    FudgeMsg msg = msgEnvelope.getMessage();
    
    CogdaLiveDataCommandResponseMessage response = null;
    switch (CogdaMessageType.getFromMessage(msg)) {
      case SNAPSHOT_REQUEST:
        response = handleSnapshotRequest(fudgeContext, msg);
        break;
      case SUBSCRIPTION_REQUEST:
        response = handleSubscriptionRequest(fudgeContext, msg);
        break;
      default:
        // Illegal here.
        // Need an "ILLEGAL_COMMAND" message.
        break;
    }
    
    // REVIEW kirk 2012-07-23 -- Does this work?
    sendMessage((new FudgeSerializer(fudgeContext)).objectToFudgeMsg(response));
  }

  /**
   * @param fudgeContext
   * @param msg
   */
  private CogdaLiveDataCommandResponseMessage handleSnapshotRequest(FudgeContext fudgeContext, FudgeMsg msg) {
    CogdaLiveDataSnapshotRequestMessage request = CogdaLiveDataSnapshotRequestBuilder.buildObjectStatic(new FudgeDeserializer(fudgeContext), msg);
    CogdaLiveDataSnapshotResponseMessage response = new CogdaLiveDataSnapshotResponseMessage();
    response.setCorrelationId(request.getCorrelationId());
    response.setSubscriptionId(request.getSubscriptionId());
    response.setNormalizationScheme(request.getNormalizationScheme());
    
    // TODO kirk 2012-07-23 -- Check entitlements.
    if (getServer().isValidLiveData(request.getSubscriptionId(), request.getNormalizationScheme())) {
      LastKnownValueStore lkvStore = getServer().getLastKnownValueStore(request.getSubscriptionId(), request.getNormalizationScheme());
      FudgeMsg fields = lkvStore.getFields();
      
      response.setGenericResult(CogdaCommandResponseResult.SUCCESSFUL);
      response.setValues(fields);
    } else {
      response.setGenericResult(CogdaCommandResponseResult.NOT_AVAILABLE);
    }
    
    return response;
  }

  /**
   * @param fudgeContext
   * @param msg
   */
  private CogdaLiveDataCommandResponseMessage handleSubscriptionRequest(FudgeContext fudgeContext, FudgeMsg msg) {
    CogdaLiveDataSubscriptionRequestMessage request = CogdaLiveDataSubscriptionRequestBuilder.buildObjectStatic(new FudgeDeserializer(fudgeContext), msg);
    CogdaLiveDataSubscriptionResponseMessage response = new CogdaLiveDataSubscriptionResponseMessage();
    response.setCorrelationId(request.getCorrelationId());
    response.setSubscriptionId(request.getSubscriptionId());
    response.setNormalizationScheme(request.getNormalizationScheme());
    
    // TODO kirk 2012-07-23 -- Check entitlements.
    if (getServer().isValidLiveData(request.getSubscriptionId(), request.getNormalizationScheme())) {
      LastKnownValueStore lkvStore = getServer().getLastKnownValueStore(request.getSubscriptionId(), request.getNormalizationScheme());
      FudgeMsg fields = lkvStore.getFields();
      
      response.setGenericResult(CogdaCommandResponseResult.SUCCESSFUL);
      response.setSnapshot(fields);
      
      _subscriptions.putIfAbsent(new LiveDataSpecification(request.getNormalizationScheme(), request.getSubscriptionId()), Boolean.TRUE);
    } else {
      response.setGenericResult(CogdaCommandResponseResult.NOT_AVAILABLE);
    }
    return response;
  }
  
  private void sendMessage(FudgeMsg msg) {
    _writerLock.lock();
    try {
      getMessageSender().send(msg);
    } finally {
      _writerLock.unlock();
    }
  }
  
  public boolean liveDataReceived(LiveDataValueUpdate valueUpdate) {
    if (!_subscriptions.containsKey(valueUpdate.getSpecification())) {
      return false;
    }
    _valuesToSendLock.lock();
    try {
      _valuesToSend.put(valueUpdate.getSpecification(), valueUpdate.getFields());
    } finally {
      _valuesToSendLock.unlock();
    }
    return true;
  }
  
  public void sendAllUpdates() {
    _writerLock.lock();
    try {
      _valuesToSendLock.lock();
      try {
        for (Map.Entry<LiveDataSpecification, FudgeMsg> entry : _valuesToSend.entrySet()) {
          sendValueUpdate(entry.getKey(), entry.getValue());
        }
        _valuesToSend.clear();
      } finally {
        _valuesToSendLock.unlock();
      }
    } finally {
      _writerLock.unlock();
    }
  }

  /**
   * @param key
   * @param values
   */
  private void sendValueUpdate(LiveDataSpecification key, FudgeMsg values) {
    CogdaLiveDataUpdateMessage message = new CogdaLiveDataUpdateMessage();
    // REVIEW kirk 2012-07-23 -- This is a terrible terrible idea performance wise, this next line.
    message.setSubscriptionId(key.getIdentifiers().getExternalIds().iterator().next());
    message.setNormalizationScheme(key.getNormalizationRuleSetId());
    message.setValues(values);
    FudgeMsg msg = CogdaLiveDataUpdateBuilder.buildMessageStatic(new FudgeSerializer(getFudgeContext()), message);
    getMessageSender().send(msg);
  }

}
