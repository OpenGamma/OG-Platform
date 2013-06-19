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
import java.util.Set;

import org.springframework.util.ObjectUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.opengamma.engine.cache.IdentifierEncodedValueSpecifications;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ExecutionLog;
import com.opengamma.engine.view.ExecutionLogMode;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains details about the result of executing a {@link CalculationJobItem}.
 */
public final class CalculationJobResultItem implements IdentifierEncodedValueSpecifications {

  /*package*/static final String MISSING_INPUTS_FAILURE_CLASS = "com.opengamma.engine.calcnode.MissingInputException";

  /*package*/static final String EXECUTION_SUPPRESSED_CLASS = "com.opengamma.engine.calcnode.ExecutionSuppressedException";

  /*package*/static final String EXECUTION_SUPPRESSED_MESSAGE = "Unable to execute because of function blacklisting entry";

  private Set<ValueSpecification> _missingOutputs;
  private long[] _missingOutputIdentifiers;
  private Set<ValueSpecification> _missingInputs;
  private long[] _missingInputIdentifiers;

  private final ExecutionLog _executionLog;

  public CalculationJobResultItem(Set<ValueSpecification> missingInputs, Set<ValueSpecification> missingOutputs, final ExecutionLog executionLog) {
    if (missingInputs == null) {
      missingInputs = ImmutableSet.<ValueSpecification>of();
    }
    if (missingOutputs == null) {
      missingOutputs = ImmutableSet.<ValueSpecification>of();
    }
    _missingInputs = missingInputs;
    _missingOutputs = missingOutputs;
    _executionLog = executionLog;
  }

  public CalculationJobResultItem(long[] missingInputIdentifiers, long[] missingOutputIdentifiers, ExecutionLog executionLog) {
    _missingInputIdentifiers = missingInputIdentifiers;
    _missingOutputIdentifiers = missingOutputIdentifiers;
    _executionLog = executionLog;
  }

  //-------------------------------------------------------------------------
  /**
   * Returns an immutable result item representing success, containing no additional data.
   * 
   * @return a result item representing success, not null
   */
  public static CalculationJobResultItem success() {
    return new CalculationJobResultItem(ImmutableSet.<ValueSpecification>of(), ImmutableSet.<ValueSpecification>of(), ExecutionLog.EMPTY);
  }

  /**
   * Returns an immutable result item representing a simple failure.
   * 
   * @param exceptionClass the exception class, not null
   * @param exceptionMessage the exception message
   * @return a result item representing a simple failure, not null
   */
  public static CalculationJobResultItem failure(String exceptionClass, String exceptionMessage) {
    ArgumentChecker.notNull(exceptionClass, "exceptionClass");
    return CalculationJobResultItemBuilder
        .of(new MutableExecutionLog(ExecutionLogMode.INDICATORS))
        .withException(exceptionClass, exceptionMessage)
        .toResultItem();
  }

  //-------------------------------------------------------------------------
  public boolean isFailed() {
    return getExecutionLog().hasException();
  }

  public InvocationResult getResult() {
    if (getExecutionLog().hasException()) {
      if (MISSING_INPUTS_FAILURE_CLASS.equals(getExecutionLog().getExceptionClass())) {
        return InvocationResult.MISSING_INPUTS;
      } else if (EXECUTION_SUPPRESSED_CLASS.equals(getExecutionLog().getExceptionClass())) {
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

  public ExecutionLog getExecutionLog() {
    return _executionLog;
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

  @Override
  public int hashCode() {
    int hc = 1;
    hc += (hc << 4) + ObjectUtils.nullSafeHashCode(_missingOutputs);
    hc += (hc << 4) + ObjectUtils.nullSafeHashCode(_missingInputs);
    return hc;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof CalculationJobResultItem)) {
      return false;
    }
    final CalculationJobResultItem other = (CalculationJobResultItem) o;
    return ObjectUtils.nullSafeEquals(other._missingOutputs, _missingOutputs)
        && ObjectUtils.nullSafeEquals(other._missingInputs, _missingInputs)
        && ObjectUtils.nullSafeEquals(other._executionLog, _executionLog);
  }

}
