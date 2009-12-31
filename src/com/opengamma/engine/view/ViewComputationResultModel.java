/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;

/**
 * The data model represents the sum total of analytic functions applied to positions
 * in a particular view. It is the primary data repository for a particular
 * {@link View}.
 *
 * @author kirk
 */
public interface ViewComputationResultModel {
  
  // REVIEW kirk 2009-09-03 -- Should these be JSR-310 instants? Probably.
  
  long getInputDataTimestamp();
  
  long getResultTimestamp();
  
  // REVIEW kirk 2009-12-31 -- This is intended to cross network boundaries,
  // so has to be at the level of specifications.
  Collection<ComputationTargetSpecification> getAllTargets();
  
  Collection<ComputedValue> getValues(ComputationTargetSpecification target);
}
