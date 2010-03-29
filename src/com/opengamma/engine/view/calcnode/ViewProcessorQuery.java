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
 *
 * @author jim
 */
public class ViewProcessorQuery {
  private final ViewProcessorQuerySender _calcNodeQuerySender;
  private final CalculationJobSpecification _jobSpec;

  public ViewProcessorQuery(ViewProcessorQuerySender calcNodeQuerySender, CalculationJobSpecification jobSpec) {
    _calcNodeQuerySender = calcNodeQuerySender;
    _jobSpec = jobSpec;
  }
  
  public Collection<ValueSpecification> getDependentValueSpecifications() {
    return _calcNodeQuerySender.getDependentValueSpecifications(_jobSpec );
  }
}
