/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.io.Serializable;
import java.math.BigDecimal;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;


/**
 * A base class for other implementations of {@link AnalyticValue} that
 * stores the definition and value.
 *
 * @author kirk
 */
public class AnalyticValueImpl<T> implements AnalyticValue<T>, Serializable, Cloneable {
  private final AnalyticValueDefinition<T> _definition;
  private final T _value;
  
  public AnalyticValueImpl(AnalyticValueDefinition<T> definition, T value) {
    if(definition == null) {
      throw new NullPointerException("Must specify an Analytic Value Definition");
    }
    if(value == null) {
      throw new NullPointerException("Must specify a value.");
    }
    _definition = definition;
    _value = value;
  }

  @Override
  public AnalyticValueDefinition<T> getDefinition() {
    return _definition;
  }

  @Override
  public T getValue() {
    return _value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public AnalyticValueImpl<T> clone() {
    try {
      return (AnalyticValueImpl<T>) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException("Yes, it is supported.");
    }
  }

  @Override
  public boolean equals(Object obj) {
    if(this == obj) {
      return true;
    }
    if(obj == null) {
      return false;
    }
    if(!getClass().equals(obj.getClass())) {
      return false;
    }
    AnalyticValueImpl<?> other = (AnalyticValueImpl<?>) obj;
    if(!ObjectUtils.equals(getValue(), other.getValue())) {
      return false;
    }
    if(!ObjectUtils.equals(getDefinition(), other.getDefinition())) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getDefinition().hashCode();
    result = prime * result + getValue().hashCode();
    return result;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

  @Override
  public AnalyticValue<T> scaleForPosition(BigDecimal quantity) {
    return this;
  }

}
