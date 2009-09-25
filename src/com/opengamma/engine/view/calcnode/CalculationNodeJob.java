/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.security.SecurityKey;
import com.opengamma.util.Sealable;
import com.opengamma.util.SealableUtils;

/**
 * The definition of 
 *
 * @author kirk
 */
public class CalculationNodeJob implements Serializable, Sealable {
  private boolean _sealed = false;
  private String _viewName;
  private long _iterationTimestamp;
  private long _jobId;
  private String _functionUniqueIdentifier;
  private SecurityKey _securityKey;
  private final Set<AnalyticValueDefinition<?>> _inputs = new HashSet<AnalyticValueDefinition<?>>();

  @Override
  public synchronized boolean isSealed() {
    return _sealed;
  }

  @Override
  public synchronized void seal() {
    _sealed = true;
  }
  
  /**
   * @return the functionUniqueIdentifier
   */
  public String getFunctionUniqueIdentifier() {
    return _functionUniqueIdentifier;
  }

  /**
   * @param functionUniqueIdentifier the functionUniqueIdentifier to set
   */
  public void setFunctionUniqueIdentifier(String functionUniqueIdentifier) {
    SealableUtils.checkSealed(this);
    _functionUniqueIdentifier = functionUniqueIdentifier;
  }

  /**
   * @return the securityKey
   */
  public SecurityKey getSecurityKey() {
    return _securityKey;
  }

  /**
   * @param securityKey the securityKey to set
   */
  public void setSecurityKey(SecurityKey securityKey) {
    SealableUtils.checkSealed(this);
    _securityKey = securityKey;
  }

  /**
   * @return the viewName
   */
  public String getViewName() {
    return _viewName;
  }

  /**
   * @param viewName the viewName to set
   */
  public void setViewName(String viewName) {
    SealableUtils.checkSealed(this);
    _viewName = viewName;
  }

  /**
   * @return the iterationTimestamp
   */
  public long getIterationTimestamp() {
    return _iterationTimestamp;
  }

  /**
   * @param iterationTimestamp the iterationTimestamp to set
   */
  public void setIterationTimestamp(long iterationTimestamp) {
    SealableUtils.checkSealed(this);
    _iterationTimestamp = iterationTimestamp;
  }

  /**
   * @return the jobId
   */
  public long getJobId() {
    return _jobId;
  }

  /**
   * @param jobId the jobId to set
   */
  public void setJobId(long jobId) {
    SealableUtils.checkSealed(this);
    _jobId = jobId;
  }

  public void addInput(AnalyticValueDefinition<?> input) {
    SealableUtils.checkSealed(this);
    assert input != null;
    _inputs.add(input);
  }
  
  public void addInputs(Collection<AnalyticValueDefinition<?>> inputs) {
    SealableUtils.checkSealed(this);
    assert inputs != null;
    _inputs.addAll(inputs);
  }
  
  public Collection<AnalyticValueDefinition<?>> getInputs() {
    return Collections.unmodifiableSet(_inputs);
  }
}
