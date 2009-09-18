/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.analytics;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * A basic {@link AnalyticValueDefinition} implementation backed by simple
 * collection classes.
 *
 * @author kirk
 */
public class AnalyticValueDefinitionImpl implements AnalyticValueDefinition, Serializable, Cloneable {
  private final Map<String, Set<Object>> _values = new TreeMap<String, Set<Object>>();

  /**
   * Empty constructor. This is provided for convenience, though it's not particularly useful
   * since the class is immutable.
   */
  public AnalyticValueDefinitionImpl() {
  }
  
  /**
   * Constructor for a definition with a single key and value.
   * @param key   The name of the key.
   * @param value The single value for the key.
   */
  public AnalyticValueDefinitionImpl(String key, Object value) {
    _values.put(key, Collections.singleton(value));
  }
  
  public AnalyticValueDefinitionImpl(String key, Object... values) {
    _values.put(key, new HashSet<Object>(Arrays.asList(values)));
  }
  
  public AnalyticValueDefinitionImpl(Map<String, ?> predicates) {
    this(predicates.entrySet());
  }
  
  public AnalyticValueDefinitionImpl(Collection<? extends Map.Entry<String, ?>> entries) {
    if(entries == null) {
      return;
    }
    for(Map.Entry<String, ?> entry : entries) {
      Set<Object> values = _values.get(entry.getKey());
      if(values == null) {
        values = new HashSet<Object>();
        _values.put(entry.getKey(), values);
      }
      values.add(entry.getValue());
    }
  }
  
  public AnalyticValueDefinitionImpl(Map.Entry<String, ?>... entries) {
    this(Arrays.asList(entries));
  }

  @Override
  public Set<String> getKeys() {
    return Collections.unmodifiableSet(_values.keySet());
  }

  @Override
  public Set<Object> getValues(String key) {
    Set<Object> values = _values.get(key);
    if(values == null) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(values);
  }

  @Override
  public Object getValue(String key) {
    Set<Object> values = _values.get(key);
    if((values == null) || (values.isEmpty())) {
      return null;
    }
    return values.iterator().next();
  }

  @Override
  public AnalyticValueDefinitionImpl clone() {
    try {
      return (AnalyticValueDefinitionImpl) super.clone();
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
    if(!(obj instanceof AnalyticValueDefinition)) {
      return false;
    }
    return AnalyticValueDefinitionComparator.equals(this, (AnalyticValueDefinition)obj);
  }

  @Override
  public int hashCode() {
    return AnalyticValueDefinitionComparator.hashCode(this);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
