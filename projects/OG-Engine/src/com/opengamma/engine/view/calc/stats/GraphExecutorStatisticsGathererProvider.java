/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.view.calc.stats;

import com.opengamma.engine.view.calc.DependencyGraphExecutor;
import com.opengamma.id.UniqueId;

/**
 * Provides {@link GraphExecutorStatisticsGatherer} implementations for a given view. This is to allow
 * a single collection of statistics, or collection by a view processor on a per-view
 * basis.
 */
public interface GraphExecutorStatisticsGathererProvider {

  /**
   * Returns a {@link GraphExecutorStatisticsGatherer} for a {@link DependencyGraphExecutor} to report
   * its performance to.
   * 
   * @param viewId the unique identifier of the view for which graphs are being executed
   * @return The statistics gatherer, never null.
   */
  GraphExecutorStatisticsGatherer getStatisticsGatherer(UniqueId viewId);

}
