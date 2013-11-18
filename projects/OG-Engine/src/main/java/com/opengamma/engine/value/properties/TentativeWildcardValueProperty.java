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
 * Internal state used to implement a {@link ValueProperties} entry that is a wild-card in order for {@link ValueProperties.Builder#withOptional} to work.
 */
public final class TentativeWildcardValueProperty extends AbstractValueProperty {

  private static final long serialVersionUID = 1L;

  // construction

  /**
   * Creates a new instance.
   * 
   * @param key the value key, never null
   * @param next the next property in the bucket, or null if this is the end of the chain
   */
  public TentativeWildcardValueProperty(final String key, final AbstractValueProperty next) {
    super(key, true, next);
  }

  @Override
  public AbstractValueProperty copy(final AbstractValueProperty next) {
    return new TentativeWildcardValueProperty(getKey(), next);
  }

  @Override
  protected AbstractValueProperty withOptional(final boolean optional) {
    if (isOptional() == optional) {
      return this;
    } else {
      return new WildcardValueProperty(getKey(), optional, getNext());
    }
  }

  // query

  @Override
  public Set<String> getValues() {
    return Collections.emptySet();
  }

  @Override
  protected AbstractValueProperty addValueImpl(final String value) {
    return new SingletonValueProperty(getKey(), isOptional(), value, getNext());
  }

  private AbstractValueProperty createFromSet(final Set<String> values) {
    final int size = values.size();
    if (size == 1) {
      return new SingletonValueProperty(getKey(), isOptional(), values.iterator().next(), getNext());
    } else if (size <= ArrayValueProperty.MAX_ARRAY_LENGTH) {
      return new ArrayValueProperty(getKey(), isOptional(), values.toArray(new String[size]), getNext());
    } else {
      return new SetValueProperty(getKey(), isOptional(), values, getNext());
    }
  }

  @Override
  protected AbstractValueProperty addValuesImpl(final String[] values) {
    if (values.length == 1) {
      return new SingletonValueProperty(getKey(), isOptional(), values[0], getNext());
    } else if (values.length <= ArrayValueProperty.MAX_ARRAY_LENGTH) {
      return new ArrayValueProperty(getKey(), isOptional(), values, getNext());
    } else {
      final Set<String> addValues = Sets.newHashSetWithExpectedSize(values.length);
      for (String value : values) {
        addValues.add(value);
      }
      return createFromSet(addValues);
    }
  }

  @Override
  protected AbstractValueProperty addValuesImpl(final Collection<String> values) {
    final Set<String> addValues = new HashSet<String>(values);
    return createFromSet(addValues);
  }

  @Override
  protected AbstractValueProperty addValuesToImpl(final AbstractValueProperty addTo) {
    return addTo.setWildcardImpl();
  }

  @Override
  protected boolean containsValue(final String value) {
    return true;
  }

  @Override
  protected boolean containsAllValues(final String[] values) {
    return true;
  }

  @Override
  protected boolean containsAllValues(final Collection<String> values) {
    return true;
  }

  @Override
  protected boolean valuesContainedBy(final AbstractValueProperty other) {
    return other.isWildcard();
  }

  @Override
  public boolean isWildcard() {
    return true;
  }

  @Override
  public String getStrict() {
    return null;
  }

  @Override
  public String getSingle() {
    return null;
  }

  @Override
  protected AbstractValueProperty setWildcardImpl() {
    return this;
  }

  @Override
  protected boolean isSatisfiedBy(final String value) {
    return true;
  }

  @Override
  public boolean isSatisfyValue(final AbstractValueProperty property) {
    return true;
  }

  @Override
  protected AbstractValueProperty intersectSingletonValue(final SingletonValueProperty other) {
    // Intersection with wild-card is always the other set
    return other.withOptional(other.isOptional() && isOptional());
  }

  @Override
  protected AbstractValueProperty intersectArrayValue(final ArrayValueProperty other) {
    // Intersection with wild-card is always the other set
    return other.withOptional(other.isOptional() && isOptional());
  }

  @Override
  protected AbstractValueProperty intersectSetValue(final SetValueProperty other) {
    // Intersection with wild-card is always the other set
    return other.withOptional(other.isOptional() && isOptional());
  }

  @Override
  public AbstractValueProperty intersectValues(final AbstractValueProperty other) {
    // Intersection with wild-card is always the other set
    return other.withOptional(other.isOptional() && isOptional());
  }

  @Override
  public void toFudgeMsg(final MutableFudgeMsg msg) {
    final MutableFudgeMsg subMsg = msg.addSubMessage(getKey(), null);
    if (isOptional()) {
      subMsg.add(ValuePropertiesFudgeBuilder.OPTIONAL_FIELD, null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
    }
  }

  // Object

  @Override
  protected int valueHashCode() {
    // Hash code of an empty set
    return 0;
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
    return false;
  }

  @Override
  protected boolean equalsValue(final AbstractValueProperty other) {
    return other.isWildcard();
  }

}
