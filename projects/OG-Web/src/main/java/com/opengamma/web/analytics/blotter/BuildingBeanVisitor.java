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
import org.json.JSONException;
import org.json.JSONObject;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
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
    _sink.setBeanTypeName(metaBean.beanType().getSimpleName());
  }

  @Override
  public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _sink.setBeanData(property.name(), _bean, traverser);
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
    _sink.setValue(property.name(), property.get(_bean));
  }

  @Override
  public T finish() {
    return _sink.finish();
  }
}

/* package */ interface BeanDataSink<T> {

  void setValue(String propertyName, Object value);

  void setCollectionValues(String propertyName, Collection<?> values);

  void setMapValues(String propertyName, Map<?, ?> values);

  void setBeanData(String propertyName, Bean bean, BeanTraverser traverser);

  void setBeanTypeName(String beanTypeName);

  T finish();
}

/* package */ class JsonBeanDataSink implements BeanDataSink<JSONObject> {

  private final JSONObject _json = new JSONObject();

  @Override
  public void setValue(String propertyName, Object value) {
    try {
      _json.put(propertyName, value);
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("Failed to create JSON", e);
    }
  }

  @Override
  public void setCollectionValues(String propertyName, Collection<?> values) {
    try {
      _json.put(propertyName, values);
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("Failed to create JSON", e);
    }
  }

  @Override
  public void setMapValues(String propertyName, Map<?, ?> values) {
    try {
      _json.put(propertyName, values);
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("Failed to create JSON", e);
    }
  }

  @Override
  public void setBeanData(String propertyName, Bean bean, BeanTraverser traverser) {
    try {
      Object value;
      if (bean == null) {
        value = null;
      } else {
        BuildingBeanVisitor<JSONObject> visitor = new BuildingBeanVisitor<JSONObject>(bean, new JsonBeanDataSink());
        value = traverser.traverse(bean.metaBean(), visitor);
      }
      _json.put(propertyName, value);
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("Failed to create JSON", e);
    }
  }

  @Override
  public void setBeanTypeName(String beanTypeName) {
    try {
      _json.put("type", beanTypeName);
    } catch (JSONException e) {
      throw new OpenGammaRuntimeException("Failed to create JSON", e);
    }
  }

  @Override
  public JSONObject finish() {
    return _json;
  }
}
