/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calc;

import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.calcnode.Capability;
import com.opengamma.engine.view.calcnode.JobDispatcher;
import com.opengamma.engine.view.calcnode.PlatformCapabilities;
import com.opengamma.util.ArgumentChecker;

/**
 * <p>Continuously tunes the parameters to a {@link MultipleNodeExecutorFactory} to maintain good performance
 * while aspects of the computing cluster change. Schedule this to run periodically to update its sampling
 * and make continuous adjustments.</p>
 * 
 * <h2>Tuning rules<h2>
 * 
 * <p>Set maximum concurrency to the average node count of the job invokers. Requires a {@link JobDispatcher}.</p>
 * 
 * <p>TODO: [ENG-200] Tuning of job size and cost parameters</p>
 */
public class MultipleNodeExecutorTuner implements Runnable {

  private static final Logger s_logger = LoggerFactory.getLogger(MultipleNodeExecutorTuner.class);

  private final MultipleNodeExecutorFactory _factory;

  private JobDispatcher _jobDispatcher;

  /**
   * @param factory The factory to tune
   */
  public MultipleNodeExecutorTuner(final MultipleNodeExecutorFactory factory) {
    ArgumentChecker.notNull(factory, "factory");
    _factory = factory;
  }

  protected MultipleNodeExecutorFactory getFactory() {
    return _factory;
  }

  public void setJobDispatcher(final JobDispatcher jobDispatcher) {
    _jobDispatcher = jobDispatcher;
  }

  protected JobDispatcher getJobDispatcher() {
    return _jobDispatcher;
  }

  /**
   * Makes one tuning adjustment.
   */
  @Override
  public void run() {
    if (getJobDispatcher() != null) {
      final Map<String, Collection<Capability>> allCapabilities = getJobDispatcher().getAllCapabilities();
      int nodesPerInvokerCount = 0;
      double nodesPerInvoker = 0;
      for (Map.Entry<String, Collection<Capability>> capabilities : allCapabilities.entrySet()) {
        for (Capability capability : capabilities.getValue()) {
          if (PlatformCapabilities.NODE_COUNT.equals(capability.getIdentifier())) {
            nodesPerInvokerCount++;
            nodesPerInvoker += capability.getUpperBoundParameter();
          }
        }
      }
      if (nodesPerInvokerCount > 0) {
        s_logger.debug("Found {} nodes at {} invokers", nodesPerInvoker, nodesPerInvokerCount);
        int maxConcurrency = getFactory().getMaximumConcurrency();
        int newMaxConcurrency = (int) Math.ceil(nodesPerInvoker / (double) nodesPerInvokerCount);
        if (newMaxConcurrency != maxConcurrency) {
          s_logger.info("Changing maximum concurrency to {}", newMaxConcurrency);
          getFactory().setMaximumConcurrency(newMaxConcurrency);
        }
      }
    }
  }

}
