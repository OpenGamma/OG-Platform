/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value.properties;

import java.util.Arrays;
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
 * Internal state used to implement a {@link ValueProperties} entry which has a single value.
 */
public final class SingletonValueProperty extends AbstractValueProperty {

  private static final long serialVersionUID = 1L;

  private final String _value;

  // construction

  public SingletonValueProperty(final String key, final boolean optional, final String value, final AbstractValueProperty next) {
    super(key, optional, next);
    _value = value;
  }

  @Override
  public AbstractValueProperty copy(final AbstractValueProperty next) {
    return new SingletonValueProperty(getKey(), isOptional(), _value, next);
  }

  @Override
  protected AbstractValueProperty withOptional(final boolean optional) {
    if (optional == isOptional()) {
      return this;
    } else {
      return new SingletonValueProperty(getKey(), optional, _value, getNext());
    }
  }

  // query/update self

  @Override
  public Set<String> getValues() {
    return Collections.singleton(_value);
  }

  /* package */String getValueImpl() {
    return _value;
  }

  @Override
  protected AbstractValueProperty addValueImpl(final String value) {
    if (_value.equals(value)) {
      return this;
    } else {
      return new ArrayValueProperty(getKey(), isOptional(), new String[] {_value, value }, getNext());
    }
  }

  @Override
  protected AbstractValueProperty addValuesImpl(final String[] values) {
    for (String value : values) {
      if (_value.equals(value)) {
        if (values.length > ArrayValueProperty.MAX_ARRAY_LENGTH) {
          return new SetValueProperty(getKey(), isOptional(), Sets.newHashSet(values), getNext());
        } else {
          return new ArrayValueProperty(getKey(), isOptional(), Arrays.copyOf(values, values.length), getNext());
        }
      }
    }
    if (values.length > ArrayValueProperty.MAX_ARRAY_LENGTH - 1) {
      final Set<String> copy = Sets.newHashSet(values);
      copy.add(_value);
      return new SetValueProperty(getKey(), isOptional(), copy, getNext());
    } else {
      final String[] copy = Arrays.copyOf(values, values.length + 1);
      copy[values.length] = _value;
      return new ArrayValueProperty(getKey(), isOptional(), copy, getNext());
    }
  }

  @Override
  protected AbstractValueProperty addValuesImpl(final Collection<String> values) {
    final int size = values.size();
    if (values.contains(_value)) {
      if (size == 1) {
        return this;
      } else if (size <= ArrayValueProperty.MAX_ARRAY_LENGTH) {
        return new ArrayValueProperty(getKey(), isOptional(), values.toArray(new String[size]), getNext());
      } else {
        return new SetValueProperty(getKey(), isOptional(), new HashSet<String>(values), getNext());
      }
    }
    if (size > ArrayValueProperty.MAX_ARRAY_LENGTH - 1) {
      final Set<String> copy = new HashSet<String>(values);
      copy.add(_value);
      return new SetValueProperty(getKey(), isOptional(), copy, getNext());
    } else {
      final String[] copy = values.toArray(new String[size + 1]);
      copy[size] = _value;
      return new ArrayValueProperty(getKey(), isOptional(), copy, getNext());
    }
  }

  @Override
  protected AbstractValueProperty addValuesToImpl(final AbstractValueProperty addTo) {
    return addTo.addValueImpl(_value);
  }

  @Override
  protected boolean containsValue(final String value) {
    return _value.equals(value);
  }

  @Override
  protected boolean containsAllValues(final String[] values) {
    return false;
  }

  @Override
  protected boolean containsAllValues(final Collection<String> values) {
    return false;
  }

  @Override
  protected boolean valuesContainedBy(final AbstractValueProperty other) {
    return other.containsValue(_value);
  }

  @Override
  public boolean isWildcard() {
    return false;
  }

  @Override
  public String getStrict() {
    return _value;
  }

  @Override
  public String getSingle() {
    return _value;
  }

  @Override
  protected AbstractValueProperty setWildcardImpl() {
    return new WildcardValueProperty(getKey(), isOptional(), getNext());
  }

  @Override
  protected boolean isSatisfiedBy(final String value) {
    return _value.equals(value);
  }

  @Override
  public boolean isSatisfyValue(final AbstractValueProperty property) {
    return property.isSatisfiedBy(_value);
  }

  @Override
  protected AbstractValueProperty intersectSingletonValue(final SingletonValueProperty other) {
    if (_value.equals(other.getValueImpl())) {
      return withOptional(other.isOptional());
    } else {
      return null;
    }
  }

  @Override
  protected AbstractValueProperty intersectArrayValue(final ArrayValueProperty other) {
    for (String value : other.getValuesImpl()) {
      if (_value.equals(value)) {
        return withOptional(other.isOptional());
      }
    }
    return null;
  }

  @Override
  protected AbstractValueProperty intersectSetValue(final SetValueProperty other) {
    if (other.getValuesImpl().contains(_value)) {
      return withOptional(other.isOptional());
    }
    return null;
  }

  @Override
  public AbstractValueProperty intersectValues(final AbstractValueProperty other) {
    return other.intersectSingletonValue(this);
  }

  @Override
  public void toFudgeMsg(final MutableFudgeMsg msg) {
    if (isOptional()) {
      final MutableFudgeMsg subMsg = msg.addSubMessage(getKey(), null);
      subMsg.add(ValuePropertiesFudgeBuilder.OPTIONAL_FIELD, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      subMsg.add(null, 0, FudgeWireType.STRING, _value);
    } else {
      msg.add(getKey(), null, FudgeWireType.STRING, _value);
    }
  }

  // Object

  @Override
  protected int valueHashCode() {
    // Hash code of a singleton set
    return _value.hashCode();
  }

  @Override
  protected boolean equalsSingleton(final String value) {
    return _value.equals(value);
  }

  @Override
  protected boolean equalsArray(final String[] values) {
    return false;
  }

  @Override
  protected boolean equalsSet(final Set<String> values) {
    return false;
  }

  @Override
  protected boolean equalsValue(final AbstractValueProperty other) {
    return other.equalsSingleton(_value);
  }

}
