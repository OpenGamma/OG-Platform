/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.wire.FudgeMsgReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.ExternalId;
import com.opengamma.livedata.LiveDataListener;
import com.opengamma.livedata.LiveDataSpecification;
import com.opengamma.livedata.LiveDataValueUpdate;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSnapshotRequestBuilder;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSnapshotRequestMessage;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSnapshotResponseBuilder;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSnapshotResponseMessage;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSubscriptionRequestBuilder;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSubscriptionRequestMessage;
import com.opengamma.livedata.cogda.msg.CogdaLiveDataSubscriptionResponseBuilder;
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
import com.opengamma.livedata.cogda.server.CogdaLiveDataServer;
import com.opengamma.livedata.msg.LiveDataSubscriptionResponse;
import com.opengamma.livedata.msg.LiveDataSubscriptionResult;
import com.opengamma.transport.ByteArrayFudgeMessageSender;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageSender;
import com.opengamma.transport.InputStreamFudgeMessageDispatcher;
import com.opengamma.transport.OutputStreamByteArrayMessageSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * Live data client connecting to a COGDA server.
 * <p>
 * This connects to an instance of {@link CogdaLiveDataServer}.
 */
public class CogdaLiveDataClient extends AbstractLiveDataClient implements Lifecycle, FudgeMessageReceiver {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(CogdaLiveDataClient.class);

  // Injected parameters:
  /**
   * The server name to connect to.
   */
  private String _serverName = "127.0.0.1";
  /**
   * The server port to connect to.
   */
  private int _serverPort = CogdaLiveDataServer.DEFAULT_LISTEN_PORT;
  /**
   * The Fudge context.
   */
  private FudgeContext _fudgeContext = OpenGammaFudgeContext.getInstance();
  /**
   * The user.
   */
  private final UserPrincipal _user;

  // Runtime state:
  /**
   * Holds the actual socket to the server.
   */
  private Socket _socket;
  /**
   * The message sender.
   */
  private FudgeMessageSender _messageSender;
  /**
   * The socket thread.
   */
  @SuppressWarnings("unused")
  private Thread _socketReadThread;
  /**
   * The generator of correlation identifiers.
   */
  private final AtomicLong _nextRequestId = new AtomicLong(1L);
  /**
   * The active subscription requests.
   */
  private final Map<Long, SubscriptionHandle> _activeSubscriptionRequests = new ConcurrentHashMap<Long, SubscriptionHandle>();

