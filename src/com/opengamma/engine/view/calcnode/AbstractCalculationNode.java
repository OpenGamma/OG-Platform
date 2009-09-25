/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import com.opengamma.engine.analytics.AnalyticFunctionRepository;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.engine.view.ViewComputationCacheSource;

/**
 * 
 *
 * @author kirk
 */
public abstract class AbstractCalculationNode {
  private final ViewComputationCacheSource _cacheSource;
  private final AnalyticFunctionRepository _functionRepository;
  private final SecurityMaster _securityMaster;
  private final CalculationNodeJobSource _jobSource;
  private final JobCompletionNotifier _completionNotifier;

  protected AbstractCalculationNode(
      ViewComputationCacheSource cacheSource,
      AnalyticFunctionRepository functionRepository,
      SecurityMaster securityMaster,
      CalculationNodeJobSource jobSource,
      JobCompletionNotifier completionNotifier) {
    // TODO kirk 2009-09-25 -- Check inputs
    _cacheSource = cacheSource;
    _functionRepository = functionRepository;
    _securityMaster = securityMaster;
    _jobSource = jobSource;
    _completionNotifier = completionNotifier;
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

  /**
   * @return the jobSource
   */
  public CalculationNodeJobSource getJobSource() {
    return _jobSource;
  }

  /**
   * @return the completionNotifier
   */
  public JobCompletionNotifier getCompletionNotifier() {
    return _completionNotifier;
  }

}
