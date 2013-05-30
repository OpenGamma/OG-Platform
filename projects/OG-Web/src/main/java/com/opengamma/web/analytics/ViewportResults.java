/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import com.opengamma.util.ArgumentChecker;

/**
 * Set of calculation results for displaying in the viewport of a grid of analytics data.
 */
public class ViewportResults {

  /** The result values by row. */
  private final List<ResultsCell> _allResults;
  /** The grid columns. */
  private final GridColumnGroups _columns;
  /** Definition of the viewport. */
  private final ViewportDefinition _viewportDefinition;
  /** Duration of the last calculation cycle. */
  private final Duration _calculationDuration;
  /** The time at which these results became available. */
  private final Instant _valuationTime;

  /**
   * @param allResults Cells in the viewport containing the data, history and the value specification. The outer
   * list contains the data by rows and the inner lists contain the data for each row
   * @param viewportDefinition Definition of the rows and columns in the viewport
   * @param columns The columns in the viewport's grid
   */
  /* package */ ViewportResults(List<ResultsCell> allResults,
                                ViewportDefinition viewportDefinition,
                                GridColumnGroups columns,
                                Duration calculationDuration, Instant valuationTime) {
    ArgumentChecker.notNull(allResults, "allResults");
    ArgumentChecker.notNull(columns, "columns");
    ArgumentChecker.notNull(viewportDefinition, "viewportDefinition");
    ArgumentChecker.notNull(calculationDuration, "calculationDuration");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    _allResults = allResults;
    _viewportDefinition = viewportDefinition;
    _columns = columns;
    _calculationDuration = calculationDuration;
    _valuationTime = valuationTime;
  }

  /**
   * @return Cells in the viewport containing the data, history and the value specification. The outer
   * list contains the data by rows and the inner lists contain the data for each row
   */
  /* package */ List<ResultsCell> getResults() {
    return _allResults;
  }

  /**
   * @return The version of the viewport used when creating the results, allows the client to that a set of results
   * correspond to the current viewport state.
   */
  /* package */ long getVersion() {
    return _viewportDefinition.getVersion();
  }

  /**
   * @return The duration of the last calculation cycle.
   */
  /* package */ Duration getCalculationDuration() {
    return _calculationDuration;
  }
  
  /**
   * Gets the calculationTime.
   * @return the calculationTime
   */
  public Instant getValuationTime() {
    return _valuationTime;
  }
  
  /* package */ ViewportDefinition getViewportDefinition() {
    return _viewportDefinition;
  }
  
  /**
   * Gets the columns.
   * @return the columns
   */
  public GridColumnGroups getColumns() {
    return _columns;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
