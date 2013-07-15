/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.util.ArgumentChecker;

/**
 * {@link BeanVisitor} that builds an object by pushing data from a bean into a {@link BeanDataSink sink}.
 * TODO a MUCH better name is required, this and BeanBuildingVisitor are too similar
 * @param <T> Type of object built by this visitor's sink
 */
/* package */ class BuildingBeanVisitor<T> implements BeanVisitor<T> {

  /** The bean that is the source of the data. */
  private final Bean _bean;
  /** The sink that will receive the converted bean data. */
  private final BeanDataSink<T> _sink;

  /* package */ BuildingBeanVisitor(Bean bean, BeanDataSink<T> sink) {
    ArgumentChecker.notNull(bean, "bean");
    ArgumentChecker.notNull(sink, "sink");
    _bean = bean;
    _sink = sink;
  }

  @Override
  public void visitMetaBean(MetaBean metaBean) {
    if (!_bean.getClass().equals(metaBean.beanType())) {
      throw new IllegalArgumentException("Bean type " + _bean.getClass().getName() + " is not the same as " +
                                             "MetaBean type " + metaBean.beanType().getName());
    }
    _sink.setBeanData(metaBean, _bean);
  }

  @Override
  public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _sink.setValue(property.name(), _sink.convert(property.get(_bean), property, property.propertyType(), traverser));
  }

  private Collection<Object> convertCollection(MetaProperty<?> property, BeanTraverser traverser) {
    Collection<?> values = (Collection<?>) property.get(_bean);
    List<Object> convertedValues = Lists.newArrayList();
    Class<?> collectionType = JodaBeanUtils.collectionType(property, property.declaringType());
    for (Object value : values) {
      convertedValues.add(_sink.convert(value, property, collectionType, traverser));
    }
    return convertedValues;
  }

  @Override
  public void visitCollectionProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _sink.setCollection(property.name(), convertCollection(property, traverser));
  }

  @Override
  public void visitSetProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _sink.setCollection(property.name(), convertCollection(property, traverser));
  }

  @Override
  public void visitListProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _sink.setCollection(property.name(), convertCollection(property, traverser));
  }

  @Override
  public void visitMapProperty(MetaProperty<?> property, BeanTraverser traverser) {
    Map<Object, Object> convertedMap = Maps.newHashMap();
    Map<?, ?> valueMap = (Map<?, ?>) property.get(_bean);
    Class<?> keyType = JodaBeanUtils.mapKeyType(property, property.declaringType());
    Class<?> valueType = JodaBeanUtils.mapValueType(property, property.declaringType());
    for (Map.Entry<?, ?> entry : valueMap.entrySet()) {
      Object key = _sink.convert(entry.getKey(), property, keyType, traverser);
      Object value = _sink.convert(entry.getValue(), property, valueType, traverser);
      convertedMap.put(key, value);
    }
    _sink.setMap(property.name(), convertedMap);
  }

  @Override
  public void visitProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _sink.setValue(property.name(), _sink.convert(property.get(_bean), property, property.propertyType(), traverser));
  }

  @Override
  public T finish() {
    return _sink.finish();
  }
}
