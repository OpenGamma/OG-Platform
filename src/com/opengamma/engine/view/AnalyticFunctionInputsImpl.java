/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.analytics.AnalyticFunctionDefinition;
import com.opengamma.engine.analytics.AnalyticFunctionInputs;
import com.opengamma.engine.analytics.AnalyticValue;
import com.opengamma.engine.analytics.AnalyticValueDefinition;
import com.opengamma.engine.analytics.AnalyticValueDefinitionComparator;

/**
 * A wrapper for the instances of {@link AnalyticValue}
 * that are passed to an {@link AnalyticFunctionDefinition}'s {@code execute} methods.
 *
 * @author kirk
 */
public class AnalyticFunctionInputsImpl implements Serializable, AnalyticFunctionInputs {
  // TODO kirk 2009-09-18 -- This is horrifically inefficient, but I'm not 100%
  // sure that that needs fixing; linear searches may actually be better than
  // building up a lot of indices. Needs to be investigated.
  private final Collection<AnalyticValue<?>> _values = new ArrayList<AnalyticValue<?>>();
  
  // REVIEW kirk 2009-09-18 -- Should we have a non-copying constructor? We usually
  // build up a list already, so I'm not sure why we're copying here except
  // as generic style.
  public AnalyticFunctionInputsImpl(Collection<AnalyticValue<?>> values) {
    for(AnalyticValue<?> analyticValue : values) {
      if(analyticValue != null) {
        _values.add(analyticValue);
      }
    }
  }
  
  public Collection<AnalyticValue<?>> getAllValues() {
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
    for(AnalyticValue<?> value : _values) {
      if(ObjectUtils.equals(value.getDefinition().getValue(definitionKey), definitionValue)) {
        return value.getValue();
      }
    }
    return null;
  }
  
  // REVIEW jim 18-Sep-09 -- Very yucky use of varargs, but nice to use... 
  public Object getValue(String definitionKey, Object definitionValue, Object... params) {
    if(definitionKey == null || params.length % 2 != 0) {
      return null;
    }
    for(AnalyticValue<?> value : _values) {
      outer:
      if(ObjectUtils.equals(value.getDefinition().getValue(definitionKey), definitionValue)) {
        for (int i=0; i<params.length; i+=2) {
          String iDefinitionKey = (String)params[i];
          Object iDefinitionValue = params[i+1];
          if (!ObjectUtils.equals(value.getDefinition().getValue(iDefinitionKey), iDefinitionValue)) {
            break outer;
          }
        }
        return value.getValue();
      }
    }
    return null;    
  }

  public List<?> getValues(String definitionKey, Object definitionValue) {
    if(definitionKey == null) {
      return null;
    }
    List<Object> result = new ArrayList<Object>();
    for(AnalyticValue<?> value : _values) {
      if(ObjectUtils.equals(value.getDefinition().getValue(definitionKey), definitionValue)) {
        result.add(value.getValue());
      }
    }
    return result;
  }

  // REVIEW jim 18-Sep-09 -- Very yucky use of varargs, but nice to use...
  public List<?> getValues(String definitionKey, Object definitionValue, Object... params) {
    if(definitionKey == null || params.length % 2 != 0) {
      return null;
    }
    List<Object> result = new ArrayList<Object>();
    for(AnalyticValue<?> value : _values) {
      outer:
      if(ObjectUtils.equals(value.getDefinition().getValue(definitionKey), definitionValue)) {
        for (int i=0; i<params.length; i+=2) {
          String iDefinitionKey = (String)params[i];
          Object iDefinitionValue = params[i+1];
          if (!ObjectUtils.equals(value.getDefinition().getValue(iDefinitionKey), iDefinitionValue)) {
            break outer;
          }
        }
        result.add(value.getValue());
      }
    }
    return result;
  }
  
  public Object getValue(AnalyticValueDefinition<?> definition) {
    if(definition == null) {
      return null;
    }
    for(AnalyticValue<?> value : _values) {
      if(AnalyticValueDefinitionComparator.matches(definition, value.getDefinition())) {
        return value.getValue();
      }
      /*if(ObjectUtils.equals(value.getDefinition(), definition)) {
        return value.getValue();
      }*/
    }
    return null;
  }
}
