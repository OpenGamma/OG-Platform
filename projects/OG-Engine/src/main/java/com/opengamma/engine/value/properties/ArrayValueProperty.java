/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.value.properties;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;

import com.google.common.collect.Sets;
import com.opengamma.engine.fudgemsg.ValuePropertiesFudgeBuilder;
import com.opengamma.engine.value.ValueProperties;

/**
 * Internal state used to implement a {@link ValueProperties} entry that has a small number of values.
 */
public final class ArrayValueProperty extends AbstractValueProperty {

  /**
   * The maximum length this should be used for before {@link SetValueProperty} is used instead.
   */
  public static final int MAX_ARRAY_LENGTH = 5;

  private static final long serialVersionUID = 1L;

  private String[] _values;

  // construction

  /**
   * Creates a new instance.
   * 
   * @param key the value key, never null
   * @param optional the optional flag
   * @param values the values to store, never null, containing at least one entry and not more than {@link #MAX_ARRAY_LENGTH}. The object will use this object but won't modify it.
   * @param next the next property in the bucket, or null if this is the end of the chain
   */
  public ArrayValueProperty(final String key, final boolean optional, final String[] values, final AbstractValueProperty next) {
    super(key, optional, next);
    assert (values.length > 1) && (values.length <= MAX_ARRAY_LENGTH);
    _values = values;
  }

  @Override
  public AbstractValueProperty copy(final AbstractValueProperty next) {
    return new ArrayValueProperty(getKey(), isOptional(), _values, next);
  }

  @Override
  protected AbstractValueProperty withOptional(final boolean optional) {
    if (isOptional() == optional) {
      return this;
    } else {
      return new ArrayValueProperty(getKey(), optional, _values, getNext());
    }
  }

  // query/update self

  @Override
  public Set<String> getValues() {
    return new StringArraySet(_values);
  }

  /* package */String[] getValuesImpl() {
    return _values;
  }

  @Override
  protected AbstractValueProperty addValueImpl(final String value) {
    for (String existing : _values) {
      if (value.equals(existing)) {
        return this;
      }
    }
    if (_values.length < MAX_ARRAY_LENGTH) {
      final String[] newValues = Arrays.copyOf(_values, _values.length + 1);
      newValues[_values.length] = value;
      _values = newValues;
      return this;
    } else {
      final Set<String> newValues = Sets.newHashSet(_values);
      newValues.add(value);
      return new SetValueProperty(getKey(), isOptional(), newValues, getNext());
    }
  }

  @Override
  protected AbstractValueProperty addValuesImpl(final String[] values) {
    int newValues = 0;
    newValueLoop: for (String value : values) { //CSIGNORE
      for (String existing : _values) {
        if (value.equals(existing)) {
          continue newValueLoop;
        }
      }
      newValues++;
    }
    if (newValues == 0) {
      return this;
    }
    newValues += _values.length;
    if (newValues <= MAX_ARRAY_LENGTH) {
      final String[] copy = Arrays.copyOf(_values, newValues);
      newValueLoop: for (String value : values) { //CSIGNORE
        for (String existing : _values) {
          if (value.equals(existing)) {
            continue newValueLoop;
          }
        }
        copy[--newValues] = value;
      }
      _values = copy;
      return this;
    } else {
      final Set<String> copy = Sets.newHashSetWithExpectedSize(newValues);
      for (String value : _values) {
        copy.add(value);
      }
      for (String value : values) {
        copy.add(value);
      }
      return new SetValueProperty(getKey(), isOptional(), copy, getNext());
    }
  }

  @Override
  protected AbstractValueProperty addValuesImpl(final Collection<String> values) {
    int newValues = 0;
    newValueLoop: for (String value : values) { //CSIGNORE
      for (String existing : _values) {
        if (value.equals(existing)) {
          continue newValueLoop;
        }
      }
      newValues++;
    }
    if (newValues == 0) {
      return this;
    }
    if (newValues <= MAX_ARRAY_LENGTH) {
      newValues += _values.length;
      final String[] copy = Arrays.copyOf(_values, newValues);
      newValueLoop: for (String value : values) { //CSIGNORE
        for (String existing : _values) {
          if (value.equals(existing)) {
            continue newValueLoop;
          }
        }
        copy[--newValues] = value;
      }
      _values = copy;
      return this;
    } else {
      final Set<String> copy = Sets.newHashSetWithExpectedSize(newValues);
      for (String value : _values) {
        copy.add(value);
      }
      copy.addAll(values);
      return new SetValueProperty(getKey(), isOptional(), copy, getNext());
    }
  }

  @Override
  protected AbstractValueProperty addValuesToImpl(final AbstractValueProperty addTo) {
    return addTo.addValuesImpl(_values);
  }

