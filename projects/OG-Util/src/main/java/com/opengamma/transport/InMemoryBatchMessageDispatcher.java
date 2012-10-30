/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A batch message dispatcher that uses an in-memory queue.
 * <p>
 * This is a simple implementation operating in-memory.
 */
public class InMemoryBatchMessageDispatcher extends AbstractBatchMessageDispatcher {

  /**
   * The byte array source.
   */
  private final BlockingQueueByteArraySource _queueSource;

  /**
   * Creates an instance using a {@code LinkedBlockingQueue}.
   */
  public InMemoryBatchMessageDispatcher() {
    this(new LinkedBlockingQueue<byte[]>());
  }

  /**
   * Creates an instance specifying the queue to use.
   * 
   * @param queue  the queue, not null
   */
  public InMemoryBatchMessageDispatcher(final BlockingQueue<byte[]> queue) {
    this(new BlockingQueueByteArraySource(queue));
  }

  /**
   * Creates an instance using the wrapped queue.
   * 
   * @param source  the byte array source, not null
   */
  protected InMemoryBatchMessageDispatcher(final BlockingQueueByteArraySource source) {
    super(source);
    _queueSource = source;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the underlying queue.
   * 
   * @return the queue, not null
   */
  public BlockingQueue<byte[]> getQueue() {
    return _queueSource.getQueue();
  }

}
