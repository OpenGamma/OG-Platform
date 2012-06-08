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
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.IdentifierMap;

/**
 * 
 */
public final class CalculationJobResultItem {

  private static final String MISSING_INPUTS_FAILURE_CLASS = "com.opengamma.engine.view.calcnode.MissingInputException";
  private static final CalculationJobResultItem SUCCESS = new CalculationJobResultItem(null, null, null, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet());

  private final String _exceptionClass;
  private final String _exceptionMsg;
  private final String _stackTrace;

  private Set<ValueSpecification> _missingOutputs;
  private long[] _missingOutputIdentifiers;
  private Set<ValueSpecification> _missingInputs;
  private long[] _missingInputIdentifiers;

  private CalculationJobResultItem(final Throwable exception, final Set<ValueSpecification> missingInputs, final Set<ValueSpecification> missingOutputs) {
    _exceptionClass = exception.getClass().getName();
    _exceptionMsg = exception.getMessage();
    final StringBuffer buffer = new StringBuffer();
    for (StackTraceElement element : exception.getStackTrace()) {
      buffer.append(element.toString() + "\n");
    }
    _stackTrace = buffer.toString();
    _missingInputs = missingInputs;
    _missingOutputs = missingOutputs;
  }

  public static CalculationJobResultItem success() {
    return SUCCESS;
  }

  public static CalculationJobResultItem failure(final String errorClass, final String errorMessage) {
    return new CalculationJobResultItem(errorClass, errorMessage, null, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet());
  }

  public static CalculationJobResultItem failure(final Throwable exception) {
    return new CalculationJobResultItem(exception, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet());
  }

  public static CalculationJobResultItem missingInputs(final Set<ValueSpecification> missingInputs) {
    return new CalculationJobResultItem(MISSING_INPUTS_FAILURE_CLASS, "Unable to execute because of " + missingInputs.size() + " missing input(s)", null, missingInputs,
        Collections.<ValueSpecification>emptySet());
  }

  public static CalculationJobResultItem partialInputs(final Set<ValueSpecification> missingInputs) {
    return new CalculationJobResultItem(null, null, null, missingInputs, Collections.<ValueSpecification>emptySet());
  }

  public CalculationJobResultItem withMissingOutputs(final Set<ValueSpecification> missingOutputs) {
    return new CalculationJobResultItem(null, null, null, _missingInputs, missingOutputs);
  }

  public CalculationJobResultItem withFailure(final String errorClass, final String errorMessage) {
    return new CalculationJobResultItem(errorClass, errorMessage, null, _missingInputs, _missingOutputs);
  }

  public CalculationJobResultItem withFailure(final Throwable exception) {
    return new CalculationJobResultItem(exception, _missingInputs, _missingOutputs);
  }

  public CalculationJobResultItem(String exceptionClass, String exceptionMsg, String stackTrace, Set<ValueSpecification> missingInputs, Set<ValueSpecification> missingOutputs) {
    _exceptionClass = exceptionClass;
    _exceptionMsg = exceptionMsg;
    _stackTrace = stackTrace;
    _missingInputs = missingInputs;
    _missingOutputs = missingOutputs;
  }

  public CalculationJobResultItem(String exceptionClass, String exceptionMsg, String stackTrace, long[] missingInputIdentifiers, long[] missingOutputIdentifiers) {
    _exceptionClass = exceptionClass;
    _exceptionMsg = exceptionMsg;
    _stackTrace = stackTrace;
    _missingInputIdentifiers = missingInputIdentifiers;
    _missingOutputIdentifiers = missingOutputIdentifiers;
  }

  public boolean failed() {
    return _exceptionClass != null;
  }

  public InvocationResult getResult() {
    if (_exceptionClass != null) {
      if (MISSING_INPUTS_FAILURE_CLASS.equals(_exceptionClass)) {
        return InvocationResult.MISSING_INPUTS;
      } else {
        return InvocationResult.FUNCTION_THREW_EXCEPTION;
      }
    } else {
      if (_missingOutputs.isEmpty()) {
        return InvocationResult.SUCCESS;
      } else {
        return InvocationResult.PARTIAL_SUCCESS;
      }
    }
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

  public Set<ValueSpecification> getMissingOutputs() {
    return Collections.unmodifiableSet(_missingOutputs);
  }

  public long[] getMissingOutputIdentifiers() {
    return _missingOutputIdentifiers;
  }

  /**
   * Numeric identifiers may have been passed when this was encoded as a Fudge message. This will resolve
   * them to full {@link ValueSpecification} objects.
   * 
   * @param identifierMap Identifier map to resolve the inputs with
   */
  public void resolveIdentifiers(final IdentifierMap identifierMap) {
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
    if (_missingOutputs == null) {
      if (_missingOutputIdentifiers == null) {
        _missingOutputs = Collections.emptySet();
      } else if (_missingOutputIdentifiers.length == 1) {
        _missingOutputs = Collections.singleton(identifierMap.getValueSpecification(_missingOutputIdentifiers[0]));
      } else {
        _missingOutputs = Sets.newHashSetWithExpectedSize(_missingOutputIdentifiers.length);
        final Collection<Long> identifiers = new ArrayList<Long>(_missingOutputIdentifiers.length);
        for (long identifier : _missingOutputIdentifiers) {
          identifiers.add(identifier);
        }
        _missingOutputs.addAll(identifierMap.getValueSpecifications(identifiers).values());
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
    if ((_missingOutputIdentifiers == null) && !_missingOutputs.isEmpty()) {
      if (_missingOutputs.size() == 1) {
        _missingOutputIdentifiers = new long[] {identifierMap.getIdentifier(_missingOutputs.iterator().next()) };
      } else {
        final Collection<Long> identifiers = identifierMap.getIdentifiers(_missingOutputs).values();
        _missingOutputIdentifiers = new long[identifiers.size()];
        int i = 0;
        for (Long identifier : identifiers) {
          _missingOutputIdentifiers[i++] = identifier;
        }
      }
    }
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("CalculationJobResultItem-").append(getResult());
    return sb.toString();
  }

}
