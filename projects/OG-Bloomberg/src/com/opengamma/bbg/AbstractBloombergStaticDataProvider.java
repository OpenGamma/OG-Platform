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
 * 
 */
public abstract class AbstractBloombergStaticDataProvider implements Lifecycle {
  // Injected Inputs:
  private final SessionOptions _sessionOptions;
  //Runtime State:
  private Session _session;
  private final AtomicLong _nextCorrelationId = new AtomicLong(1L);
  private final Map<CorrelationID, CorrelationID> _correlationIDMap = new ConcurrentHashMap<CorrelationID, CorrelationID>();
  private final Map<CorrelationID, BlockingQueue<Element>> _correlationIDElementMap = new ConcurrentHashMap<CorrelationID, BlockingQueue<Element>>();
  private BloombergSessionEventProcessor _bbgEventProcessor;
  private Thread _thread;

  /**
   * @param sessionOptions Options for connecting to the Bloomberg Server API process
   */
  public AbstractBloombergStaticDataProvider(SessionOptions sessionOptions) {
    ArgumentChecker.notNull(sessionOptions, "Session Options");
    ArgumentChecker.notNull(sessionOptions.getServerHost(), "Session Option Server Host");
    _sessionOptions = sessionOptions;
  }

  /**
   * @return the sessionOptions
   */
  public SessionOptions getSessionOptions() {
    return _sessionOptions;
  }

  /**
   * @param session the session to set
   */
  protected void setSession(Session session) {
    _session = session;
  }

  protected Service openService(String serviceName) {
    try {
      if (!getSession().openService(serviceName)) {
        throw new OpenGammaRuntimeException("Unable to open " + serviceName);
      }
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new OpenGammaRuntimeException("Unable to open " + serviceName, e);
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Unable to open " + serviceName, e);
    }
    return getSession().getService(serviceName);
  }

  protected abstract void openServices();

  /**
   * @return the session
   */
  protected Session getSession() {
    return _session;
  }

  protected long generateCorrelationID() {
    return _nextCorrelationId.getAndAdd(1L);
  }

  protected void ensureStarted() {
    if (getSession() == null) {
      throw new IllegalStateException("Session not set; has start() been called?");
    }
    if ((_thread == null) || !_thread.isAlive()) {
      throw new IllegalStateException("Event polling thread not alive; has start() been called?");
    }
  }

  protected CorrelationID submitBloombergRequest(Request request) {
    getLogger().debug("Sending Request={}", request);
    CorrelationID cid = new CorrelationID(generateCorrelationID());
    synchronized (cid) {
      _correlationIDMap.put(cid, cid);
      try {
        getSession().sendRequest(request, cid);
      } catch (Exception e) {
        _correlationIDMap.remove(cid);
        throw new OpenGammaRuntimeException("Unable to send request " + request, e);
      }
      try {
        cid.wait();
      } catch (InterruptedException e) {
        Thread.interrupted();
        throw new OpenGammaRuntimeException("Unable to process request " + request, e);
      }
    }
    return cid;
  }

  protected CorrelationID submitBloombergAuthorizationRequest(Request request, UserHandle userHandle) {
    getLogger().debug("Sending Request={}", request);
    CorrelationID cid = new CorrelationID(generateCorrelationID());
    synchronized (cid) {
      _correlationIDMap.put(cid, cid);
      try {
        getSession().sendAuthorizationRequest(request, userHandle, cid);
      } catch (Exception e) {
        _correlationIDMap.remove(cid);
        throw new OpenGammaRuntimeException("Unable to send request " + request, e);
      }
      try {
        cid.wait();
      } catch (InterruptedException e) {
        Thread.interrupted();
        throw new OpenGammaRuntimeException("Unable to process request " + request, e);
      }
    }
    return cid;
  }

  @Override
  public synchronized boolean isRunning() {
    getLogger().info("IsRunning on lifecycle method invocation");
    if (_thread == null) {
      return false;
    }
    return _thread.isAlive();
  }

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
    
    //create and start the bloomberg event processor
    _bbgEventProcessor = new BloombergSessionEventProcessor();
    _thread = new Thread(_bbgEventProcessor, "BSM Event Processor");
    _thread.setDaemon(true);
    _thread.start();
  }

  @Override
  public synchronized void stop() {
    if (!isRunning()) {
      getLogger().info("stop() called, not necessary to do anything");
      return;
    }
    
    getLogger().info("Stopping on lifecycle method invocation");
    _bbgEventProcessor.terminate();
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
      //wake up waiting client thread if response is completed and there is a thread waiting on the cid
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

  protected abstract Logger getLogger();
  
  protected BlockingQueue<Element> getResultElement(CorrelationID cid) {
    BlockingQueue<Element> resultElements = _correlationIDElementMap.remove(cid);
    //clear correlation maps
    _correlationIDMap.remove(cid);
    return resultElements;
  }

}
