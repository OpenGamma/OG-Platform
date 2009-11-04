/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.analytics.AnalyticFunctionRepository;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionBean;
import com.opengamma.engine.position.PositionReference;
import com.opengamma.engine.security.Security;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.view.ViewComputationCache;
import com.opengamma.engine.view.ViewComputationCacheSource;

/**
 * 
 *
 * @author kirk
 */
public abstract class AbstractCalculationNode {
  private static final Logger s_logger = LoggerFactory.getLogger(AbstractCalculationNode.class);
  private final ViewComputationCacheSource _cacheSource;
  private final AnalyticFunctionRepository _functionRepository;
  private final SecurityMaster _securityMaster;

  protected AbstractCalculationNode(
      ViewComputationCacheSource cacheSource,
      AnalyticFunctionRepository functionRepository,
      SecurityMaster securityMaster) {
    // TODO kirk 2009-09-25 -- Check inputs
    _cacheSource = cacheSource;
    _functionRepository = functionRepository;
    _securityMaster = securityMaster;
  }

  /**
   * @return the cacheSource
   */
  public ViewComputationCacheSource getCacheSource() {
    return _cacheSource;
  }

  /**
   * @return the functionRepository
   */
  public AnalyticFunctionRepository getFunctionRepository() {
    return _functionRepository;
  }

  /**
   * @return the securityMaster
   */
  public SecurityMaster getSecurityMaster() {
    return _securityMaster;
  }

  protected CalculationJobResult executeJob(CalculationJob job) {
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
    return jobResult;
  }

  /**
   * @param positionReference
   * @return
   */
  protected Position constructPosition(PositionReference positionReference) {
    Security security = getSecurityMaster().getSecurity(positionReference.getSecurityIdentityKey());
    if(security == null) {
      // REVIEW kirk 2009-11-04 -- This is bad because the try{} above won't catch it and
      // it'll kill the calc node.
      throw new OpenGammaRuntimeException("Unable to resolve security identity key " + positionReference.getSecurityIdentityKey());
    }
    return new PositionBean(positionReference.getQuantity(), security);
  }
  
}
