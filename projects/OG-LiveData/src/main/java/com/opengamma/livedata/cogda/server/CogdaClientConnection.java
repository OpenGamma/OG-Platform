/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.cogda.server;

import java.util.HashMap;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.user.UserAccount;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.cogda.msg.CogdaCommandResponseResult;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataBuilderUtil;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataCommandResponseMessage;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSnapshotRequestBuilder;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSnapshotRequestMessage;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSnapshotResponseMessage;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSubscriptionRequestBuilder;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSubscriptionRequestMessage;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSubscriptionResponseMessage;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataUnsubscribeBuilder;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataUnsubscribeMessage;
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

  /**
   * Subscribe to a stream
   */
  public static final String SUBSCRIBE = "subscribe";
  /**
   * Snapshot the state of the world
   */
  public static final String SNAPSHOT = "snapshot";

  private static final Logger s_logger = LoggerFactory.getLogger(CogdaClientConnection.class);
  private final FudgeContext _fudgeContext;
  private final CogdaLiveDataServer _server;
  private final FudgeMessageSender _messageSender;
  
  // REVIEW kirk 2013-03-27 -- The only reason why _subscriptions exists is to act as
  // a quick pass on whether the client is subscribed to a specification.
  // This is to avoid going into a locking state waiting for _valuesToSend. 
  private final ConcurrentMap<LiveDataSpecification, Boolean> _subscriptions = new ConcurrentHashMap<LiveDataSpecification, Boolean>();
  private final Map<LiveDataSpecification, FudgeMsg> _valuesToSend = new HashMap<LiveDataSpecification, FudgeMsg>();
  private final Lock _writerLock = new ReentrantLock();
  private final Lock _valuesToSendLock = new ReentrantLock();
  
  private UserPrincipal _userPrincipal;
  private UserAccount _user;
  
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
  public UserPrincipal getUserPrincipal() {
    return _userPrincipal;
  }

  /**
   * Gets the user.
   * @return the user
   */
  public UserAccount getUser() {
    return _user;
  }

  /**
   * Sets the user.
   * @param user  the user
   */
  public void setUser(UserAccount user) {
    _user = user;
  }

  @Override
  public void connectionReset(FudgeConnection connection) {
    s_logger.warn("Connection Reset");
  }

  @Override
  public void connectionFailed(FudgeConnection connection, Exception cause) {
    // TODO kirk 2012-08-15 -- Fix this so that failed connections result in
    // torn down client connections.
    // Cause may be null.
    s_logger.warn("Connection failed \"{}\"", (cause != null) ? cause.getMessage() : "no cause");
    s_logger.info("Connection failed", cause);
    getServer().removeClient(this);
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
      _userPrincipal = getServer().authenticate(request.getUserName(), request.getPassword());
      if (_userPrincipal != null) {
        _user = getServer().getUserAccount(request.getUserName());
      }
    }
    
    if (getUserPrincipal() == null) {
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
    if (getUserPrincipal() == null) {
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
      case UNSUBSCRIBE:
        handleUnsubscription(fudgeContext, msg);
        break;
      default:
        // Illegal here.
        // Need an "ILLEGAL_COMMAND" message.
        break;
    }
    
    if (response != null) {
      sendMessage(CogdaLiveDataBuilderUtil.buildCommandResponseMessage(fudgeContext, response));
    }
  }
  
  protected boolean isEntitled(String operation, ExternalId subscriptionId, String normalizationScheme) {
    return true;  // TODO: check permissions against user
//    String entitlementDetail = MessageFormat.format("/{0}/{1}[{2}]", subscriptionId.getScheme(), subscriptionId.getValue(), normalizationScheme);
//    String entitlementString = EntitlementUtils.generateEntitlementString(true, operation, "cogda", entitlementDetail);
//    return EntitlementUtils.userHasEntitlement(getUser(), entitlementString);
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
    
    if (!getServer().isValidLiveData(request.getSubscriptionId(), request.getNormalizationScheme())) {
      response.setGenericResult(CogdaCommandResponseResult.NOT_AVAILABLE);
    } else if (!isEntitled(SNAPSHOT, request.getSubscriptionId(), request.getNormalizationScheme())) {
      response.setGenericResult(CogdaCommandResponseResult.NOT_AUTHORIZED);
    } else {
      LastKnownValueStore lkvStore = getServer().getLastKnownValueStore(request.getSubscriptionId(), request.getNormalizationScheme());
      FudgeMsg fields = null;
      if (lkvStore != null) {
        fields = lkvStore.getFields();
      } else {
        s_logger.warn("Valid live data {} lacks fields in LKV store", request);
        fields = fudgeContext.newMessage();
      }
      
      response.setGenericResult(CogdaCommandResponseResult.SUCCESSFUL);
      response.setValues(fields);
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
    if (!getServer().isValidLiveData(request.getSubscriptionId(), request.getNormalizationScheme())) {
      response.setGenericResult(CogdaCommandResponseResult.NOT_AVAILABLE);
    } else if (!isEntitled(SUBSCRIBE, request.getSubscriptionId(), request.getNormalizationScheme())) {
      response.setGenericResult(CogdaCommandResponseResult.NOT_AUTHORIZED);
    } else {
      LastKnownValueStore lkvStore = getServer().getLastKnownValueStore(request.getSubscriptionId(), request.getNormalizationScheme());
      FudgeMsg fields = null;
      if (lkvStore != null) {
        fields = lkvStore.getFields();
      } else {
        s_logger.warn("Valid live data {} lacks fields in LKV store", request);
        fields = fudgeContext.newMessage();
      }
      
      response.setGenericResult(CogdaCommandResponseResult.SUCCESSFUL);
      response.setSnapshot(fields);
      
      _subscriptions.putIfAbsent(new LiveDataSpecification(request.getNormalizationScheme(), request.getSubscriptionId()), Boolean.TRUE);
    }
    return response;
  }
  
  private void handleUnsubscription(FudgeContext fudgeContext, FudgeMsg msg) {
    CogdaLiveDataUnsubscribeMessage request = CogdaLiveDataUnsubscribeBuilder.buildObjectStatic(new FudgeDeserializer(fudgeContext), msg);
    
    _subscriptions.remove(new LiveDataSpecification(request.getNormalizationScheme(), request.getSubscriptionId()));
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
    try {
      getMessageSender().send(msg);
    } catch (Exception e) {
      s_logger.info("Exception thrown; assuming socket closed and tearing down client.");
      // Note that the actual connection state will be handled by the FudgeConnectionStateListener callback.
    }
  }

}
