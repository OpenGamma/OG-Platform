/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Set;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CalculationJobResultItem {
  
  private static final String ITEM_FIELD_NAME = "item";
  private static final String INVOCATION_RESULT_FIELD_NAME = "result";

  // SENT BACK TO THE MASTER NODE
  
  private final CalculationJobItem _item;
  private final InvocationResult _result;
  
  // NOT SENT BACK TO THE MASTER NODE
  
  private transient Set<ComputedValue> _results;
  private transient Exception _exception;
  
  public CalculationJobResultItem(CalculationJobItem item,
      InvocationResult result) {
    ArgumentChecker.notNull(item, "Calculation job item");
    ArgumentChecker.notNull(result, "Result");
    _item = item;
    _result = result;
  }

  public CalculationJobItem getItem() {
    return _item;
  }
  
  public ComputationTargetSpecification getComputationTargetSpecification() {
    return getItem().getComputationTargetSpecification();
  }

  public InvocationResult getResult() {
    return _result;
  }
  
  public Set<ComputedValue> getResults() {
    return _results;
  }

  public void setResults(Set<ComputedValue> results) {
    ArgumentChecker.notNull(results, "Results");
    _results = results;
  }
  
  public Exception getException() {
    return _exception;
  }

  public void setException(Exception exception) {
    _exception = exception;
  }
  
  public Set<ValueSpecification> getOutputs() {
    return getItem().getOutputs();
  }

  public FudgeFieldContainer toFudgeMsg(FudgeSerializationContext fudgeContext) {
    MutableFudgeFieldContainer msg = fudgeContext.newMessage();
    msg.add(ITEM_FIELD_NAME, getItem().toFudgeMsg(fudgeContext));
    msg.add(INVOCATION_RESULT_FIELD_NAME, getResult().name());
    return msg;
  }
  
  public static CalculationJobResultItem fromFudgeMsg(FudgeDeserializationContext fudgeContext, FudgeFieldContainer msg) {
    CalculationJobItem item = CalculationJobItem.fromFudgeMsg(fudgeContext, msg.getMessage(ITEM_FIELD_NAME));
    InvocationResult result = InvocationResult.valueOf(msg.getString(INVOCATION_RESULT_FIELD_NAME));
    
    return new CalculationJobResultItem(item, result);
  }
  
}