  /**
   * Creates an instance.
   * 
   * @param user  the user to connect with, not null
   */
  public CogdaLiveDataClient(UserPrincipal user) {
    ArgumentChecker.notNull(user, "user");
    _user = user;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the server name.
   * 
   * @return the server name, not null
   */
  public String getServerName() {
    return _serverName;
  }

  /**
   * Sets the server name.
   * 
   * @param serverName  the server name, not null
   */
  public void setServerName(String serverName) {
    _serverName = serverName;
  }

  /**
   * Gets the server port.
   * 
   * @return the server port
   */
  public int getServerPort() {
    return _serverPort;
  }

  /**
   * Sets the server port.
   * 
   * @param serverPort  the server port
   */
  public void setServerPort(int serverPort) {
    _serverPort = serverPort;
  }

  /**
   * Gets the fudge context.
   * 
   * @return the fudge context, not null
   */
  @Override
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Sets the fudge context.
   * 
   * @param fudgeContext  the fudge context, not null
   */
  @Override
  public void setFudgeContext(FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Checks whether the specified user matches the user this client is for.
   * 
   * @param user  the user to check, not null
   * @throws IllegalArgumentException if the user is invalid
   */
  protected void checkUserMatches(UserPrincipal user) {
    if (!ObjectUtils.equals(user, _user)) {
      throw new IllegalArgumentException("Specified user " + user + " does not match client user " + _user);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isEntitled(UserPrincipal user, LiveDataSpecification requestedSpecification) {
    // TODO kirk 2012-08-23 -- Implement this properly.
    return true;
  }

  @Override
  public Map<LiveDataSpecification, Boolean> isEntitled(UserPrincipal user, Collection<LiveDataSpecification> requestedSpecifications) {
    Map<LiveDataSpecification, Boolean> result = new HashMap<LiveDataSpecification, Boolean>();
    for (LiveDataSpecification ldc : requestedSpecifications) {
      result.put(ldc, isEntitled(user, ldc));
    }
    return result;
  }

  @Override
  protected void handleSubscriptionRequest(Collection<SubscriptionHandle> subHandle) {
    // TODO kirk 2012-08-15 -- Batch these up. This is just for testing.
    for (SubscriptionHandle handle : subHandle) {
      long correlationId = _nextRequestId.getAndIncrement();
      switch (handle.getSubscriptionType()) {
        case NON_PERSISTENT:
        case PERSISTENT:
          CogdaLiveDataSubscriptionRequestMessage subRequest = new CogdaLiveDataSubscriptionRequestMessage();
          subRequest.setCorrelationId(correlationId);
          subRequest.setNormalizationScheme(handle.getRequestedSpecification().getNormalizationRuleSetId());
          // REVIEW kirk 2012-08-15 -- The next line is SOOOOO UGLLYYYYY!!!!!
          subRequest.setSubscriptionId(handle.getRequestedSpecification().getIdentifiers().getExternalIds().iterator().next());
          _activeSubscriptionRequests.put(correlationId, handle);
          _messageSender.send(CogdaLiveDataSubscriptionRequestBuilder.buildMessageStatic(new FudgeSerializer(getFudgeContext()), subRequest));
          // Same thing in Cogda.
          break;
        case SNAPSHOT:
          CogdaLiveDataSnapshotRequestMessage snapshotRequest = new CogdaLiveDataSnapshotRequestMessage();
          snapshotRequest.setCorrelationId(correlationId);
          snapshotRequest.setNormalizationScheme(handle.getRequestedSpecification().getNormalizationRuleSetId());
          // REVIEW kirk 2012-08-15 -- The next line is SOOOOO UGLLYYYYY!!!!!
          snapshotRequest.setSubscriptionId(handle.getRequestedSpecification().getIdentifiers().getExternalIds().iterator().next());
          _activeSubscriptionRequests.put(correlationId, handle);
          _messageSender.send(CogdaLiveDataSnapshotRequestBuilder.buildMessageStatic(new FudgeSerializer(getFudgeContext()), snapshotRequest));
          break;
      }
    }
  }

  @Override
  protected void cancelPublication(LiveDataSpecification fullyQualifiedSpecification) {
    CogdaLiveDataUnsubscribeMessage message = new CogdaLiveDataUnsubscribeMessage();
    message.setCorrelationId(_nextRequestId.getAndIncrement());
    message.setNormalizationScheme(fullyQualifiedSpecification.getNormalizationRuleSetId());
    message.setSubscriptionId(fullyQualifiedSpecification.getIdentifiers().iterator().next());
    _messageSender.send(CogdaLiveDataUnsubscribeBuilder.buildMessageStatic(new FudgeSerializer(getFudgeContext()), message));
  }

  @Override
  public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
    s_logger.info("Got message {}", msgEnvelope);
    FudgeMsg msg = msgEnvelope.getMessage();
    CogdaMessageType msgType = CogdaMessageType.getFromMessage(msg);
    switch (msgType) {
      case SUBSCRIPTION_RESPONSE:
      case SNAPSHOT_RESPONSE:
        dispatchCommandResponse(msgType, msg);
        break;
      case LIVE_DATA_UPDATE:
        dispatchLiveDataUpdate(msg);
        break;
      default:
        s_logger.warn("Received message that wasn't understood: {}", msg);
    }
  }

  /**
   * Dispatches a message to the server.
   * 
   * @param msg  the message, not null
   */
  private void dispatchLiveDataUpdate(FudgeMsg msg) {
    CogdaLiveDataUpdateMessage updateMessage = CogdaLiveDataUpdateBuilder.buildObjectStatic(new FudgeDeserializer(getFudgeContext()), msg);
    LiveDataSpecification ldspec = new LiveDataSpecification(updateMessage.getNormalizationScheme(), updateMessage.getSubscriptionId());
    LiveDataValueUpdateBean valueUpdateBean = new LiveDataValueUpdateBean(0L, ldspec, updateMessage.getValues());
    super.valueUpdate(valueUpdateBean);
  }

  /**
   * Dispatches a command response.
   * 
   * @param msgType  the type, not null
   * @param msg  the message, not null
   */
  private void dispatchCommandResponse(CogdaMessageType msgType, FudgeMsg msg) {
    if (!msg.hasField("correlationId")) {
      s_logger.warn("Received subscription response message without correlationId: {}", msg);
      return;
    }
    long correlationId = msg.getLong("correlationId");
    
    SubscriptionHandle subHandle = _activeSubscriptionRequests.remove(correlationId);
    if (subHandle == null) {
      s_logger.warn("Got subscription result on correlationId {} without active subscription: {}", correlationId, msg);
      return;
    }
    
    switch (msgType) {
      case SUBSCRIPTION_RESPONSE:
        dispatchSubscriptionResponse(msg, subHandle);
        break;
      case SNAPSHOT_RESPONSE:
        dispatchSnapshotResponse(msg, subHandle);
        break;
      default:
        s_logger.warn("Got unexpected msg type {} as a command response - {}", msgType, msg);
        break;
    }
  }

  /**
   * Dispatches the response to a snapshot.
   * 
   * @param msg  the message, not null
   * @param subHandle  the subscription handle, not null
   */
  private void dispatchSnapshotResponse(FudgeMsg msg, SubscriptionHandle subHandle) {
    CogdaLiveDataSnapshotResponseMessage responseMessage = CogdaLiveDataSnapshotResponseBuilder.buildObjectStatic(new FudgeDeserializer(getFudgeContext()), msg);
    LiveDataSpecification ldSpec = new LiveDataSpecification(responseMessage.getNormalizationScheme(), responseMessage.getSubscriptionId());
    
    LiveDataSubscriptionResult ldsResult = responseMessage.getGenericResult().toLiveDataSubscriptionResult();
    LiveDataSubscriptionResponse ldsResponse = new LiveDataSubscriptionResponse(subHandle.getRequestedSpecification(), ldsResult);
    ldsResponse.setFullyQualifiedSpecification(ldSpec);
    ldsResponse.setUserMessage(responseMessage.getUserMessage());
    
    LiveDataValueUpdateBean valueUpdateBean = new LiveDataValueUpdateBean(0L, subHandle.getRequestedSpecification(), responseMessage.getValues());
    ldsResponse.setSnapshot(valueUpdateBean);
    subHandle.subscriptionResultReceived(ldsResponse);
  }

  /**
   * Dispatches the response to subscription.
   * 
   * @param msg  the message, not null
   * @param subHandle  the subscription handle, not null
   */
  private void dispatchSubscriptionResponse(FudgeMsg msg, SubscriptionHandle subHandle) {
    CogdaLiveDataSubscriptionResponseMessage responseMessage = CogdaLiveDataSubscriptionResponseBuilder.buildObjectStatic(new FudgeDeserializer(getFudgeContext()), msg);
    LiveDataSpecification ldSpec = new LiveDataSpecification(responseMessage.getNormalizationScheme(), responseMessage.getSubscriptionId());
    
    LiveDataSubscriptionResult ldsResult = responseMessage.getGenericResult().toLiveDataSubscriptionResult();
    LiveDataSubscriptionResponse ldsResponse = new LiveDataSubscriptionResponse(subHandle.getRequestedSpecification(), ldsResult);
    ldsResponse.setFullyQualifiedSpecification(ldSpec);
    ldsResponse.setUserMessage(responseMessage.getUserMessage());
    
    LiveDataValueUpdateBean valueUpdateBean = new LiveDataValueUpdateBean(0L, subHandle.getRequestedSpecification(), responseMessage.getSnapshot());
    ldsResponse.setSnapshot(valueUpdateBean);
    
    switch (responseMessage.getGenericResult()) {
      case SUCCESSFUL:
        super.subscriptionRequestSatisfied(subHandle, ldsResponse);
        super.subscriptionStartingToReceiveTicks(subHandle, ldsResponse);
        break;
      default:
        super.subscriptionRequestFailed(subHandle, ldsResponse);
    }
    subHandle.subscriptionResultReceived(ldsResponse);
    subHandle.getListener().valueUpdate(valueUpdateBean);
  }

  //-------------------------------------------------------------------------
  @Override
  public void start() {
    if (_socket != null) {
      throw new IllegalStateException("Socket is currently established.");
    }
    InetAddress serverAddress = null;
    try {
      serverAddress = InetAddress.getByName(getServerName());
    } catch (UnknownHostException ex) {
      s_logger.error("Illegal host name: " + getServerName(), ex);
      throw new IllegalArgumentException("Cannot identify host " + getServerName());
    }
    try {
      Socket socket = new Socket(serverAddress, getServerPort());
      InputStream is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      _messageSender = new ByteArrayFudgeMessageSender(new OutputStreamByteArrayMessageSender(os));
      
      login(is);
      
      InputStreamFudgeMessageDispatcher messageDispatcher = new InputStreamFudgeMessageDispatcher(is, this);
      Thread t = new Thread(messageDispatcher, "CogdaLiveDataClient Dispatch Thread");
      t.setDaemon(true);
      t.start();
      _socketReadThread = t;
      
      _socket = socket;
    } catch (IOException ioe) {
      s_logger.error("Unable to establish connection to" + getServerName() + ":" + getServerPort(), ioe);
      throw new OpenGammaRuntimeException("Unable to establish connection to" + getServerName() + ":" + getServerPort());
    }
    
  }

  protected void login(InputStream is) throws IOException {
    ConnectionRequestMessage requestMessage = new ConnectionRequestMessage();
    requestMessage.setUserName(_user.getUserName());
    _messageSender.send(ConnectionRequestBuilder.buildMessageStatic(new FudgeSerializer(getFudgeContext()), requestMessage));
    // TODO kirk 2012-08-22 -- This needs a timeout.
    FudgeMsgReader reader = getFudgeContext().createMessageReader(is);
    FudgeMsg msg = reader.nextMessage();
    ConnectionResponseMessage response = ConnectionResponseBuilder.buildObjectStatic(new FudgeDeserializer(getFudgeContext()), msg);
    switch(response.getResult()) {
      case NEW_CONNECTION_SUCCESS:
      case EXISTING_CONNECTION_RESTART:
        // We're good to go!
        // TODO kirk 2012-08-15 -- Add logic eventually for connection restart semantics.
        s_logger.warn("Successfully logged into server.");
        break;
      case NOT_AUTHORIZED:
        // REVIEW kirk 2012-08-15 -- Is this the right error?
        throw new OpenGammaRuntimeException("Server says NOT_AUTHORIZED");
    }
  }

  @Override
  public void stop() {
  }

  @Override
  public boolean isRunning() {
    return ((_socket != null) && (_socket.isConnected()));
  }

  //-------------------------------------------------------------------------
  /**
   * A simple test that runs against localhost. Only useful for protocol development.
   * 
   * @param args Command-line args. Ignored.
   * @throws InterruptedException Required to make the compiler happy
   */
  public static void main(final String[] args) throws InterruptedException { // CSIGNORE
    CogdaLiveDataClient client = new CogdaLiveDataClient(UserPrincipal.getLocalUser());
    //client.setServerName("cogdasvr-lx-1.hq.opengamma.com");
    client.start();
    
    LiveDataSpecification lds = new LiveDataSpecification("OpenGamma", ExternalId.of("SURF", "FV2DBEURUSD12M"));
    LiveDataSubscriptionResponse response = client.snapshot(UserPrincipal.getLocalUser(), lds, 60000L);
    s_logger.warn("Snapshot {}", response);
    List<LiveDataSpecification> subs = new LinkedList<LiveDataSpecification>();
    subs.add(lds);
    subs.add(new LiveDataSpecification("OpenGamma", ExternalId.of("SURF", "ASIRSEUR49Y30A03L")));
    subs.add(new LiveDataSpecification("OpenGamma", ExternalId.of("SURF", "FV1DRUSDBRL06M")));
    subs.add(new LiveDataSpecification("OpenGamma", ExternalId.of("ICAP", "SAUD_9Y")));
    subs.add(new LiveDataSpecification("OpenGamma", ExternalId.of("ICAP", "GBP_5Y")));
    subs.add(new LiveDataSpecification("OpenGamma", ExternalId.of("ICAP", "GBPUSD7M")));
    LiveDataListener ldl = new LiveDataListener() {
      @Override
      public void subscriptionResultReceived(LiveDataSubscriptionResponse subscriptionResult) {
        s_logger.warn("Sub result {}", subscriptionResult);
      }

      @Override
      public void subscriptionResultsReceived(final Collection<LiveDataSubscriptionResponse> subscriptionResults) {
        s_logger.warn("Sub result {}", subscriptionResults);
      }

      @Override
      public void subscriptionStopped(LiveDataSpecification fullyQualifiedSpecification) {
        s_logger.warn("Sub stopped {}", fullyQualifiedSpecification);
      }

      @Override
      public void valueUpdate(LiveDataValueUpdate valueUpdate) {
        s_logger.warn("Data received {}", valueUpdate);
      }
      
    }; 
    client.subscribe(UserPrincipal.getLocalUser(), subs, ldl);
    
    client.subscribe(UserPrincipal.getLocalUser(), new LiveDataSpecification("OpenGamma", ExternalId.of("SURF", "NO_SUCH_THING")), ldl);
    
    Thread.sleep(100000000L);
  }

}
