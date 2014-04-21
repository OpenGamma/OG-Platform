/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;

import org.threeten.bp.Instant;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * Result model containing the output of a single calculation cycle in a table.
 * Each row contains the outputs for a single trade, position or node.
 * TODO is this useful enough to live in a more general purpose package?
 */
public class SimpleResultModel {

  /** Execution options used when calculating the results. */
  private final ViewCycleExecutionOptions _executionOptions;
  
  /** The column names in the order they are defined in the view definition and appear in the results. */
  private final List<String> _columnNames;
  
  /** The trades, positions and portfolio nodes in the order they appear in the portfolio and the results. */
  private final List<UniqueIdentifiable> _targets;

  /** The results. */
  private final Table<Integer, Integer, Object> _results;

  /* package */ SimpleResultModel(List<UniqueIdentifiable> targets,
                                  List<String> columnNames,
                                  Table<Integer, Integer, Object> results,
                                  ViewCycleExecutionOptions executionOptions) {
    _executionOptions = ArgumentChecker.notNull(executionOptions, "executionOptions");
    _targets = ImmutableList.copyOf(ArgumentChecker.notNull(targets, "targets"));
    _columnNames = ImmutableList.copyOf(ArgumentChecker.notNull(columnNames, "columnNames"));
    _results = ArgumentChecker.notNull(results, "resultsGrid");
  }

  /**
   * @return The name of the cycle, possibly null
   */
  public String getCycleName() {
    return _executionOptions.getName();
  }

  /**
   * @return The column names in the order they are defined in the view definition and appear in the results.
   */
  public List<String> getColumnNames() {
    return _columnNames;
  }

  /**
   * @return The trades, positions and portfolio nodes in the order they appear in the portfolio and the results.
   */
  public List<UniqueIdentifiable> getTargets() {
    return _targets;
  }

  /**
   * @return The results
   */
  public Table<Integer, Integer, Object> getResults() {
    return _results;
  }

  /**
   * @return Valuation time used when calculating the results.
   */
  public Instant getValuationTime() {
    return _executionOptions.getValuationTime();
  }

  /**
   * @return Market data used when calculating the results.
   */
  public List<MarketDataSpecification> getMarketDataSpecifications() {
    return _executionOptions.getMarketDataSpecifications();
  }

  @Override
  public String toString() {
    return "SimpleResultModel [" +
        "_columnNames=" + _columnNames +
        ", _results=" + _results +
        ", _targets=" + _targets +
        ", _executionOptions=" + _executionOptions +
        "]";
  }
}
