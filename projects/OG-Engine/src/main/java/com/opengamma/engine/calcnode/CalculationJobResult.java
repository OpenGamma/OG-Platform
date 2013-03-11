/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.Object2LongMap;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ObjectUtils;

import com.opengamma.engine.cache.IdentifierEncodedValueSpecifications;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains details about the result of a calculation job. The result can be correlated to the original
 * {@link CalculationJob} through the {@link CalculationJobSpecification}.
 */
public class CalculationJobResult implements IdentifierEncodedValueSpecifications {

  private static final Logger s_logger = LoggerFactory.getLogger(CalculationJobResult.class);

  private final CalculationJobSpecification _specification;

  /**
   * The set of result items in the same order as the items from the original job request.
   */
  private final List<CalculationJobResultItem> _resultItems;
  
  private final long _durationNanos;
  private final String _nodeId;
  
  /**
   * Constructs an instance.
   * 
   * @param specification  the original calculation job specification, not null
   * @param durationNanos  the duration of the job in nanoseconds
   * @param resultItems  the results in the same order as the items in the original request, not null
   * @param nodeId  the identifier of the calculation node used to perform the job
   */
  public CalculationJobResult(CalculationJobSpecification specification, long durationNanos,
      List<CalculationJobResultItem> resultItems, String nodeId) {
    ArgumentChecker.notNull(specification, "specification");
    ArgumentChecker.notNull(resultItems, "resultItems");
    if (durationNanos < 0) {
      // Avoid failing for this, as nanoTime() may not work correctly
      s_logger.warn("Duration must be non-negative: " + durationNanos);
      durationNanos = 0;
    }
    ArgumentChecker.notNull(nodeId, "Node ID the job was executed on");
    
    _specification = specification;
    _durationNanos = durationNanos;
    _resultItems = resultItems;
    _nodeId = nodeId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the original calculation job specification.
   * 
   * @return the original calculation job specification, not null
   */
  public CalculationJobSpecification getSpecification() {
    return _specification;
  }

  /**
   * Gets the results in the same order as the items in the original request.
   * 
   * @return the result items, not null
   */
  public List<CalculationJobResultItem> getResultItems() {
    return Collections.unmodifiableList(_resultItems);
  }

  /**
   * Gets the duration of the job in nanoseconds.
   * 
   * @return the duration of the job in nanoseconds
   */
  public long getDuration() {
    return _durationNanos;
  }
  
  /**
   * Gets the identifier of the compute node used to perform the job.
   * 
   * @return the identifier of the compute node used to perform the job
   */
  public String getComputeNodeId() {
    return _nodeId;
  }
  
  //-------------------------------------------------------------------------
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

  @Override
  public int hashCode() {
    int hc = 1;
    hc += (hc << 4) + _specification.hashCode();
    hc += (hc << 4) + _resultItems.hashCode();
    hc += (hc << 4) + ((int) (_durationNanos >>> 32) ^ (int) _durationNanos);
    hc += (hc << 4) + ObjectUtils.nullSafeHashCode(_nodeId);
    return hc;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CalculationJobResult)) {
      return false;
    }
    final CalculationJobResult other = (CalculationJobResult) o;
    return _specification.equals(other._specification)
        && _resultItems.equals(other._resultItems)
        && (_durationNanos == other._durationNanos)
        && ObjectUtils.nullSafeEquals(_nodeId, other._nodeId);
  }

}
