/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.bloomberglp.blpapi.CorrelationID;
import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.MessageIterator;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;
import com.bloomberglp.blpapi.UserHandle;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.livedata.ConnectionUnavailableException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;

/**
 * Abstract data provider for connecting to Bloomberg.
 */
public abstract class AbstractBloombergStaticDataProvider implements Lifecycle {

  private static final Logger s_logger = LoggerFactory.getLogger(AbstractBloombergStaticDataProvider.class);

  /**
   * The Bloomberg session options.
   */
  private final BloombergConnector _bloombergConnector;

  /**
   * The provider of correlation identifiers.
   */
  private final AtomicLong _nextCorrelationId = new AtomicLong(1L);
  /**
   * The lookup table of correlation identifiers.
   */
  private final Map<CorrelationID, CorrelationID> _correlationIDMap = new ConcurrentHashMap<>();
  /**
   * The lookup table of results.
   */
  private final Map<CorrelationID, BlockingQueue<Element>> _correlationIDElementMap = new ConcurrentHashMap<>();
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
   * Creates an instance.
   * 
   * @param bloombergConnector the Bloomberg connector, not null
   * @param serviceName The Bloomberg service to start
   */
  public AbstractBloombergStaticDataProvider(BloombergConnector bloombergConnector, String serviceName) {
    ArgumentChecker.notNull(bloombergConnector, "bloombergConnector");
    ArgumentChecker.notEmpty(serviceName, "serviceName");
    _bloombergConnector = bloombergConnector;
    _sessionProvider = new SessionProvider(_bloombergConnector, serviceName);
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
    return _sessionProvider.getService();
  }

  private synchronized void releaseBlockedRequests() {
    // Notify all threads waiting on their correlation IDs. Any that were from the previous session
    // will spot the session object change and abort themselves. Any that were from the current
    // session (eg got a new session handle between the old session becoming invalid and this being called)
    // will see that the session has not changed and carry on waiting for a real notification.
    Collection<CorrelationID> cids = _correlationIDMap.values();
    for (CorrelationID correlationID : cids) {
      synchronized (correlationID) {
        correlationID.notifyAll();
      }
    }
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
  protected BlockingQueue<Element> submitBloombergRequest(Request request) {
    getLogger().debug("Sending Request={}", request);
    final Session session = getSession();
    final BlockingQueue<Element> result;
    final CorrelationID cid = new CorrelationID(generateCorrelationID());
    try {
      synchronized (cid) {
        _correlationIDMap.put(cid, cid);
        try {
          getSession().sendRequest(request, cid);
          // Wait until either the request is handled (and the dispatcher removes it from the map), or
          // the data provider changes which means the original request is lost.
          do {
            cid.wait();
          } while (_correlationIDMap.containsKey(cid) && (getSession() == session));
        } catch (Exception ex) {
          throw new OpenGammaRuntimeException("Unable to send request " + request, ex);
        } finally {
          _correlationIDMap.remove(cid);
        }
      }
    } finally {
      // Either return the data if populated by the dispatch thread, or just discard it from the map for housekeeping
      // purposes.
      result = _correlationIDElementMap.remove(cid);
    }
    if (result == null) {
      throw new OpenGammaRuntimeException("Did not receive response for request " + request);
    }
    return result;
  }

  /**
   * Sends an authorization request to Bloomberg, waiting for the response.
   * 
   * @param request the request to send, not null
   * @param userHandle the user handle, not null
   * @return the collection of results, not null
   */
  @SuppressWarnings("deprecation")
  protected BlockingQueue<Element> submitBloombergAuthorizationRequest(Request request, UserHandle userHandle) {
    getLogger().debug("Sending Request={}", request);
    final Session session = getSession();
    final BlockingQueue<Element> result;
    final CorrelationID cid = new CorrelationID(generateCorrelationID());
    try {
      synchronized (cid) {
        _correlationIDMap.put(cid, cid);
        try {
          session.sendAuthorizationRequest(request, userHandle, cid);
          // Wait until either the request is handled (and the dispatcher removes it from the map), or
          // the data provider changes which means the original request is lost.
          do {
            cid.wait();
          } while (_correlationIDMap.containsKey(cid) && (getSession() == session));
        } catch (Exception ex) {
          throw new OpenGammaRuntimeException("Unable to send/process request " + request, ex);
        } finally {
          _correlationIDMap.remove(cid);
        }
      }
    } finally {
      // Either return the data if populated by the dispatch thread, or just discard it from the map for housekeeping
      // purposes.
      result = _correlationIDElementMap.remove(cid);
    }
    if (result == null) {
      throw new OpenGammaRuntimeException("Did not receive response for request " + request);
    }
    return result;
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
    _sessionProvider.stop();
    _eventProcessor.terminate();
    _eventProcessor = null;
    try {
      _thread.join();
    } catch (InterruptedException e) {
      Thread.interrupted();
    }
    _thread = null;
    releaseBlockedRequests();
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
        s_logger.warn("Unable to retrieve the next event available for processing on this session", e);
        return;
      } catch (ConnectionUnavailableException e) {
        s_logger.warn("No connection to Bloomberg available, failed to get next event", e);
        try {
          Thread.sleep(RETRY_PERIOD);
        } catch (InterruptedException e1) {
          s_logger.warn("Interrupted waiting to retry", e1);
        }
        return;
      } catch (RuntimeException e) {
        s_logger.warn("Unable to retrieve the next event available for processing on this session", e);
        return;
      }
      if (event == null) {
        //getLogger().debug("Got NULL event");
        return;
      }
      //getLogger().debug("Got event of type {}", event.eventType());
      MessageIterator msgIter = event.messageIterator();
      CorrelationID realCID = null;
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

        CorrelationID bbgCID = msg.correlationID();
        Element element = msg.asElement();
        getLogger().debug("got msg with cid={} msg.asElement={}", bbgCID, msg.asElement());
        if (bbgCID != null) {
          realCID = _correlationIDMap.get(bbgCID);
          if (realCID != null) {
            BlockingQueue<Element> messages = _correlationIDElementMap.get(realCID);
            if (messages == null) {
              messages = new LinkedBlockingQueue<>();
              _correlationIDElementMap.put(realCID, messages);
            }
            messages.add(element);
          }
        }
      }
      // wake up waiting client thread if response is completed and there is a thread waiting on the cid
      if (event.eventType() == Event.EventType.RESPONSE && realCID != null) {
        //Remove the CID from the map so the caller knows this isn't a spurious wakeup
        if (_correlationIDMap.remove(realCID) != null) {
          synchronized (realCID) {
            realCID.notify();
          }
        }
      }
    }

  }

}
