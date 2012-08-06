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
   * The active Bloomberg session.
   */
  private Session _session;
  /**
   * The provider of correlation identifiers.
   */
  private final AtomicLong _nextCorrelationId = new AtomicLong(1L);
  /**
   * The lookup table of correlation identifiers.
   */
  private final Map<CorrelationID, CorrelationID> _correlationIDMap = new ConcurrentHashMap<CorrelationID, CorrelationID>();
  /**
   * The lookup table of results.
   */
  private final Map<CorrelationID, BlockingQueue<Element>> _correlationIDElementMap = new ConcurrentHashMap<CorrelationID, BlockingQueue<Element>>();
  /**
   * The event processor listening to Bloomberg.
   */
  private BloombergSessionEventProcessor _eventProcessor;
  /**
   * The thread hosting the event processor.
   */
  private Thread _thread;

  /**
   * Creates an instance.
   * 
   * @param bloombergConnector  the Bloomberg connector, not null
   */
  public AbstractBloombergStaticDataProvider(BloombergConnector bloombergConnector) {
    ArgumentChecker.notNull(bloombergConnector, "bloombergConnector");
    _bloombergConnector = bloombergConnector;
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
   */
  protected Session getSession() {
    return _session;
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
    
    getLogger().info("Bloomberg session being opened...");
    _session = getBloombergConnector().createOpenSession();
    getLogger().info("Bloomberg session open");
    
    getLogger().info("Bloomberg services being opened...");
    openServices();
    getLogger().info("Bloomberg services open");
    
    getLogger().info("Bloomberg event processor being started...");
    _eventProcessor = new BloombergSessionEventProcessor();
    _thread = new Thread(_eventProcessor, "BSM Event Processor");
    _thread.setDaemon(true);
    _thread.start();
    getLogger().info("Bloomberg event processor started");
    
    getLogger().info("Bloomberg started");
  }

  /**
   * Opens all the services.
   * <p>
   * This method is typically implemented to call {@link #openService(String)}.
   */
  protected abstract void openServices();

  /**
   * Opens a Bloomberg service for the given name.
   * 
   * @param serviceName  the service name, not null
   * @return the service, not null
   */
  protected Service openService(String serviceName) {
    try {
      if (getSession().openService(serviceName) == false) {
        throw new OpenGammaRuntimeException("Bloomberg service failed to start: " + serviceName);
      }
    } catch (InterruptedException ex) {
      Thread.interrupted();
      throw new OpenGammaRuntimeException("Bloomberg service failed to start: " + serviceName, ex);
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException("Bloomberg service failed to start: " + serviceName, ex);
    }
    return getSession().getService(serviceName);
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
    
    getLogger().info("Bloomberg session being stopped...");
    if (_session != null) {
      try {
        _session.stop();
      } catch (InterruptedException e) {
        Thread.interrupted();
      }
    }
    getLogger().info("Bloomberg session stopped");
  }

  //-------------------------------------------------------------------------
  /**
   * Thread runner that handles Bloomberg events.
   */
  private class BloombergSessionEventProcessor extends TerminatableJob {
    @Override
    protected void runOneCycle() {
      Event event = null;
      try {
        event = getSession().nextEvent(1000L);
      } catch (InterruptedException ex) {
        Thread.interrupted();
        throw new OpenGammaRuntimeException("Unable to retrieve the next event available for processing on this session", ex);
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
              messages = new LinkedBlockingQueue<Element>();
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
