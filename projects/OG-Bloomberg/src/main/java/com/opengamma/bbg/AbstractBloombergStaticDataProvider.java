/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.springframework.context.Lifecycle;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Identity;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SettableFuture;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.permission.BloombergBpipeApplicationUserIdentityProvider;
import com.opengamma.livedata.ConnectionUnavailableException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;

/**
 * Abstract data provider for connecting to Bloomberg.
 */
public abstract class AbstractBloombergStaticDataProvider implements Lifecycle {

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
   * The service name
   */
  private final String _serviceName;
  private final boolean _requiresAuthorization;

  /**
   * The bpipe applicatiion user identity.
   */
  private volatile Identity _applicationIdentity;

  /**
   * Creates an instance.
   * 
   * @param bloombergConnector the Bloomberg connector, not null
   * @param serviceName The Bloomberg service to start
   */
  public AbstractBloombergStaticDataProvider(BloombergConnector bloombergConnector, String serviceName) {
    ArgumentChecker.notNull(bloombergConnector, "bloombergConnector");
    ArgumentChecker.notNull(bloombergConnector.getSessionOptions(), "bloombergConnector.sessionOptions");
    ArgumentChecker.notEmpty(serviceName, "serviceName");

    _requiresAuthorization = bloombergConnector.requiresAuthentication();
    _serviceName = serviceName;
    _bloombergConnector = bloombergConnector;
    _sessionProvider = new SessionProvider(_bloombergConnector, getServiceNames());

  }

  private List<String> getServiceNames() {
    List<String> serviceNames = Lists.newArrayList(_serviceName);
    if (_requiresAuthorization) {
      serviceNames.add(BloombergConstants.AUTH_SVC_NAME);
    }
    return serviceNames;
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
      if (_requiresAuthorization) {
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


    if (_requiresAuthorization) {
      // we need authorization done
      BloombergBpipeApplicationUserIdentityProvider identityProvider = new BloombergBpipeApplicationUserIdentityProvider(_sessionProvider);
      _applicationIdentity = identityProvider.getIdentity();
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
    _sessionProvider.stop();
    getLogger().info("Bloomberg event processor stopped");
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
