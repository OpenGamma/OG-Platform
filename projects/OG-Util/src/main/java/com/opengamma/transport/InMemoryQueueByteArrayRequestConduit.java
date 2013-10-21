/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.Lifecycle;

import com.opengamma.util.MdcAwareThreadPoolExecutor;
import com.opengamma.util.NamedThreadPoolFactory;

/**
 * Constructs a number of threads which each will dispatch byte array requests.
 * Can be used as a simple way of multithreading request receivers.
 */
public class InMemoryQueueByteArrayRequestConduit implements Lifecycle, ByteArrayRequestSender {
  private class DispatchJob implements Runnable {
    private final byte[] _requestMessage;
    private final ByteArrayMessageReceiver _responseReceiver;
    
    public DispatchJob(byte[] requestMessage, ByteArrayMessageReceiver responseReceiver) {
      _requestMessage = requestMessage;
      _responseReceiver = responseReceiver;
    }

    /**
     * @return the requestMessage
     */
    public byte[] getRequestMessage() {
      return _requestMessage;
    }

    /**
     * @return the responseReceiver
     */
    public ByteArrayMessageReceiver getResponseReceiver() {
      return _responseReceiver;
    }

    @Override
    public void run() {
      byte[] response = getUnderlying().requestReceived(getRequestMessage());
      getResponseReceiver().messageReceived(response);
    }

  }
  
  private final AtomicBoolean _started = new AtomicBoolean(false);
  private final ByteArrayRequestReceiver _underlying;
  private final ExecutorService _executor;
  private final boolean _localExecutor;
  
  public InMemoryQueueByteArrayRequestConduit(int nWorkerThreads, ByteArrayRequestReceiver underlying) {
    _underlying = underlying;
    ThreadFactory tf = new NamedThreadPoolFactory("InMemoryQueueByteArrayRequestConduit", true);
    ThreadPoolExecutor executor = new MdcAwareThreadPoolExecutor(
        0, nWorkerThreads, 5L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), tf);
    _executor = executor;
    _localExecutor = true;
  }
  
  public InMemoryQueueByteArrayRequestConduit(ExecutorService executor, ByteArrayRequestReceiver underlying) {
    _underlying = underlying;
    _executor = executor;
    _localExecutor = false;
  }
  
  /**
   * @return the underlying
   */
  public ByteArrayRequestReceiver getUnderlying() {
    return _underlying;
  }

  /**
   * @return the executor
   */
  public ExecutorService getExecutor() {
    return _executor;
  }

  @Override
  public void sendRequest(byte[] request,
      ByteArrayMessageReceiver responseReceiver) {
    getExecutor().execute(new DispatchJob(request, responseReceiver));
  }

  @Override
  public boolean isRunning() {
    return _started.get();
  }

  @Override
  public synchronized void start() {
    _started.set(true);
  }

  @Override
  public synchronized void stop() {
    if (!isRunning()) {
      return;
    }
    if (_localExecutor) {
      getExecutor().shutdown();
      try {
        getExecutor().awaitTermination(60, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        Thread.interrupted();
      }
    }
    _started.set(false);
  }

}
