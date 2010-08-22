/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collections;
import java.util.Set;

import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CalculationJobResultItem {

  private static final String ITEM_FIELD_NAME = "item";
  private static final String INVOCATION_RESULT_FIELD_NAME = "result";
  private static final String EXCEPTION_CLASS_FIELD_NAME = "exceptionClass";
  private static final String EXCEPTION_MSG_FIELD_NAME = "exceptionMsg";
  private static final String STACK_TRACE_FIELD_NAME = "stackTrace";
  private static final String MISSING_INPUTS_FIELD_NAME = "missingInputs";

  private final CalculationJobItem _item;
  private final InvocationResult _result;

  private final String _exceptionClass;
  private final String _exceptionMsg;
  private final String _stackTrace;

  private final Set<ValueSpecification> _missingInputs;

  public CalculationJobResultItem(CalculationJobItem item, Exception exception) {
    ArgumentChecker.notNull(item, "Calculation job item");
    ArgumentChecker.notNull(exception, "Result");

    _item = item;

    if (exception instanceof MissingInputException) {
      _result = InvocationResult.MISSING_INPUTS;
      _missingInputs = ((MissingInputException) exception).getMissingInputs();
    } else {
      _result = InvocationResult.FUNCTION_THREW_EXCEPTION;
      _missingInputs = Collections.emptySet();
    }

    _exceptionClass = exception.getClass().getName();
    _exceptionMsg = exception.getMessage();

    StringBuffer buffer = new StringBuffer();
    for (StackTraceElement element : exception.getStackTrace()) {
      buffer.append(element.toString() + "\n");
    }
    _stackTrace = buffer.toString();
  }

  public CalculationJobResultItem(CalculationJobItem item) {
    ArgumentChecker.notNull(item, "Calculation job item");

    _item = item;
    _result = InvocationResult.SUCCESS;

    _exceptionClass = null;
    _exceptionMsg = null;
    _stackTrace = null;
    _missingInputs = Collections.emptySet();
  }

  private CalculationJobResultItem(CalculationJobItem item, InvocationResult result, String exceptionClass, String exceptionMsg, String stackTrace, Set<ValueSpecification> missingInputs) {
    _item = item;
    _result = result;
    _exceptionClass = exceptionClass;
    _exceptionMsg = exceptionMsg;
    _stackTrace = stackTrace;
    _missingInputs = missingInputs;
  }

  public boolean failed() {
    return getResult() != InvocationResult.SUCCESS;
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

  public Set<ValueSpecification> getOutputs() {
    return getItem().getOutputs();
  }

  public String getExceptionClass() {
    return _exceptionClass;
  }

  public String getExceptionMsg() {
    return _exceptionMsg;
  }

  public String getStackTrace() {
    return _stackTrace;
  }

  public Set<ValueSpecification> getMissingInputs() {
    return Collections.unmodifiableSet(_missingInputs);
  }

  public FudgeFieldContainer toFudgeMsg(FudgeSerializationContext fudgeContext) {
    MutableFudgeFieldContainer msg = fudgeContext.newMessage();
    msg.add(ITEM_FIELD_NAME, getItem().toFudgeMsg(fudgeContext));
    msg.add(INVOCATION_RESULT_FIELD_NAME, getResult().name());
    msg.add(EXCEPTION_CLASS_FIELD_NAME, getExceptionClass());
    msg.add(EXCEPTION_MSG_FIELD_NAME, getExceptionMsg());
    msg.add(STACK_TRACE_FIELD_NAME, getStackTrace());
    fudgeContext.objectToFudgeMsg(msg, MISSING_INPUTS_FIELD_NAME, null, getMissingInputs());
    return msg;
  }

  @SuppressWarnings("unchecked")
  public static CalculationJobResultItem fromFudgeMsg(FudgeDeserializationContext fudgeContext, FudgeFieldContainer msg) {
    CalculationJobItem item = CalculationJobItem.fromFudgeMsg(fudgeContext, msg.getMessage(ITEM_FIELD_NAME));
    InvocationResult result = InvocationResult.valueOf(msg.getString(INVOCATION_RESULT_FIELD_NAME));
    String exceptionClass = msg.getString(EXCEPTION_CLASS_FIELD_NAME);
    String exceptionMsg = msg.getString(EXCEPTION_MSG_FIELD_NAME);
    String stackTrace = msg.getString(STACK_TRACE_FIELD_NAME);
    Set<ValueSpecification> missingInputs = fudgeContext.fieldValueToObject(Set.class, msg.getByName(MISSING_INPUTS_FIELD_NAME));

    return new CalculationJobResultItem(item, result, exceptionClass, exceptionMsg, stackTrace, missingInputs);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("CalculationJobResultItem for ").append(getItem());
    return sb.toString();
  }

}
