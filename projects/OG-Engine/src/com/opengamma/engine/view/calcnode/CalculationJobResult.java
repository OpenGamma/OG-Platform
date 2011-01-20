/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.IdentifierMap;
import com.opengamma.util.ArgumentChecker;

/**
 * The response that a Calculation Node will return to invokers.
 *
 */
public class CalculationJobResult implements Serializable {
  
  private final CalculationJobSpecification _specification;
  private final List<CalculationJobResultItem> _resultItems;
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
      throw new IllegalArgumentException("Duration must be non-negative");
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

  /**
   * Numeric identifiers may have been passed when the result items were encoded as a Fudge message. This will resolve
   * them to full {@link ValueSpecification} objects.
   * 
   * @param identifierMap Identifier map to resolve the inputs with
   */
  public void resolveInputs(final IdentifierMap identifierMap) {
    for (CalculationJobResultItem item : _resultItems) {
      item.resolveInputs(identifierMap);
    }
  }

  /**
   * Convert full {@link ValueSpecification} objects to numeric identifiers within the result items for more efficient Fudge
   * encoding.
   * 
   * @param identifierMap Identifier map to convert the inputs with
   */
  public void convertInputs(final IdentifierMap identifierMap) {
    for (CalculationJobResultItem item : _resultItems) {
      item.convertInputs(identifierMap);
    }
  }

  @Override
  public String toString() {
    return "CalculationJobResult with " + _specification.toString();
  } 
}
