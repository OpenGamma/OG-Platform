/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
// TODO is this class necessary? I don't think it adds much (any?) value over directly implementing BeanVisitor
/* package */
@SuppressWarnings("unchecked")
class BuildingBeanVisitor<T> implements BeanVisitor<T> {

  private final Bean _bean;
  private final BeanDataSink<T> _sink;

  /* package */ BuildingBeanVisitor(Bean bean, BeanDataSink<T> sink) {
    ArgumentChecker.notNull(bean, "bean");
    ArgumentChecker.notNull(sink, "sink");
    _bean = bean;
    _sink = sink;
  }

  @Override
  public void visitBean(MetaBean metaBean) {
    if (!_bean.getClass().equals(metaBean.beanType())) {
      throw new IllegalArgumentException("Bean type " + _bean.getClass().getName() + " is not the same as " +
                                             "MetaBean type " + metaBean.beanType().getName());
    }
    _sink.setBeanData(metaBean, _bean);
  }

  @Override
  public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _sink.setBeanData(property.name(), (Bean) property.get(_bean), traverser);
  }

  @Override
  public void visitCollectionProperty(MetaProperty<?> property) {
    _sink.setCollectionValues(property.name(), (Collection<String>) property.get(_bean));
  }

  @Override
  public void visitSetProperty(MetaProperty<?> property) {
    _sink.setCollectionValues(property.name(), (Collection<String>) property.get(_bean));
  }

  @Override
  public void visitListProperty(MetaProperty<?> property) {
    _sink.setCollectionValues(property.name(), (Collection<String>) property.get(_bean));
  }

  @Override
  public void visitMapProperty(MetaProperty<?> property) {
    _sink.setMapValues(property.name(), (Map<?, ?>) property.get(_bean));
  }

  @Override
  public void visitProperty(MetaProperty<?> property) {
    _sink.setValue(property.name(), property.getString(_bean));
  }

  @Override
  public T finish() {
    return _sink.finish();
  }
}

// TODO is this necessary? implement the visitor directly?
/* package */ interface BeanDataSink<T> {

  void setValue(String propertyName, Object value);

  void setCollectionValues(String propertyName, Collection<?> values);

  void setMapValues(String propertyName, Map<?, ?> values);

  void setBeanData(String propertyName, Bean bean, BeanTraverser traverser);

  void setBeanData(MetaBean metaBean, Bean bean);

  T finish();
}

// TODO just implement the visitor interface directly?
/* package */ class JsonBeanDataSink implements BeanDataSink<JSONObject> {

  private final Map<String, Object> _json = Maps.newHashMap();

  @Override
  public void setValue(String propertyName, Object value) {
    _json.put(propertyName, value);
  }

  @Override
  public void setCollectionValues(String propertyName, Collection<?> values) {
    _json.put(propertyName, values);
  }

  @Override
  public void setMapValues(String propertyName, Map<?, ?> values) {
    _json.put(propertyName, values);
  }

  @Override
  public void setBeanData(String propertyName, Bean bean, BeanTraverser traverser) {
    Object value;
    if (bean == null) {
      value = null;
    } else {
      BuildingBeanVisitor<JSONObject> visitor = new BuildingBeanVisitor<JSONObject>(bean, new JsonBeanDataSink());
      value = traverser.traverse(bean.metaBean(), visitor);
    }
    _json.put(propertyName, value);
  }

  @Override
  public void setBeanData(MetaBean metaBean, Bean bean) {
    _json.put("type", metaBean.beanType().getSimpleName());
  }

  @Override
  public JSONObject finish() {
    return new JSONObject(_json);
  }
}

/* package */ class JsonBeanDataSource implements BeanDataSource {

  private final JSONObject _json;

  /* package */ JsonBeanDataSource(JSONObject json) {
    ArgumentChecker.notNull(json, "json");
    _json = json;
  }

  /**
   * The JSON library has a NULL sentinel but doesn't document where it's used and where null is used.
   */
  private static boolean isNull(Object value) {
    return value == null || value == JSONObject.NULL;
  }

  @Override
  public String getValue(String propertyName) {
    Object value = _json.opt(propertyName);
    if (isNull(value)) {
      return null;
    }
    return (String) value;
  }

  @Override
  public List<String> getCollectionValues(String propertyName) {
    JSONArray array = _json.optJSONArray(propertyName);
    if (isNull(array)) {
      return null;
    }
    List<String> strings = Lists.newArrayListWithCapacity(array.length());
    for (int i = 0; i < array.length(); i++) {
      strings.add((String) array.opt(i));
    }
    return strings;
  }

  @Override
  public Map<String, String> getMapValues(String propertyName) {
    JSONObject jsonObject = _json.optJSONObject(propertyName);
    if (isNull(jsonObject)) {
      return null;
    }
    Map<String, String> map = Maps.newHashMap();
    for (Iterator it = jsonObject.keys(); it.hasNext(); ) {
      String key = (String) it.next();
      map.put(key, (String) jsonObject.opt(key));
    }
    return map;
  }

  @Override
  public BeanDataSource getBeanData(String propertyName) {
    JSONObject json = _json.optJSONObject(propertyName);
    if (isNull(json)) {
      return null;
    }
    return new JsonBeanDataSource(json);
  }

  @Override
  public String getBeanTypeName() {
    // TODO should this be configurable?
    try {
      return _json.getString("type");
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("Failed to read JSON", e);
    }
  }
}
