/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.engine.analytics.AnalyticFunctionRepository;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.view.AnalyticFunctionInvocationJob;
import com.opengamma.engine.view.ViewComputationCache;
import com.opengamma.engine.view.ViewComputationCacheSource;
import com.opengamma.util.TerminatableJob;

/**
 * 
 *
 * @author kirk
 */
public class SingleThreadCalculationNode extends AbstractCalculationNode
implements Lifecycle {
  private static final Logger s_logger = LoggerFactory.getLogger(SingleThreadCalculationNode.class);
  private final DispatchJob _dispatchJob = new DispatchJob();
  private Thread _dispatchThread;

  /**
   * @param cacheSource
   * @param functionRepository
   * @param securityMaster
   * @param jobSource
   * @param completionNotifier
   */
  public SingleThreadCalculationNode(
      ViewComputationCacheSource cacheSource,
      AnalyticFunctionRepository functionRepository,
      SecurityMaster securityMaster,
      CalculationNodeJobSource jobSource,
      JobCompletionNotifier completionNotifier) {
    super(cacheSource, functionRepository, securityMaster, jobSource,
        completionNotifier);
  }

  @Override
  public synchronized boolean isRunning() {
    return _dispatchThread != null;
  }

  @Override
  public synchronized void start() {
    _dispatchThread = new Thread(_dispatchJob, "SingleThreadCalculationNode job dispatch");
    _dispatchThread.start();
  }

  @Override
  public synchronized void stop() {
    _dispatchJob.terminate();
    try {
      _dispatchThread.join(100 * 1000l);
    } catch (InterruptedException e) {
      s_logger.warn("Interrupted waiting for dispatch thread to terminate.", e);
      Thread.interrupted();
    }
    _dispatchThread = null;
  }
  
  private class DispatchJob extends TerminatableJob {

    @Override
    protected void runOneCycle() {
      CalculationNodeJob job = getJobSource().getJob(5, TimeUnit.SECONDS);
      if(job != null) {
        Security security = getSecurityMaster().getSecurity(job.getSecurityKey());
        ViewComputationCache cache = getCacheSource().getCache(job.getViewName(), job.getIterationTimestamp());
        AnalyticFunctionInvocationJob invocationJob = new AnalyticFunctionInvocationJob(
            job.getFunctionUniqueIdentifier(), job.getInputs(), security, cache, getFunctionRepository());
        invocationJob.run();
        getCompletionNotifier().jobCompleted(job);
      }
    }
    
  }

}
