/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;

import javax.time.Instant;

import com.opengamma.engine.ComputationTargetSpecification;

/**
 * A base interface for the common structures for whole and delta result models. The result
 * model can be queried by target (giving a configuration/value space) or by configuration
 * (giving a target/value space).
 */
public interface ViewResultModel {

  Instant getValuationTime();

  Instant getResultTimestamp();

  // REVIEW kirk 2009-12-31 -- This is intended to cross network boundaries,
  // so has to be at the level of specifications.
  Collection<ComputationTargetSpecification> getAllTargets();

  Collection<String> getCalculationConfigurationNames();

  ViewCalculationResultModel getCalculationResult(String calcConfigurationName);

  ViewTargetResultModel getTargetResult(ComputationTargetSpecification targetSpecification);

}