  @Override
  protected boolean containsValue(final String value) {
    for (String myValue : _values) {
      if (value.equals(myValue)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected boolean containsAllValues(final String[] values) {
    int lo = 0;
    int hi = _values.length - 1;
    for (String value : values) {
      boolean match = false;
      for (int i = lo; i <= hi; i++) {
        if (value.equals(_values[i])) {
          match = true;
          if (i == lo) {
            lo++;
          } else if (i == hi) {
            hi--;
          }
          break;
        }
      }
      if (!match) {
        return false;
      }
    }
    return true;
  }

  @Override
  protected boolean containsAllValues(final Collection<String> values) {
    int lo = 0;
    int hi = _values.length - 1;
    for (String value : values) {
      boolean match = false;
      for (int i = lo; i <= hi; i++) {
        if (value.equals(_values[i])) {
          match = true;
          if (i == lo) {
            lo++;
          } else if (i == hi) {
            hi--;
          }
          break;
        }
      }
      if (!match) {
        return false;
      }
    }
    return true;
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
    return _values[0];
  }

  @Override
  protected AbstractValueProperty setWildcardImpl() {
    return new WildcardValueProperty(getKey(), isOptional(), getNext());
  }

  @Override
  protected boolean isSatisfiedBy(final String value) {
    for (String myValue : _values) {
      if (value.equals(myValue)) {
        return true;
      }
    }
    return false;
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
    final String value = other.getValueImpl();
    for (String myValue : _values) {
      if (value.equals(myValue)) {
        return other.withOptional(isOptional());
      }
    }
    return null;
  }

  @Override
  protected AbstractValueProperty intersectArrayValue(final ArrayValueProperty other) {
    if (other._values.length < _values.length) {
      return other.intersectArrayValue(this);
    }
    outerLoop: for (int i = 0; i < _values.length; i++) { //CSIGNORE
      String si = _values[i];
      for (int j = 0; j < other._values.length; j++) {
        final String sj = other._values[j];
        if (si.equals(sj)) {
          continue outerLoop;
        }
      }
      final String[] result = new String[_values.length - 1];
      int x = i;
      if (i > 0) {
        System.arraycopy(_values, 0, result, 0, i);
      }
      i++;
      while (i < _values.length) {
        si = _values[i];
        for (int j = 0; j < other._values.length; j++) {
          final String sj = other._values[j];
          if (si.equals(sj)) {
            result[x++] = si;
            break;
          }
        }
        i++;
      }
      if (x > 1) {
        if (result.length != x) {
          return new ArrayValueProperty(getKey(), isOptional() && other.isOptional(), Arrays.copyOf(result, x), getNext());
        } else {
          return new ArrayValueProperty(getKey(), isOptional() && other.isOptional(), result, getNext());
        }
      } else if (x == 1) {
        // Single value in intersection
        return new SingletonValueProperty(getKey(), isOptional() && other.isOptional(), result[0], getNext());
      } else {
        // Nothing in intersection
        return null;
      }
    }
    // Everything in this array was present in the other
    return withOptional(other.isOptional());
  }

  @Override
  protected AbstractValueProperty intersectSetValue(final SetValueProperty other) {
    final Set<String> otherValues = other.getValuesImpl();
    for (int i = 0; i < _values.length; i++) { // CSIGNORE
      if (otherValues.contains(_values[i])) {
        continue;
      }
      final String[] result = new String[_values.length - 1];
      int x = i;
      if (i > 0) {
        System.arraycopy(_values, 0, result, 0, i);
      }
      i++;
      while (i < _values.length) {
        final String si = _values[i++];
        if (otherValues.contains(si)) {
          result[x++] = si;
        }
      }
      if (x > 1) {
        if (result.length != x) {
          return new ArrayValueProperty(getKey(), isOptional() && other.isOptional(), Arrays.copyOf(result, x), getNext());
        } else {
          return new ArrayValueProperty(getKey(), isOptional() && other.isOptional(), result, getNext());
        }
      } else if (x == 1) {
        // Single value in intersection
        return new SingletonValueProperty(getKey(), isOptional() && other.isOptional(), result[0], getNext());
      } else {
        // Nothing in intersection
        return null;
      }
    }
    // Everything in this array was present in the set
    return withOptional(other.isOptional());
  }

  @Override
  public AbstractValueProperty intersectValues(final AbstractValueProperty other) {
    return other.intersectArrayValue(this);
  }

  @Override
  public void toFudgeMsg(final MutableFudgeMsg msg) {
    final MutableFudgeMsg subMsg = msg.addSubMessage(getKey(), null);
    if (isOptional()) {
      subMsg.add(ValuePropertiesFudgeBuilder.OPTIONAL_FIELD, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
    }
    for (int i = 0; i < _values.length; i++) {
      subMsg.add(null, i, FudgeWireType.STRING, _values[i]);
    }
  }

  // Object

  @Override
  protected int valueHashCode() {
    // Hash code of a set of these strings
    int hc = 0;
    for (String value : _values) {
      hc += value.hashCode();
    }
    return hc;
  }

  @Override
  protected boolean equalsSingleton(final String value) {
    return false;
  }

  @Override
  protected boolean equalsArray(final String[] values) {
    if (_values.length != values.length) {
      return false;
    }
    return containsAllValues(values);
  }

  @Override
  protected boolean equalsSet(final Set<String> values) {
    return false;
  }

  @Override
  protected boolean equalsValue(final AbstractValueProperty other) {
    return other.equalsArray(_values);
  }

}
