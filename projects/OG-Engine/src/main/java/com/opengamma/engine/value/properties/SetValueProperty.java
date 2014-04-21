/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value.properties;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.google.common.collect.Sets;
import com.opengamma.engine.fudgemsg.ValuePropertiesFudgeBuilder;
import com.opengamma.engine.value.ValueProperties;

/**
 * Internal state used to implement a {@link ValueProperties} entry that has more than a small number of values.
 */
public final class SetValueProperty extends AbstractValueProperty {

  private static final long serialVersionUID = 1L;

  private Set<String> _values;

  // construction

  /**
   * Creates a new instance.
   * 
   * @param key the value key, never null
   * @param optional the optional flag
   * @param values the values to store, never null, containing more than {@link ArrayValueProperty#MAX_ARRAY_LENGTH} entries. The object will use this object but will not modify it.
   * @param next the next property in the bucket, or null if this is the end of the chain
   */
  public SetValueProperty(final String key, final boolean optional, final Set<String> values, final AbstractValueProperty next) {
    super(key, optional, next);
    assert values.size() > ArrayValueProperty.MAX_ARRAY_LENGTH;
    _values = values;
  }

  @Override
  public AbstractValueProperty copy(final AbstractValueProperty next) {
    return new SetValueProperty(getKey(), isOptional(), _values, next);
  }

  @Override
  public AbstractValueProperty withOptional(final boolean optional) {
    if (isOptional() == optional) {
      return this;
    } else {
      return new SetValueProperty(getKey(), optional, _values, getNext());
    }
  }

  // query/update self

  @Override
  public Set<String> getValues() {
    return Collections.unmodifiableSet(_values);
  }

  /* package */Set<String> getValuesImpl() {
    return _values;
  }

  @Override
  protected AbstractValueProperty addValueImpl(final String value) {
    if (!_values.contains(value)) {
      final Set<String> newValues = new HashSet<String>(_values);
      newValues.add(value);
      _values = newValues;
    }
    return this;
  }

  @Override
  protected AbstractValueProperty addValuesImpl(final String[] values) {
    int newValues = 0;
    for (String value : values) {
      if (!_values.contains(value)) {
        newValues++;
      }
    }
    if (newValues > 0) {
      final Set<String> copy = Sets.newHashSetWithExpectedSize(_values.size() + newValues);
      copy.addAll(_values);
      for (String value : values) {
        copy.add(value);
      }
      _values = copy;
    }
    return this;
  }

  @Override
  protected AbstractValueProperty addValuesImpl(final Collection<String> values) {
    int newValues = 0;
    for (String value : values) {
      if (!_values.contains(value)) {
        newValues++;
      }
    }
    if (newValues > 0) {
      final Set<String> copy = Sets.newHashSetWithExpectedSize(_values.size() + newValues);
      copy.addAll(_values);
      copy.addAll(values);
      _values = copy;
    }
    return this;
  }

  @Override
  protected AbstractValueProperty addValuesToImpl(final AbstractValueProperty addTo) {
    return addTo.addValuesImpl(_values);
  }

  @Override
  protected boolean containsValue(final String value) {
    return _values.contains(value);
  }

  @Override
  protected boolean containsAllValues(final String[] values) {
    for (String value : values) {
      if (!_values.contains(value)) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected boolean containsAllValues(final Collection<String> values) {
    return _values.containsAll(values);
  }

  @Override
  protected boolean valuesContainedBy(final AbstractValueProperty other) {
    return other.containsAllValues(_values);
  }

  @Override
  public boolean isWildcard() {
    return false;
  }

  @Override
  public String getStrict() {
    return null;
  }

  @Override
  public String getSingle() {
    return _values.iterator().next();
  }

  @Override
  protected AbstractValueProperty setWildcardImpl() {
    return new WildcardValueProperty(getKey(), isOptional(), getNext());
  }

  @Override
  protected boolean isSatisfiedBy(final String value) {
    return _values.contains(value);
  }

  @Override
  public boolean isSatisfyValue(final AbstractValueProperty property) {
    for (String myValue : _values) {
      if (property.isSatisfiedBy(myValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected AbstractValueProperty intersectSingletonValue(final SingletonValueProperty other) {
    if (_values.contains(other.getValueImpl())) {
      return other.withOptional(isOptional());
    }
    return null;
  }

  @Override
  protected AbstractValueProperty intersectArrayValue(final ArrayValueProperty other) {
    return other.intersectSetValue(this);
  }

  @Override
  protected AbstractValueProperty intersectSetValue(final SetValueProperty other) {
    final Set<String> otherValues = other.getValuesImpl();
    final Set<String> intersect = new HashSet<String>(_values);
    intersect.retainAll(otherValues);
    if (intersect.isEmpty()) {
      return null;
    }
    final int size = intersect.size();
    if (size == 1) {
      // Single value in intersection
      return new SingletonValueProperty(getKey(), isOptional() && other.isOptional(), intersect.iterator().next(), getNext());
    } else if (size <= ArrayValueProperty.MAX_ARRAY_LENGTH) {
      // Small array in intersection
      return new ArrayValueProperty(getKey(), isOptional() && other.isOptional(), intersect.toArray(new String[size]), getNext());
    } else if (intersect.isEmpty()) {
      // No intersection
      return null;
    } else if (size == _values.size()) {
      // Intersection same as self
      return withOptional(other.isOptional());
    } else if (size == otherValues.size()) {
      // Intersection same as other
      return other.withOptional(isOptional());
    } else {
      // New set intersection
      return new SetValueProperty(getKey(), isOptional() && other.isOptional(), intersect, null);
    }
  }

  @Override
  public AbstractValueProperty intersectValues(final AbstractValueProperty other) {
    return other.intersectSetValue(this);
  }

  @Override
  public void toFudgeMsg(final MutableFudgeMsg msg) {
    final MutableFudgeMsg subMsg = msg.addSubMessage(getKey(), null);
    if (isOptional()) {
      subMsg.add(ValuePropertiesFudgeBuilder.OPTIONAL_FIELD, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
    }
    int ordinal = 0;
    for (String value : _values) {
      subMsg.add(null, ordinal++, FudgeWireType.STRING, value);
    }
  }

  // Object

  @Override
  protected int valueHashCode() {
    return _values.hashCode();
  }

  @Override
  protected boolean equalsSingleton(final String value) {
    return false;
  }

  @Override
  protected boolean equalsArray(final String[] values) {
    return false;
  }

  @Override
  protected boolean equalsSet(final Set<String> values) {
    return _values.equals(values);
  }

  @Override
  protected boolean equalsValue(final AbstractValueProperty other) {
    return other.equalsSet(_values);
  }

}
