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
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.cache.IdentifierEncodedValueSpecifications;

/**
 * 
 */
public final class CalculationJobResultItem implements IdentifierEncodedValueSpecifications {

  private static final String MISSING_INPUTS_FAILURE_CLASS = "com.opengamma.engine.view.calcnode.MissingInputException";
  private static final String EXECUTION_SUPPRESSED_CLASS = "com.opengamma.engine.view.calcnode.ExecutionSuppressedException";

  private static final CalculationJobResultItem SUCCESS = new CalculationJobResultItem(null, null, null, Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet());
  private static final CalculationJobResultItem SUPPRESSED = new CalculationJobResultItem(EXECUTION_SUPPRESSED_CLASS, "Unable to execute because of function blacklisting entry", null,
      Collections.<ValueSpecification>emptySet(), Collections.<ValueSpecification>emptySet());

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

  public static CalculationJobResultItem suppressed() {
    return SUPPRESSED;
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

  public boolean isFailed() {
    return _exceptionClass != null;
  }

  public InvocationResult getResult() {
    if (_exceptionClass != null) {
      if (MISSING_INPUTS_FAILURE_CLASS.equals(_exceptionClass)) {
        return InvocationResult.MISSING_INPUTS;
      } else if (EXECUTION_SUPPRESSED_CLASS.equals(_exceptionClass)) {
        return InvocationResult.SUPPRESSED;
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

  @Override
  public void convertIdentifiers(final Long2ObjectMap<ValueSpecification> identifiers) {
    if (_missingInputs == null) {
      if (_missingInputIdentifiers == null) {
        _missingInputs = Collections.emptySet();
      } else {
        _missingInputs = Sets.newHashSetWithExpectedSize(_missingInputIdentifiers.length);
        for (long identifier : _missingInputIdentifiers) {
          _missingInputs.add(identifiers.get(identifier));
        }
      }
    }
    if (_missingOutputs == null) {
      if (_missingOutputIdentifiers == null) {
        _missingOutputs = Collections.emptySet();
      } else {
        _missingOutputs = Sets.newHashSetWithExpectedSize(_missingOutputIdentifiers.length);
        for (long identifier : _missingOutputIdentifiers) {
          _missingOutputs.add(identifiers.get(identifier));
        }
      }
    }
  }

  @Override
  public void collectIdentifiers(final LongSet identifiers) {
    if (_missingInputIdentifiers != null) {
      for (long identifier : _missingInputIdentifiers) {
        identifiers.add(identifier);
      }
    }
    if (_missingOutputIdentifiers != null) {
      for (long identifier : _missingOutputIdentifiers) {
        identifiers.add(identifier);
      }
    }
  }

  @Override
  public void convertValueSpecifications(final Object2LongMap<ValueSpecification> valueSpecifications) {
    if ((_missingInputIdentifiers == null) && !_missingInputs.isEmpty()) {
      _missingInputIdentifiers = new long[_missingInputs.size()];
      int i = 0;
      for (ValueSpecification input : _missingInputs) {
        _missingInputIdentifiers[i++] = valueSpecifications.getLong(input);
      }
    }
    if ((_missingOutputIdentifiers == null) && !_missingOutputs.isEmpty()) {
      _missingOutputIdentifiers = new long[_missingOutputs.size()];
      int i = 0;
      for (ValueSpecification output : _missingOutputs) {
        _missingOutputIdentifiers[i++] = valueSpecifications.getLong(output);
      }
    }
  }

  @Override
  public void collectValueSpecifications(final Set<ValueSpecification> valueSpecifications) {
    valueSpecifications.addAll(_missingInputs);
    valueSpecifications.addAll(_missingOutputs);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("CalculationJobResultItem-").append(getResult());
    return sb.toString();
  }

}
