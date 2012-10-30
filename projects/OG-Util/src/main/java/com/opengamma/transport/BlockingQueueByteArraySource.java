/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
public class BlockingQueueByteArraySource implements ByteArraySource {
  private final BlockingQueue<byte[]> _queue;
  
  public BlockingQueueByteArraySource() {
    this(new LinkedBlockingQueue<byte[]>());
  }
  
  public BlockingQueueByteArraySource(BlockingQueue<byte[]> queue) {
    ArgumentChecker.notNull(queue, "queue");
    _queue = queue;
  }

  /**
   * @return the queue
   */
  public BlockingQueue<byte[]> getQueue() {
    return _queue;
  }

  @Override
  public List<byte[]> batchReceive(long maxWaitInMilliseconds) {
    List<byte[]> result = new LinkedList<byte[]>();
    byte[] head = null;
    try {
      head = getQueue().poll(maxWaitInMilliseconds, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.interrupted();
    }
    if (head != null) {
      result.add(head);
      getQueue().drainTo(result);
    }
    return result;
  }

  @Override
  public List<byte[]> batchReceiveNoWait() {
    List<byte[]> result = new LinkedList<byte[]>();
    getQueue().drainTo(result);
    return result;
  }

  @Override
  public byte[] receive(long maxWaitInMilliseconds) {
    try {
      return getQueue().poll(maxWaitInMilliseconds, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      Thread.interrupted();
    }
    return null;
  }

  @Override
  public byte[] receiveNoWait() {
    return getQueue().poll();
  }

}
