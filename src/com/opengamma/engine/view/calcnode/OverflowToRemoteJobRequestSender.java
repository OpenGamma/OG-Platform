/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.NamedThreadPoolFactory;

/**
 * An implementation of {@link JobRequestSender} which will overflow to another
 * {@link JobRequestSender} when its internal pool of worker threads is exhausted.
 */
public class OverflowToRemoteJobRequestSender implements JobRequestSender, Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(OverflowToRemoteJobRequestSender.class);
  private final JobRequestSender _overflowSender;
  private final CalculationNodeRequestReceiver _calculationNode;
  private final ExecutorService _executorService;
  private final SynchronousQueue<Runnable> _offerQueue;
  
  public OverflowToRemoteJobRequestSender(
      JobRequestSender overflowSender,
      CalculationNodeRequestReceiver calculationNode,
      int nLocalWorkers) {
    ArgumentChecker.notNull(overflowSender, "Overflow Sender");
    ArgumentChecker.notNull(calculationNode, "Calculation node");
    Validate.isTrue(nLocalWorkers > 0, "Must specify a positive number of local workers.");
    
    _overflowSender = overflowSender;
    _calculationNode = calculationNode;
    
    ThreadFactory tf = new NamedThreadPoolFactory("InMemoryQueueByteArrayRequestConduit", true);
    _offerQueue = new SynchronousQueue<Runnable>();
    ThreadPoolExecutor executor = new ThreadPoolExecutor(0, nLocalWorkers, 5L, TimeUnit.SECONDS, _offerQueue, tf);
    _executorService = executor;
  }

  /**
   * @return the overflowSender
   */
  public JobRequestSender getOverflowSender() {
    return _overflowSender;
  }

  /**
   * @return the calculationNode
   */
  public CalculationNodeRequestReceiver getCalculationNode() {
    return _calculationNode;
  }

  @Override
  public void sendRequest(CalculationJobSpecification jobSpec, List<CalculationJobItem> items, JobResultReceiver resultReceiver) {
    Runnable runnable = new LocalDispatchRunnable(jobSpec, items, resultReceiver);
    if (!_offerQueue.offer(runnable)) {
      // Overflow.
      s_logger.debug("Overflowing {} to overflow sender", jobSpec);
      getOverflowSender().sendRequest(jobSpec, items, resultReceiver);
    }
  }


  @Override
  public boolean isRunning() {
    return true;
  }

  @Override
  public void start() {
    // Intentional NO-OP.
  }

  @Override
  public void stop() {
    _executorService.shutdown();
  }
  
  private final class LocalDispatchRunnable implements Runnable {
    private final CalculationJobSpecification _jobSpec;
    private final List<CalculationJobItem> _items;
    private final JobResultReceiver _resultReceiver;
    
    public LocalDispatchRunnable(CalculationJobSpecification jobSpec,
      List<CalculationJobItem> items, 
      JobResultReceiver resultReceiver) {
      _jobSpec = jobSpec;
      _items = items;
      _resultReceiver = resultReceiver;
    }
    
    @Override
    public void run() {
      getCalculationNode().sendRequest(_jobSpec, _items, _resultReceiver);
    }
    
  }

}
