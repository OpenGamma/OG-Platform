/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.Collections;
import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.IdentifierMap;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class CalculationJobResultItem {

  private final CalculationJobItem _item;
  private final InvocationResult _result;

  private final String _exceptionClass;
  private final String _exceptionMsg;
  private final String _stackTrace;

  private final Set<ValueSpecification> _missingInputs;

  public CalculationJobResultItem(CalculationJobItem item, Throwable exception) {
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

  /**
   * Numeric identifiers may have been passed when this was encoded as a Fudge message. This will resolve
   * them to full {@link ValueSpecification} objects.
   * 
   * @param identifierMap Identifier map to resolve the inputs with
   */
  public void resolveInputs(final IdentifierMap identifierMap) {
    _item.resolveInputs(identifierMap);
  }

  /**
   * Convert full {@link ValueSpecification} objects to numeric identifiers for more efficient Fudge
   * encoding.
   * 
   * @param identifierMap Identifier map to convert the inputs with
   */
  public void convertInputs(final IdentifierMap identifierMap) {
    _item.convertInputs(identifierMap);
  }

  public static CalculationJobResultItem create(CalculationJobItem item, InvocationResult result, 
      String exceptionClass, String exceptionMsg, String stackTrace, Set<ValueSpecification> missingInputs) {
    return new CalculationJobResultItem(item, result, exceptionClass, exceptionMsg, stackTrace, missingInputs);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("CalculationJobResultItem for ").append(getItem());
    return sb.toString();
  }

}
