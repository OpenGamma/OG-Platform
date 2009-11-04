/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.analytics.AnalyticFunctionRepository;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionBean;
import com.opengamma.engine.position.PositionReference;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
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
      CalculationJobSource jobSource,
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
    _dispatchThread = new Thread(_dispatchJob, "SingleThreadCalculationNode dispatch");
    _dispatchThread.setDaemon(true);
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
      CalculationJob job = getJobSource().getJob(5, TimeUnit.SECONDS);
      if(job != null) {
        CalculationJobSpecification spec = job.getSpecification();
        assert spec != null;
        ViewComputationCache cache = getCacheSource().getCache(spec.getViewName(), spec.getIterationTimestamp());
        AnalyticFunctionInvocationJob invocationJob;
        switch(job.getComputationTargetType()) {
        case SECURITY:
          {
            Security security = getSecurityMaster().getSecurity(job.getSecurityKey());
            invocationJob = new AnalyticFunctionInvocationJob(
                job.getFunctionUniqueIdentifier(), job.getInputs(), security, cache, getFunctionRepository());
          }
          break;
        case POSITION:
          {
            Position position = constructPosition(job.getPositionReference());
            invocationJob = new AnalyticFunctionInvocationJob(job.getFunctionUniqueIdentifier(),
                  job.getInputs(), position, cache, getFunctionRepository());
          }
          break;
        case MULTIPLE_POSITIONS:
          {
            List<Position> positions = new ArrayList<Position>(job.getPositionReferences().size());
            for(PositionReference positionReference : job.getPositionReferences()) {
              positions.add(constructPosition(positionReference));
            }
            invocationJob = new AnalyticFunctionInvocationJob(job.getFunctionUniqueIdentifier(),
                                                              job.getInputs(), positions, cache, getFunctionRepository());
          }
          break;
        case PRIMITIVE:
          invocationJob = new AnalyticFunctionInvocationJob(
              job.getFunctionUniqueIdentifier(), job.getInputs(), cache, getFunctionRepository());
          break;
        default:
          throw new OpenGammaRuntimeException("switch doesn't cover all cases");
        }
        long startTS = System.currentTimeMillis();
        boolean wasException = false;
        try {
          invocationJob.run();
        } catch (MissingInputException e) {
          // NOTE kirk 2009-10-20 -- We intentionally only do the message here so that we don't
          // litter the logs with stack traces.
          s_logger.info("Unable to invoke due to missing inputs invoking on {}: {}", job.getSecurityKey(), e.getMessage());
          wasException = true;
        } catch (Exception e) {
          s_logger.info("Invoking " + job.getFunctionUniqueIdentifier() + " on " + job.getSecurityKey() + " throw exception.",e);
          wasException = true;
        }
        long endTS = System.currentTimeMillis();
        long duration = endTS - startTS;
        InvocationResult invocationResult = wasException ? InvocationResult.ERROR : InvocationResult.SUCCESS;
        CalculationJobResult jobResult = new CalculationJobResult(spec, invocationResult, duration);
        getCompletionNotifier().jobCompleted(jobResult);
      }
    }

    /**
     * @param positionReference
     * @return
     */
    private Position constructPosition(PositionReference positionReference) {
      Security security = getSecurityMaster().getSecurity(positionReference.getSecurityIdentityKey());
      if(security == null) {
        // REVIEW kirk 2009-11-04 -- This is bad because the try{} above won't catch it and
        // it'll kill the calc node.
        throw new OpenGammaRuntimeException("Unable to resolve security identity key " + positionReference.getSecurityIdentityKey());
      }
      return new PositionBean(positionReference.getQuantity(), security);
    }
    
  }
}
