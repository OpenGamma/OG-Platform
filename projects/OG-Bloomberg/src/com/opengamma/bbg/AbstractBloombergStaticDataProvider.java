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
import com.bloomberglp.blpapi.SessionOptions;
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
  private final SessionOptions _sessionOptions;

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
   * @param sessionOptions Options for connecting to the Bloomberg Server API process
   */
  public AbstractBloombergStaticDataProvider(SessionOptions sessionOptions) {
    ArgumentChecker.notNull(sessionOptions, "Session Options");
    ArgumentChecker.notNull(sessionOptions.getServerHost(), "Session Option Server Host");
    _sessionOptions = sessionOptions;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Bloomberg session options.
   * 
   * @return the session options
   */
  public SessionOptions getSessionOptions() {
    return _sessionOptions;
  }

  /**
   * Gets the active logger.
   * 
   * @return the logger.
   */
  protected abstract Logger getLogger();

  //-------------------------------------------------------------------------
  /**
   * Gets thes Bloomberg session.
   * 
   * @return the session
   */
  protected Session getSession() {
    return _session;
  }

  /**
   * Sets the Bloomberg session.
   * 
   * @param session  the session to set
   */
  protected void setSession(Session session) {
    _session = session;
  }

  //-------------------------------------------------------------------------
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
    ensureStarted();
    try {
      if (!getSession().openService(serviceName)) {
        throw new OpenGammaRuntimeException("Unable to open " + serviceName);
      }
    } catch (InterruptedException ex) {
      Thread.interrupted();
      throw new OpenGammaRuntimeException("Unable to open " + serviceName, ex);
    } catch (Exception ex) {
      throw new OpenGammaRuntimeException("Unable to open " + serviceName, ex);
    }
    return getSession().getService(serviceName);
  }

  //-------------------------------------------------------------------------
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

  /**
   * Generates a correlation identifier.
   * 
   * @return the correlation identifier, not null
   */
  protected long generateCorrelationID() {
    return _nextCorrelationId.getAndIncrement();
  }

  /**
   * Sends a request to Bloomberg, waiting for a correlation identifier.
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
   * Sends an authorization request to Bloomberg, waiting for a correlation identifier.
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
   * Checks if the Bloomberg service is running.
   * 
   * @return true if running
   */
  @Override
  public synchronized boolean isRunning() {
    getLogger().info("IsRunning on lifecycle method invocation");
    if (_thread == null) {
      return false;
    }
    return _thread.isAlive();
  }

  /**
   * Starts the Bloomberg service.
   */
  @Override
  public synchronized void start() {
    if (isRunning()) {
      getLogger().info("start() called, not necessary to do anything");
      return;
    }
    
    getLogger().info("Starting on lifecycle method invocation");
    getLogger().info("Making Bloomberg service connection...");
    SessionOptions options = getSessionOptions();
    final Session session = new Session(options);
    try {
      if (!session.start()) {
        throw new OpenGammaRuntimeException("Unable to start session with options " + getSessionOptions());
      }
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Unable to start session with options " + getSessionOptions(), e);
    }
    setSession(session);

    getLogger().info("Connected. Opening service.");
    openServices();
    
    // create and start the bloomberg event processor
    _eventProcessor = new BloombergSessionEventProcessor();
    _thread = new Thread(_eventProcessor, "BSM Event Processor");
    _thread.setDaemon(true);
    _thread.start();
  }

  /**
   * Stops the Bloomberg service.
   */
  @Override
  public synchronized void stop() {
    if (!isRunning()) {
      getLogger().info("stop() called, not necessary to do anything");
      return;
    }
    
    getLogger().info("Stopping on lifecycle method invocation");
    _eventProcessor.terminate();
    try {
      _thread.join();
    } catch (InterruptedException e) {
      Thread.interrupted();
    }
    _thread = null;
    if (_session != null) {
      try {
        _session.stop();
      } catch (InterruptedException e) {
        Thread.interrupted();
      }
    }
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
      } catch (InterruptedException e) {
        Thread.interrupted();
        throw new OpenGammaRuntimeException("Unable to retrieve the next event available for processing on this session", e);
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
