/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.transport;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.TerminatableJob;

/**
 * An abstract implementation of a system that can consume batches of
 * messages and dispatch them to a {@link BatchByteArrayMessageReceiver}.
 * It will create a single <em>non-daemon</em> thread to pull messages
 * off the underlying message source.
 *
 * @author kirk
 */
public abstract class AbstractBatchMessageDispatcher implements Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractBatchMessageDispatcher.class);
  private final Set<BatchByteArrayMessageReceiver> _receivers =
    new HashSet<BatchByteArrayMessageReceiver>();
  private final MessageCollectionJob _collectionJob = new MessageCollectionJob();
  
  private final ByteArraySource _source;
  
  private String _name = "AbstractBatchMessageDispatcher";
  private Thread _dispatchThread;
  
  protected AbstractBatchMessageDispatcher(ByteArraySource source) {
    ArgumentChecker.checkNotNull(source, "byte array source");
    _source = source;
  }
  
  public void addReceiver(BatchByteArrayMessageReceiver receiver) {
    ArgumentChecker.checkNotNull(receiver, "batch message receiver");
    synchronized(_receivers) {
      _receivers.add(receiver);
    }
  }
  
  /**
   * @return the receivers
   */
  public Set<BatchByteArrayMessageReceiver> getReceivers() {
    return Collections.unmodifiableSet(_receivers);
  }

  /**
   * @return the collectionJob
   */
  public MessageCollectionJob getCollectionJob() {
    return _collectionJob;
  }
  
  /**
   * @return the source
   */
  public ByteArraySource getSource() {
    return _source;
  }

  /**
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * @param name the name to set
   */
  public void setName(String name) {
    _name = name;
  }

  /**
   * @return the dispatchThread
   */
  public Thread getDispatchThread() {
    return _dispatchThread;
  }

  @Override
  public boolean isRunning() {
    return ((getDispatchThread() != null)
        && getDispatchThread().isAlive());
  }

  @Override
  public synchronized void start() {
    if(isRunning()) {
      throw new IllegalStateException("Cannot start a running dispatcher.");
    }
    Thread dispatchThread = new Thread(getCollectionJob(), getName());
    dispatchThread.setDaemon(false);
    dispatchThread.start();
    _dispatchThread = dispatchThread;
  }

  @Override
  public synchronized void stop() {
    if(!isRunning()) {
      throw new IllegalStateException("Cannot stop a dispatcher which isn't running.");
    }
    getCollectionJob().terminate();
    try {
      getDispatchThread().join(30000l);
    } catch (InterruptedException e) {
      Thread.interrupted();
      s_logger.info("Interrupted while waiting for dispatch thread to finish.");
    }
    if(getDispatchThread().isAlive()) {
      s_logger.warn("Waited 30 seconds for dispatch thread to finish, but it didn't terminate normally.");
    }
    _dispatchThread = null;
  }
  
  protected void dispatchMessages(List<byte[]> messages) {
    synchronized(_receivers) {
      for(BatchByteArrayMessageReceiver receiver : getReceivers()) {
        receiver.messagesReceived(messages);
      }
    }
  }

  protected class MessageCollectionJob extends TerminatableJob {

    @Override
    protected void runOneCycle() {
      List<byte[]> batchMessages = getSource().batchReceive(1000l);
      if((batchMessages == null) || (batchMessages.isEmpty())) {
        return;
      }
      dispatchMessages(batchMessages);
    }
    
  }

}
