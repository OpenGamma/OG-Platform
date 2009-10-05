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
 * The definition of a particular job that must be performed by
 * a Calculation Node.
 *
 * @author kirk
 */
public class CalculationJob implements Serializable {
  private final CalculationJobSpecification _specification;
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
    this(new CalculationJobSpecification(viewName, iterationTimestamp, jobId),
        functionUniqueIdentifier, securityKey, inputs);
  }
  
  public CalculationJob(CalculationJobSpecification specification,
      String functionUniqueIdentifier, SecurityKey securityKey,
      Collection<AnalyticValueDefinition<?>> inputs) {
    // TODO kirk 2009-09-29 -- Check Inputs.
    _specification = specification;
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

  /**
   * @return the specification
   */
  public CalculationJobSpecification getSpecification() {
    return _specification;
  }

}
