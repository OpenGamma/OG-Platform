/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.springframework.context.Lifecycle;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.EventQueue;
import com.bloomberglp.blpapi.Identity;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.ConnectionUnavailableException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;

/**
 * Abstract data provider for connecting to Bloomberg.
 */
public abstract class AbstractBloombergStaticDataProvider implements Lifecycle {

  private static final int WAIT_TIME_MS = 10 * 1000; // 10 seconds
  private static final Name TOKEN_SUCCESS = Name.getName("TokenGenerationSuccess");
  private static final Name TOKEN_ELEMENT = Name.getName("token");
  private static final Name AUTHORIZATION_SUCCESSS = Name.getName("AuthorizationSuccess");

  private static final String AUTH_APP_PREFIX = "AuthenticationMode=APPLICATION_ONLY;ApplicationAuthenticationType=APPNAME_AND_KEY;ApplicationName=";

  /**
   * Default shedule time in hours to re authorize bpipe user with Bloomberg
   */
  public static final int RE_AUTHORIZATION_SCHEDULE_TIME = 24;

  /**
   * The Bloomberg session options.
   */
  private final BloombergConnector _bloombergConnector;

  /**
   * The provider of correlation identifiers.
   */
  private final AtomicLong _nextCorrelationId = new AtomicLong(1L);

  /**
   * Result futures
   */
  private final Map<CorrelationID, SettableFuture<List<Element>>> _responseFutures = new ConcurrentHashMap<>();
  /**
   * Actual results
   */
  private final Map<CorrelationID, List<Element>> _responseMessages = new ConcurrentHashMap<>();
  /**
   * The event processor listening to Bloomberg.
   */
  private BloombergSessionEventProcessor _eventProcessor;
  /**
   * The thread hosting the event processor.
   */
  private Thread _thread;

  /**
   * Manages the Bloomberg session and service.
   */
  private final SessionProvider _sessionProvider;

  /**
   * The application name.
   */
  private final String _applicationName;

  /**
   * The service name
   */
  private final String _serviceName;

  /**
   * The bpipe applicatiion user identity.
   */
  private volatile Identity _applicationIdentity;

  /**
   * The identity re authorization schedule time in hours
   * <p>
   * Defaults to 24hrs if not set
   */

  private final int _reAuthorizationScheduleTime;

  /**
   * Scheduler to check against system identity every 24hrs
   */
  private ScheduledExecutorService _identityScheduler;
  private ScheduledFuture<?> _identityCheckTask;

