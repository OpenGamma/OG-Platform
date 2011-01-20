/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */

package com.opengamma.engine.view.calc.stats;

import com.opengamma.engine.view.View;
import com.opengamma.engine.view.calc.DependencyGraphExecutor;

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
   * @param view The view for which graphs are being executed
   * @return The statistics gatherer, never {@code null}.
   */
  GraphExecutorStatisticsGatherer getStatisticsGatherer(View view);

}
