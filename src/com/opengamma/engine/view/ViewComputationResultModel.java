/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.Map;

import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.depgraph.DependencyGraphModel;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.security.SecurityMaster;

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
  
  /**
   * Obtain the positions that are part of this computation result.
   * This includes both the aggregate and leaf positions.
   * This may be different from the current state in the defining
   * {@link View} because positions can change over the course of time.
   * 
   * @return All positions part of this computation pass.
   */
  Collection<Position> getPositions();
  
  Map<AnalyticValueDefinition<?>, AnalyticValue<?>> getValues(Position position);
  
  AnalyticValue<?> getValue(Position position, AnalyticValueDefinition<?> valueDefinition);
  
  DependencyGraphModel getDependencyGraphModel();
  
  ViewComputationCache getComputationCache();
  // review this.
  SecurityMaster getSecurityMaster();
}
