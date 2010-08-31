/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.IdentifierMap;
import com.opengamma.util.ArgumentChecker;

/**
 * The response that a Calculation Node will return to invokers.
 *
 */
public class CalculationJobResult implements Serializable {
  private static final String DURATION_FIELD_NAME = "duration";
  private static final String ITEMS_FIELD_NAME = "resultItems";
  private static final String COMPUTE_NODE_ID_FIELD_NAME = "computeNodeId";
  
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

  public FudgeFieldContainer toFudgeMsg(FudgeSerializationContext fudgeContext) {
    MutableFudgeFieldContainer msg = fudgeContext.newMessage();
    getSpecification().toFudgeMsg(msg);
    msg.add(DURATION_FIELD_NAME, getDuration());
    msg.add(COMPUTE_NODE_ID_FIELD_NAME, getComputeNodeId());
    for (CalculationJobResultItem item : getResultItems()) {
      msg.add(ITEMS_FIELD_NAME, item.toFudgeMsg(fudgeContext));
    }
    return msg;
  }
  
  public static CalculationJobResult fromFudgeMsg(FudgeDeserializationContext fudgeContext, FudgeFieldContainer msg) {
    CalculationJobSpecification jobSpec = CalculationJobSpecification.fromFudgeMsg(msg);
    long duration = msg.getLong(DURATION_FIELD_NAME);
    String nodeId = msg.getString(COMPUTE_NODE_ID_FIELD_NAME);
    List<CalculationJobResultItem> jobItems = new ArrayList<CalculationJobResultItem>();
    for (FudgeField field : msg.getAllByName(ITEMS_FIELD_NAME)) {
      CalculationJobResultItem jobItem = CalculationJobResultItem.fromFudgeMsg(fudgeContext, (FudgeFieldContainer) field.getValue());
      jobItems.add(jobItem);
    }
    return new CalculationJobResult(jobSpec, duration, jobItems, nodeId);
  }
  
  @Override
  public String toString() {
    return "CalculationJobResult with " + _specification.toString();
  } 
}
