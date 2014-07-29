/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetSpecificationResolver;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * An implementation of {@link FunctionInputs} that stores all inputs in internal maps.
 */
public class FunctionInputsImpl implements FunctionInputs, Serializable {

  private static final long serialVersionUID = 1L;

  private final ComputationTargetSpecificationResolver.AtVersionCorrection _resolver;
  private final Set<ComputedValue> _values;
  private final Map<String, ComputedValue> _valuesByRequirementName = new HashMap<String, ComputedValue>();
  private final Map<Pair<String, Object>, ComputedValue[]> _valuesByRequirement = new HashMap<Pair<String, Object>, ComputedValue[]>();
  private final Collection<ValueSpecification> _missingValues;

  public FunctionInputsImpl(final ComputationTargetSpecificationResolver.AtVersionCorrection resolver, final ComputedValue value) {
    _resolver = resolver;
    _missingValues = Collections.emptySet();
    _values = new HashSet<ComputedValue>();
    addValue(value);
  }

  public FunctionInputsImpl(final ComputationTargetSpecificationResolver.AtVersionCorrection resolver, final Collection<? extends ComputedValue> values) {
    this(resolver, values, Collections.<ValueSpecification>emptySet());
  }

  public FunctionInputsImpl(final ComputationTargetSpecificationResolver.AtVersionCorrection resolver, final Collection<? extends ComputedValue> values,
      final Collection<ValueSpecification> missingValues) {
    _resolver = resolver;
    _missingValues = missingValues;
    _values = Sets.newHashSetWithExpectedSize(values.size());
    for (final ComputedValue value : values) {
      addValue(value);
    }
  }

  private void targetRefKey(final ComputationTargetReference targetRef, final List<UniqueId> uids) {
    if (targetRef.getParent() != null) {
      targetRefKey(targetRef.getParent(), uids);
    }
    uids.add(_resolver.getTargetSpecification(targetRef).getUniqueId());
  }

  private Object targetSpecKey(final ComputationTargetSpecification targetSpec) {
    if (targetSpec.getParent() == null) {
      return targetSpec.getUniqueId();
    } else {
      final List<UniqueId> uids = new ArrayList<UniqueId>();
      targetRefKey(targetSpec.getParent(), uids);
      uids.add(targetSpec.getUniqueId());
      return uids;
    }
  }

  private void addValue(final ComputedValue value) {
    ArgumentChecker.notNull(value, "Computed Value");
    if (value.getValue() instanceof ComputedValue) {
      throw new IllegalArgumentException("Double-nested value");
    }
    _values.add(value);
    _valuesByRequirementName.put(value.getSpecification().getValueName(), value);
    final Pair<String, Object> key = Pairs.of(value.getSpecification().getValueName(), targetSpecKey(value.getSpecification().getTargetSpecification()));
    final ComputedValue[] prev = _valuesByRequirement.get(key);
    if (prev == null) {
      _valuesByRequirement.put(key, new ComputedValue[] {value });
    } else {
      final ComputedValue[] values = new ComputedValue[prev.length + 1];
      System.arraycopy(prev, 0, values, 0, prev.length);
      values[prev.length] = value;
      _valuesByRequirement.put(key, values);
    }
  }

  @Override
  public Collection<ComputedValue> getAllValues() {
    return Collections.unmodifiableSet(_values);
  }

  @Override
  public Object getValue(final ValueRequirement requirement) {
    final ComputedValue cv = getComputedValue(requirement);
    if (cv != null) {
      return cv.getValue();
    }
    return null;
  }

  @Override
  public ComputedValue getComputedValue(final ValueRequirement requirement) {
    final Pair<String, Object> key = Pairs.of(requirement.getValueName(), targetSpecKey(_resolver.getTargetSpecification(requirement.getTargetReference())));
    final ComputedValue[] values = _valuesByRequirement.get(key);
    if (values != null) {
      for (final ComputedValue value : values) {
        // Shortcut to check the properties as we already know the name and target match
        if (requirement.getConstraints().isSatisfiedBy(value.getSpecification().getProperties())) {
          return value;
        }
      }
    }
    return null;
  }

  @Override
  public Object getValue(final String requirementName) {
    final ComputedValue computedValue = getComputedValue(requirementName);
    return computedValue != null ? computedValue.getValue() : null;
  }

  @Override
  public ComputedValue getComputedValue(final String requirementName) {
    return _valuesByRequirementName.get(requirementName);
  }

  @Override
  public Collection<ValueSpecification> getMissingValues() {
    return Collections.unmodifiableCollection(_missingValues);
  }

  @Override
  public String toString() {
    return "Values = " + _values + ", Missing = " + _missingValues;
  }

}
