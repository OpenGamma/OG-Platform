/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
  
  public Collection<ValueSpecification> getDependentValueSpecifications() {
    return _calcNodeQuerySender.getDependentValueSpecifications(_jobSpec);
  }
}