  /**
   * Creates an instance.
   * 
   * @param bloombergConnector the Bloomberg connector, not null
   * @param serviceName The Bloomberg service to start
   * @param applicationName the bpipe application if applicable
   * @param reAuthorizationScheduleTime the identity re authorization schedule time in hours
   */
  public AbstractBloombergStaticDataProvider(BloombergConnector bloombergConnector, String serviceName, String applicationName, double reAuthorizationScheduleTime) {
    ArgumentChecker.notNull(bloombergConnector, "bloombergConnector");
    ArgumentChecker.notEmpty(serviceName, "serviceName");
    ArgumentChecker.isTrue(reAuthorizationScheduleTime > 0, "validation schedule time must be positive number");

    final List<String> serviceNames = Lists.newArrayList(serviceName);
    if (applicationName != null) {
      applicationName = StringUtils.trimToNull(applicationName);
      ArgumentChecker.notNull(applicationName, "applicationName");
      bloombergConnector.getSessionOptions().setAuthenticationOptions(AUTH_APP_PREFIX + applicationName);
      serviceNames.add(BloombergConstants.AUTH_SVC_NAME);
    }

    _bloombergConnector = bloombergConnector;
    _sessionProvider = new SessionProvider(_bloombergConnector, serviceNames);
    _reAuthorizationScheduleTime = (int) Math.round(3600 * reAuthorizationScheduleTime);
    _applicationName = applicationName;
    _serviceName = serviceName;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Bloomberg session options.
   * 
   * @return the session options
   */
  public BloombergConnector getBloombergConnector() {
    return _bloombergConnector;
  }

  /**
   * Gets the applicationName.
   * @return the applicationName
   */
  public String getApplicationName() {
    return _applicationName;
  }

  /**
   * Gets the active logger.
   * 
   * @return the logger.
   */
  protected abstract Logger getLogger();

  //-------------------------------------------------------------------------
  /**
   * Gets the Bloomberg session.
   * 
   * @return the session
   * @throws OpenGammaRuntimeException If no connection to Bloomberg is available
   */
  protected Session getSession() {
    return _sessionProvider.getSession();
  }

  /**
   * @return The Bloomberg service.
   * @throws OpenGammaRuntimeException If no connection to Bloomberg is available
   */
  protected Service getService() {
    return _sessionProvider.getService(_serviceName);
  }

  private synchronized void releaseBlockedRequests() {
    for (Entry<CorrelationID, SettableFuture<List<Element>>> entry : _responseFutures.entrySet()) {
      List<Element> messages = _responseMessages.remove(entry.getKey());
      if (messages == null) {
        messages = new ArrayList<>();
      }
      entry.getValue().set(messages);
    }
    _responseFutures.clear();
    _responseMessages.clear();
  }

  /**
   * Shuts down the Bloomberg session and service, releasing any resources.
   */
  private void invalidateSession() {
    _sessionProvider.invalidateSession();
    releaseBlockedRequests();
  }

  //-------------------------------------------------------------------------
  /**
   * Sends a request to Bloomberg, waiting for the response.
   * 
   * @param request the request to send, not null
   * @return the correlation identifier, not null
   */
  protected Future<List<Element>> submitRequest(Request request) {
    Session session = getSession();
    CorrelationID cid = new CorrelationID(generateCorrelationID());
    
    SettableFuture<List<Element>> resultFuture = SettableFuture.<List<Element>>create();
    ArrayList<Element> result = new ArrayList<>();
    try {
      if (_applicationIdentity != null) {
        getLogger().debug("submitting authorized request {} with cid {}", request, cid);
        session.sendRequest(request, _applicationIdentity, cid);
      } else {
        getLogger().debug("submitting normal request {} with cid {}", request, cid);
        session.sendRequest(request, cid);
      }
      _responseMessages.put(cid, result);
      _responseFutures.put(cid, resultFuture);
    } catch (IOException ex) {
      getLogger().warn("Error executing bloomberg reference data request", ex);
      resultFuture.set(result);
    }
    return resultFuture;
  }

  /**
   * Sends an authorization request to Bloomberg, waiting for the response.
   * 
   * @param request the request to send, not null
   * @param userIdentity the user identity, not null
   * @return the collection of results, not null
   */
  protected Future<List<Element>> submitAuthorizationRequest(Request request, Identity userIdentity) {
    getLogger().debug("Sending Request={}", request);
    Session session = getSession();
    CorrelationID cid = new CorrelationID(generateCorrelationID());

    SettableFuture<List<Element>> resultFuture = SettableFuture.<List<Element>>create();
    ArrayList<Element> result = new ArrayList<>();
    try {
      session.sendAuthorizationRequest(request, userIdentity, cid);
      _responseMessages.put(cid, result);
      _responseFutures.put(cid, resultFuture);
    } catch (IOException ex) {
      getLogger().warn("Error executing bloomberg reference data request", ex);
      resultFuture.set(result);
    }
    return resultFuture;
  }

  /**
   * Generates a correlation identifier.
   * 
   * @return the correlation identifier, not null
   */
  protected long generateCorrelationID() {
    return _nextCorrelationId.getAndIncrement();
  }

  //-------------------------------------------------------------------------
  /**
   * Starts the Bloomberg service.
   */
  @Override
  public synchronized void start() {
    if (isRunning()) {
      getLogger().info("Bloomberg already started");
      return;
    }
    getLogger().info("Bloomberg event processor being started...");
    _sessionProvider.start();
    _eventProcessor = new BloombergSessionEventProcessor();
    _thread = new Thread(_eventProcessor, "BSM Event Processor");
    _thread.setDaemon(true);
    _thread.start();
    getLogger().info("Bloomberg event processor started");
    getLogger().info("Bloomberg started");


    if (_applicationName != null) {
      _applicationIdentity = authorizeApplicationUser();
      _identityScheduler = Executors.newScheduledThreadPool(1);
      _identityCheckTask = _identityScheduler.scheduleAtFixedRate(new Runnable() {

        @Override
        public void run() {
          getLogger().debug("Re authorizing the user");
          try {
            _applicationIdentity = authorizeApplicationUser();
          } catch (Exception ex) {
            releaseBlockedRequests();
            throw ex;
          }
        }
      }, _reAuthorizationScheduleTime, _reAuthorizationScheduleTime, SECONDS);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Checks if the Bloomberg service is running.
   * 
   * @return true if running
   */
  @Override
  public synchronized boolean isRunning() {
    if (_thread == null) {
      return false;
    }
    return _thread.isAlive();
  }

  /**
   * Ensures that the Bloomberg session has been started.
   */
  protected void ensureStarted() {
    getSession();
    if (_thread == null || _thread.isAlive() == false) {
      throw new IllegalStateException("Event polling thread not alive; has start() been called?");
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Stops the Bloomberg service.
   */
  @Override
  public synchronized void stop() {
    if (isRunning() == false) {
      getLogger().info("Bloomberg already stopped");
      return;
    }
    getLogger().info("Bloomberg event processor being stopped...");
    _eventProcessor.terminate();
    try {
      _thread.join();
    } catch (InterruptedException e) {
      Thread.interrupted();
    }
    _eventProcessor = null;
    _thread = null;
    releaseBlockedRequests();

    getLogger().debug("shutting down identity scheduler task");
    if (isAuthorized()) {
      _identityCheckTask.cancel(true);
      _identityScheduler.shutdownNow();
    }
    _sessionProvider.stop();
    getLogger().info("Bloomberg event processor stopped");
  }

  protected boolean isAuthorized() {
    return _identityCheckTask != null && _identityScheduler != null;
  }

  private Identity authorizeApplicationUser() {
    Session session = getSession();
    final Identity identity = session.createIdentity();
    String authenticationOptions = _bloombergConnector.getSessionOptions().authenticationOptions();

    getLogger().debug("Attempting to authorize application user using authentication option: {}", authenticationOptions);
    EventQueue tokenEventQueue = new EventQueue();
    try {
      getSession().generateToken(new CorrelationID(generateCorrelationID()), tokenEventQueue);
      String token = null;
      //Generate token responses will come on this dedicated queue. There would be no other messages on that queue.
      Event event = tokenEventQueue.nextEvent(WAIT_TIME_MS);
      if (event.eventType() == Event.EventType.TOKEN_STATUS || event.eventType() == Event.EventType.REQUEST_STATUS) {
        for (Message msg : event) {
          if (msg.messageType() == TOKEN_SUCCESS) {
            token = msg.getElementAsString(TOKEN_ELEMENT);
          }
        }
      }
      if (token == null) {
        throw new OpenGammaRuntimeException("Failed to get token for bpipe app using  authentication option: " + authenticationOptions);
      }
  
      getLogger().debug("Token {} generated for application: {}", token, authenticationOptions);

      Service authService = _sessionProvider.getService(BloombergConstants.AUTH_SVC_NAME);
      Request authRequest = authService.createAuthorizationRequest();
      authRequest.set(TOKEN_ELEMENT, token);
  
      List<Element> authorizationResponse = submitAuthorizationRequest(authRequest, identity).get();
      if (authorizationResponse == null || authorizationResponse.isEmpty()) {
        throw new OpenGammaRuntimeException("Bloomberg authorization failed using  authentication option: " + authenticationOptions);
      }
      for (Element resultElem : authorizationResponse) {
        if (resultElem != null && AUTHORIZATION_SUCCESSS == resultElem.name()) {
          getLogger().debug("Authorization success for application: {}", authenticationOptions);
          return identity;
        }
      }
      throw new OpenGammaRuntimeException("Bloomberg authorization failed using  authentication option: " + authenticationOptions);
    } catch (IOException | InterruptedException | ExecutionException ex) {
      throw new OpenGammaRuntimeException("Failure to authenticate the bloomberg user using  authentication option:" + authenticationOptions);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Thread runner that handles Bloomberg events.
   */
  private class BloombergSessionEventProcessor extends TerminatableJob {

    /** Time to wait between attepts if there is no Bloomberg connection available. */
    private static final long RETRY_PERIOD = 30000;

    @Override
    protected void runOneCycle() {
      Event event;
      try {
        event = getSession().nextEvent(1000L);
      } catch (InterruptedException e) {
        Thread.interrupted();
        getLogger().warn("Unable to retrieve the next event available for processing on this session", e);
        return;
      } catch (ConnectionUnavailableException e) {
        getLogger().warn("No connection to Bloomberg available, failed to get next event", e);
        try {
          Thread.sleep(RETRY_PERIOD);
        } catch (InterruptedException e1) {
          getLogger().warn("Interrupted waiting to retry", e1);
        }
        return;
      } catch (RuntimeException e) {
        getLogger().warn("Unable to retrieve the next event available for processing on this session", e);
        return;
      }
      if (event == null) {
        //getLogger().debug("Got NULL event");
        return;
      }
      getLogger().debug("Got event of type {}", event.eventType());
      if (getLogger().isDebugEnabled()) {
        for (Message msg : event) {
          getLogger().debug("{}", msg);
        }
      }

      MessageIterator msgIter = event.messageIterator();
      while (msgIter.hasNext()) {
        Message msg = msgIter.next();
        if (event.eventType() == Event.EventType.SESSION_STATUS) {
          if (msg.messageType().toString().equals("SessionTerminated")) {
            getLogger().error("Session terminated");
            // Invalidate the session (which will release any blocked threads)
            invalidateSession();
            return;
          }
        }

        final CorrelationID responseCid = msg.correlationID();
        Element element = msg.asElement();
        getLogger().debug("got msg with cid={} msg.asElement={}", responseCid, msg.asElement());
        if (responseCid != null) {
          List<Element> messages = _responseMessages.get(responseCid);
          if (messages != null) {
            messages.add(element);
          }
        }
      }

      if (event.eventType() == Event.EventType.RESPONSE) {
        for (Message message : event) {
          CorrelationID correlationID = message.correlationID();
          List<Element> result = _responseMessages.remove(correlationID);
          if (result != null) {
            SettableFuture<List<Element>> responseFuture = _responseFutures.remove(correlationID);
            if (responseFuture != null) {
              responseFuture.set(result);
            }
          }
        }
      }
    }
  }
}
