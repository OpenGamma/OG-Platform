/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

// REVIEW kirk 2009-09-18 -- Should this be an interface? I really don't see
// the point of it at the moment, which is why I didn't make it one.
/**
 * A wrapper for the instances of {@link AnalyticValue}
 * that are passed to an {@link AnalyticFunction}'s {@code execute} methods.
 *
 * @author kirk
 */
public class AnalyticFunctionInputs implements Serializable {
  // TODO kirk 2009-09-18 -- This is horrifically inefficient, but I'm not 100%
  // sure that that needs fixing; linear searches may actually be better than
  // building up a lot of indices. Needs to be investigated.
  private final Collection<AnalyticValue> _values = new ArrayList<AnalyticValue>();
  
  // REVIEW kirk 2009-09-18 -- Should we have a non-copying constructor? We usually
  // build up a list already, so I'm not sure why we're copying here except
  // as generic style.
  public AnalyticFunctionInputs(Collection<AnalyticValue> values) {
    if(values == null) {
      return;
    }
    _values.addAll(values);
  }
  
  public Collection<AnalyticValue> getAllValues() {
    return Collections.unmodifiableCollection(_values);
  }

  @SuppressWarnings("unchecked")
  public <T> T getValue(Class<T> valueObjectClass) {
    if(valueObjectClass == null) {
      return null;
    }
    for(AnalyticValue value : _values) {
      if(valueObjectClass.isAssignableFrom(value.getValue().getClass())) {
        return (T)value.getValue();
      }
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public <T> Collection<T> getValues(Class<T> valueObjectClass) {
    if(valueObjectClass == null) {
      return Collections.emptySet();
    }
    List<T> result = new ArrayList<T>();
    for(AnalyticValue value : _values) {
      if(valueObjectClass.isAssignableFrom(value.getValue().getClass())) {
        result.add((T)value.getValue());
      }
    }
    return result;
  }
  
  public Object getValue(String definitionKey, Object definitionValue) {
    if(definitionKey == null) {
      return null;
    }
    for(AnalyticValue value : _values) {
      if(ObjectUtils.equals(value.getDefinition().getValue(definitionKey), definitionValue)) {
        return value.getValue();
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  public List<?> getValues(String definitionKey, Object definitionValue) {
    if(definitionKey == null) {
      return null;
    }
    List result = new ArrayList();
    for(AnalyticValue value : _values) {
      if(ObjectUtils.equals(value.getDefinition().getValue(definitionKey), definitionValue)) {
        result.add(value.getValue());
      }
    }
    return result;
  }
}
