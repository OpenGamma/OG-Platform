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

import com.opengamma.util.ArgumentChecker;

/**
 * The response that a Calculation Node will return to invokers.
 *
 */
public class CalculationJobResult implements Serializable {
  private static final String DURATION_FIELD_NAME = "duration";
  private static final String ITEMS_FIELD_NAME = "resultItems";
  
  private final CalculationJobSpecification _specification;
  private final List<CalculationJobResultItem> _resultItems;
  private final long _durationNanos;
  
  public CalculationJobResult(
      CalculationJobSpecification specification,
      long durationNanos,
      List<CalculationJobResultItem> resultItems) {
    ArgumentChecker.notNull(specification, "Calculation job spec");
    ArgumentChecker.notNull(resultItems, "Result items");
    if (durationNanos < 0) {
      throw new IllegalArgumentException("Duration must be non-negative");
    }
    
    _specification = specification;
    _durationNanos = durationNanos;
    _resultItems = resultItems;
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

  public FudgeFieldContainer toFudgeMsg(FudgeSerializationContext fudgeContext) {
    MutableFudgeFieldContainer msg = fudgeContext.newMessage();
    getSpecification().writeFields(msg);
    msg.add(DURATION_FIELD_NAME, getDuration());
    for (CalculationJobResultItem item : getResultItems()) {
      msg.add(ITEMS_FIELD_NAME, item.toFudgeMsg(fudgeContext));
    }
    return msg;
  }
  
  public static CalculationJobResult fromFudgeMsg(FudgeDeserializationContext fudgeContext, FudgeFieldContainer msg) {
    CalculationJobSpecification jobSpec = CalculationJobSpecification.fromFudgeMsg(msg);
    long duration = msg.getLong(DURATION_FIELD_NAME);
    List<CalculationJobResultItem> jobItems = new ArrayList<CalculationJobResultItem>();
    for (FudgeField field : msg.getAllByName(ITEMS_FIELD_NAME)) {
      CalculationJobResultItem jobItem = CalculationJobResultItem.fromFudgeMsg(fudgeContext, (FudgeFieldContainer) field.getValue());
      jobItems.add(jobItem);
    }
    return new CalculationJobResult(jobSpec, duration, jobItems);
  }
  
  @Override
  public String toString() {
    return "CalculationJobResult with " + _specification.toString();
  } 
}
