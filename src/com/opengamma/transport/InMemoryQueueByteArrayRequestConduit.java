/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.TerminatableJob;

/**
 * Constructs a number of threads which each will dispatch byte array requests.
 * Can be used as a simple way of multithreading request receivers.
 *
 * @author kirk
 */
public class InMemoryQueueByteArrayRequestConduit implements Lifecycle, ByteArrayRequestSender {
  private static class Job {
    private final byte[] _requestMessage;
    private final ByteArrayMessageReceiver _responseReceiver;
    
    public Job(byte[] requestMessage, ByteArrayMessageReceiver responseReceiver) {
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

  }
  
  private final BlockingQueue<Job> _jobQueue = new SynchronousQueue<Job>();
  private final List<TerminatableJob> _terminatableJobs = new ArrayList<TerminatableJob>();
  private final List<Thread> _workerThreads = new ArrayList<Thread>();
  private final AtomicBoolean _started = new AtomicBoolean(false);
  private final ByteArrayRequestReceiver _underlying;
  
  public InMemoryQueueByteArrayRequestConduit(int nWorkerThreads, ByteArrayRequestReceiver underlying) {
    _underlying = underlying;
    for(int i = 0; i < nWorkerThreads; i++) {
      TerminatableJob tj = new TerminatableJob() {
        @Override
        protected void runOneCycle() {
          receiveAndDispatch();
        }
      };
      _terminatableJobs.add(tj);
      Thread t = new Thread(tj, "QueueByteArrayRequestConduit-" + i);
      t.setDaemon(true);
      _workerThreads.add(t);
    }
  }
  
  /**
   * @return the underlying
   */
  public ByteArrayRequestReceiver getUnderlying() {
    return _underlying;
  }

  @Override
  public void sendRequest(byte[] request,
      ByteArrayMessageReceiver responseReceiver) {
    try {
      _jobQueue.put(new Job(request, responseReceiver));
    } catch (InterruptedException e) {
      Thread.interrupted();
      throw new OpenGammaRuntimeException("Unable to send a request.");
    }
  }

  protected void receiveAndDispatch() {
    Job job = null;
    try {
      job = _jobQueue.poll(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.interrupted();
    }
    if(job == null) {
      return;
    }
    byte[] response = getUnderlying().requestReceived(job.getRequestMessage());
    job.getResponseReceiver().messageReceived(response);
  }

  @Override
  public boolean isRunning() {
    return _started.get();
  }

  @Override
  public synchronized void start() {
    for(Thread t : _workerThreads) {
      t.start();
    }
    _started.set(true);
  }

  @Override
  public synchronized void stop() {
    if(!isRunning()) {
      return;
    }
    for(TerminatableJob tj : _terminatableJobs) {
      tj.terminate();
    }
    for(Thread t : _workerThreads) {
      try {
        t.join();
      } catch (InterruptedException e) {
        Thread.interrupted();
      }
    }
    _started.set(false);
  }

}
