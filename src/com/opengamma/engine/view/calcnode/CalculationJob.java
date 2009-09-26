/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.security.SecurityKey;

/**
 * The definition of 
 *
 * @author kirk
 */
public class CalculationJob extends CalculationJobSpecification implements Serializable {
  private final String _functionUniqueIdentifier;
  private final SecurityKey _securityKey;
  private final Set<AnalyticValueDefinition<?>> _inputs = new HashSet<AnalyticValueDefinition<?>>();
  
  /**
   * @param viewName
   * @param iterationTimestamp
   * @param jobId
   */
  public CalculationJob(String viewName, long iterationTimestamp, long jobId,
      String functionUniqueIdentifier, SecurityKey securityKey,
      Collection<AnalyticValueDefinition<?>> inputs) {
    super(viewName, iterationTimestamp, jobId);
    // TODO kirk 2009-09-25 -- Check Inputs
    _functionUniqueIdentifier = functionUniqueIdentifier;
    _securityKey = securityKey;
    _inputs.addAll(inputs);
  }

  /**
   * @return the functionUniqueIdentifier
   */
  public String getFunctionUniqueIdentifier() {
    return _functionUniqueIdentifier;
  }

  /**
   * @return the securityKey
   */
  public SecurityKey getSecurityKey() {
    return _securityKey;
  }

  /**
   * @return the inputs
   */
  public Set<AnalyticValueDefinition<?>> getInputs() {
    return _inputs;
  }

}
