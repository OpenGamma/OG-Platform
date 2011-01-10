/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 
 *
 * @author kirk
 */
public class InMemoryBatchMessageDispatcher extends
    AbstractBatchMessageDispatcher {
  private final BlockingQueueByteArraySource _queueSource;
  
  public InMemoryBatchMessageDispatcher() {
    this(new LinkedBlockingQueue<byte[]>());
  }
  
  public InMemoryBatchMessageDispatcher(BlockingQueue<byte[]> queue) {
    this(new BlockingQueueByteArraySource(queue));
  }
  
  protected InMemoryBatchMessageDispatcher(BlockingQueueByteArraySource source) {
    super(source);
    _queueSource = source;
  }
  
  public BlockingQueue<byte[]> getQueue() {
    return _queueSource.getQueue();
  }
  
}
