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
   * @param bloombergConnector  the Bloomberg connector, not null
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

  //-------------------------------------------------------------------------
  /**
   * Sends a request to Bloomberg, waiting for the response.
   * 
   * @param request  the request to send, not null
   * @return the correlation identifier, not null
   */
  protected CorrelationID submitBloombergRequest(Request request) {
    getLogger().debug("Sending Request={}", request);
    CorrelationID cid = new CorrelationID(generateCorrelationID());
    synchronized (cid) {
      _correlationIDMap.put(cid, cid);
      try {
        getSession().sendRequest(request, cid);
      } catch (Exception ex) {
        _correlationIDMap.remove(cid);
        throw new OpenGammaRuntimeException("Unable to send request " + request, ex);
      }
      try {
        cid.wait();
      } catch (InterruptedException ex) {
        Thread.interrupted();
        throw new OpenGammaRuntimeException("Unable to process request " + request, ex);
      }
    }
    return cid;
  }

  /**
   * Sends an authorization request to Bloomberg, waiting for the response.
   * 
   * @param request  the request to send, not null
   * @param userHandle  the user handle, not null
   * @return the correlation identifier, not null
   */
  @SuppressWarnings("deprecation")
  protected CorrelationID submitBloombergAuthorizationRequest(Request request, UserHandle userHandle) {
    getLogger().debug("Sending Request={}", request);
    CorrelationID cid = new CorrelationID(generateCorrelationID());
    synchronized (cid) {
      _correlationIDMap.put(cid, cid);
      try {
        getSession().sendAuthorizationRequest(request, userHandle, cid);
      } catch (Exception ex) {
        _correlationIDMap.remove(cid);
        throw new OpenGammaRuntimeException("Unable to send request " + request, ex);
      }
      try {
        cid.wait();
      } catch (InterruptedException ex) {
        Thread.interrupted();
        throw new OpenGammaRuntimeException("Unable to process request " + request, ex);
      }
    }
    return cid;
  }

  /**
   * Generates a correlation identifier.
   * 
   * @return the correlation identifier, not null
   */
  protected long generateCorrelationID() {
    return _nextCorrelationId.getAndIncrement();
  }

  /**
   * Gets the result given a correlation identifier.
   * 
   * @param cid  the correlation identifier, not null
   * @return the collection of results, not null
   */
  protected BlockingQueue<Element> getResultElement(CorrelationID cid) {
    BlockingQueue<Element> resultElements = _correlationIDElementMap.remove(cid);
    // clear correlation maps
    _correlationIDMap.remove(cid);
    return resultElements;
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
    if (getSession() == null) {
      throw new IllegalStateException("Session not set; has start() been called?");
    }
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
    _thread = null;
    getLogger().info("Bloomberg event processor stopped");
    _sessionProvider.invalidateSession();
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
            terminate();
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
        //cid is removed from the map by the request thread after it has  been notified
        synchronized (realCID) {
          realCID.notify();
        }
      }
    }

    @Override
    public void terminate() {
      super.terminate();
      
      // notify all threads waiting on cid
      Collection<CorrelationID> cids = _correlationIDMap.values();
      for (CorrelationID correlationID : cids) {
        synchronized (correlationID) {
          correlationID.notifyAll();
        }
      }
    }
  }

}
