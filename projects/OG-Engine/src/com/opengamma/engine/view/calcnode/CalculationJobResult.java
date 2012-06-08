/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.IdentifierEncodedValueSpecifications;
import com.opengamma.util.ArgumentChecker;

/**
 * The response that a Calculation Node will return to invokers.
 *
 */
public class CalculationJobResult implements IdentifierEncodedValueSpecifications {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(CalculationJobResult.class);

  private final CalculationJobSpecification _specification;

  /**
   * The set of result items in the same order as the items from the original job request.
   */
  private final List<CalculationJobResultItem> _resultItems;
  // TODO: don't return all result items -- just the ones that were failures
  private final long _durationNanos;
  private final String _nodeId;
  
  public CalculationJobResult(
      CalculationJobSpecification specification,
      long durationNanos,
      List<CalculationJobResultItem> resultItems,
      String nodeId) {
    ArgumentChecker.notNull(specification, "Calculation job spec");
    ArgumentChecker.notNull(resultItems, "Result items");
    if (durationNanos < 0) {
      // avoid failing for this, as nanoTime() may not work correctly
      s_logger.warn("Duration must be non-negative: " + durationNanos);
      durationNanos = 0;
    }
    ArgumentChecker.notNull(nodeId, "Node ID the job was executed on");
    
    _specification = specification;
    _durationNanos = durationNanos;
    _resultItems = resultItems;
    _nodeId = nodeId;
  }

  public CalculationJobSpecification getSpecification() {
    return _specification;
  }

  public List<CalculationJobResultItem> getResultItems() {
    return Collections.unmodifiableList(_resultItems);
  }

  /**
   * @return the duration, in nanoseconds
   */
  public long getDuration() {
    return _durationNanos;
  }
  
  public String getComputeNodeId() {
    return _nodeId;
  }

  @Override
  public void convertIdentifiers(final Long2ObjectMap<ValueSpecification> identifiers) {
    for (CalculationJobResultItem item : _resultItems) {
      item.convertIdentifiers(identifiers);
    }
  }

  @Override
  public void collectIdentifiers(final LongSet identifiers) {
    for (CalculationJobResultItem item : _resultItems) {
      item.collectIdentifiers(identifiers);
    }
  }

  @Override
  public void convertValueSpecifications(final Object2LongMap<ValueSpecification> valueSpecifications) {
    for (CalculationJobResultItem item : _resultItems) {
      item.convertValueSpecifications(valueSpecifications);
    }
  }

  @Override
  public void collectValueSpecifications(final Set<ValueSpecification> valueSpecifications) {
    for (CalculationJobResultItem item : _resultItems) {
      item.collectValueSpecifications(valueSpecifications);
    }
  }

  @Override
  public String toString() {
    return "CalculationJobResult with " + _specification.toString();
  } 
}
