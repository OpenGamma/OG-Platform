/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Collection;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.convert.StringConvert;
import org.joda.convert.StringConverter;
import org.json.JSONObject;

import com.google.common.collect.Maps;

/**
 * Receives data from a Joda bean and writes it into a JSON object.
 */
/* package */ class JsonDataSink implements BeanDataSink<JSONObject> {

  /** The JSON structure. */
  private final Map<String, Object> _json = Maps.newHashMap();
  /** For converting object values to strings to populate the JSON. */
  private final StringConvert _stringConvert;

  /* package */ JsonDataSink(StringConvert stringConvert) {
    _stringConvert = stringConvert;
  }

  @Override
  public void setBeanData(MetaBean metaBean, Bean bean) {
    _json.put("type", metaBean.beanType().getSimpleName());
  }

  @Override
  public void setValue(String propertyName, Object value) {
    _json.put(propertyName, value);
  }

  @Override
  public void setCollection(String propertyName, Collection<?> values) {
    _json.put(propertyName, values);
  }

  @Override
  public void setMap(String propertyName, Map<?, ?> values) {
    _json.put(propertyName, values);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object convert(Object value, Class<?> type, BeanTraverser traverser) {
    if (value == null) {
      return null;
    }
    Object convertedValue = convert(value, type);
    if (convertedValue != null) {
      return convertedValue;
    }
    if (Bean.class.isAssignableFrom(value.getClass())) {
      Bean bean = (Bean) value;
      BuildingBeanVisitor<JSONObject> visitor = new BuildingBeanVisitor<>(bean, new JsonDataSink(_stringConvert));
      return traverser.traverse(bean.metaBean(), visitor);
    } else {
      throw new IllegalArgumentException("Unable to convert " + value.getClass());
    }
  }

  /**
   * Converts a value to a string, returns null if it can't.
   *
   * @param value The value
   * @param type The type to use when looking up the converted, not necessarily the same as the value's type
   * @return The value as a string, null if it can't be converted
   */
  private String convert(Object value, Class<?> type) {
    StringConverter<Object> converter = getConverter(type);
    if (converter != null) {
      return converter.convertToString(value);
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private StringConverter<Object> getConverter(Class<?> type) {
    try {
      return (StringConverter<Object>) _stringConvert.findConverter(type);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public JSONObject finish() {
    return new JSONObject(_json);
  }

}

