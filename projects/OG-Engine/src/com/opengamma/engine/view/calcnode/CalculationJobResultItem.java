/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;
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

  private Set<ValueSpecification> _missingInputs;
  private long[] _missingInputIdentifiers;

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

  public CalculationJobResultItem(CalculationJobItem item, InvocationResult result, String exceptionClass, String exceptionMsg, String stackTrace, long[] missingInputIdentifiers) {
    _item = item;
    _result = result;
    _exceptionClass = exceptionClass;
    _exceptionMsg = exceptionMsg;
    _stackTrace = stackTrace;
    _missingInputIdentifiers = missingInputIdentifiers;
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

  public long[] getMissingInputIdentifiers() {
    return _missingInputIdentifiers;
  }

  /**
   * Numeric identifiers may have been passed when this was encoded as a Fudge message. This will resolve
   * them to full {@link ValueSpecification} objects.
   * 
   * @param identifierMap Identifier map to resolve the inputs with
   */
  public void resolveIdentifiers(final IdentifierMap identifierMap) {
    _item.resolveIdentifiers(identifierMap);
    if (_missingInputs == null) {
      if (_missingInputIdentifiers == null) {
        _missingInputs = Collections.emptySet();
      } else if (_missingInputIdentifiers.length == 1) {
        _missingInputs = Collections.singleton(identifierMap.getValueSpecification(_missingInputIdentifiers[0]));
      } else {
        _missingInputs = Sets.newHashSetWithExpectedSize(_missingInputIdentifiers.length);
        final Collection<Long> identifiers = new ArrayList<Long>(_missingInputIdentifiers.length);
        for (long identifier : _missingInputIdentifiers) {
          identifiers.add(identifier);
        }
        _missingInputs.addAll(identifierMap.getValueSpecifications(identifiers).values());
      }
    }
  }

  /**
   * Convert full {@link ValueSpecification} objects to numeric identifiers for more efficient Fudge
   * encoding.
   * 
   * @param identifierMap Identifier map to convert the inputs with
   */
  public void convertIdentifiers(final IdentifierMap identifierMap) {
    _item.convertIdentifiers(identifierMap);
    if ((_missingInputIdentifiers == null) && !_missingInputs.isEmpty()) {
      if (_missingInputs.size() == 1) {
        _missingInputIdentifiers = new long[] {identifierMap.getIdentifier(_missingInputs.iterator().next()) };
      } else {
        final Collection<Long> identifiers = identifierMap.getIdentifiers(_missingInputs).values();
        _missingInputIdentifiers = new long[identifiers.size()];
        int i = 0;
        for (Long identifier : identifiers) {
          _missingInputIdentifiers[i++] = identifier;
        }
      }
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("CalculationJobResultItem for ").append(getItem());
    return sb.toString();
  }

}
