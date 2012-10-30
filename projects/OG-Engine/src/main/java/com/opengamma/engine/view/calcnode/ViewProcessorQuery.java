/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collection;

import com.opengamma.engine.value.ValueSpecification;

/**
 * 
 */
public class ViewProcessorQuery {
  private final ViewProcessorQuerySender _calcNodeQuerySender;
  private final CalculationJobSpecification _jobSpec;

  public ViewProcessorQuery(ViewProcessorQuerySender calcNodeQuerySender, CalculationJobSpecification jobSpec) {
    // REVIEW kirk 2010-05-24 -- Any reason these aren't null checked?
    // I've not made them null checked as it suits me for a unit test that they can be null,
    // but it seems to me that they probably should be null checked.
    _calcNodeQuerySender = calcNodeQuerySender;
    _jobSpec = jobSpec;
  }
  
  /**
   * Returns all of the ValueSpecifications from the entire dependency graph. This is not optimized for wire
   * transit, and may return a load of stuff that isn't even part of the job when using the MultipleNodeExecutor.
   * If you're using this:
   * 
   *      1) Do you really want all of the value specs from the entire job graph? How about just the bit near your inputs/outputs?
   *      2) Revisit the sender and receiver so that they work with an identifier map source
   *      3) Revisit the MultipleNodeExecutor so that the graph fragments it registers are subgraphs for the jobs
   *      
   * @return The value specifications
   */
  public Collection<ValueSpecification> getDependentValueSpecifications() {
    return _calcNodeQuerySender.getDependentValueSpecifications(_jobSpec);
  }
}
