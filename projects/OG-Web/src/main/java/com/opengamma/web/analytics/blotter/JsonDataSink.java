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
import org.joda.beans.MetaProperty;
import org.json.JSONObject;

import com.google.common.collect.Maps;
import com.opengamma.util.ArgumentChecker;

/**
 * Receives data from a Joda bean and writes it into a JSON object.
 */
/* package */ class JsonDataSink implements BeanDataSink<JSONObject> {

  /** The JSON structure. */
  private final Map<String, Object> _json = Maps.newHashMap();
  /** For converting object values to strings to populate the JSON. */
  private final Converters _converters;

  /* package */ JsonDataSink(Converters converters) {
    ArgumentChecker.notNull(converters, "converters");
    _converters = converters;
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

  @Override
  public Object convert(Object value, MetaProperty<?> property, Class<?> type, BeanTraverser traverser) {
    Object convertedValue = _converters.convert(value, property, type);
    if (convertedValue != Converters.CONVERSION_FAILED) {
      return convertedValue;
    }
    if (Bean.class.isAssignableFrom(value.getClass())) {
      Bean bean = (Bean) value;
      BuildingBeanVisitor<JSONObject> visitor = new BuildingBeanVisitor<>(bean, new JsonDataSink(_converters));
      return traverser.traverse(bean.metaBean(), visitor);
    } else {
      throw new IllegalArgumentException("Unable to convert " + value.getClass().getName());
    }
  }

  @Override
  public JSONObject finish() {
    return new JSONObject(_json);
  }

}

