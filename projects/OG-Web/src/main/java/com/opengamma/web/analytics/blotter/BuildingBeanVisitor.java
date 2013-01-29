/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.convert.StringConvert;
import org.joda.convert.StringConverter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.util.ArgumentChecker;

/**
 * {@link BeanVisitor} that builds an object by pushing data from a bean into a {@link BeanDataSink sink}.
 * TODO a MUCH better name is required, this and BeanBuildingVisitor are too similar
 */
@SuppressWarnings("unchecked")
/* package */ class BuildingBeanVisitor<T> implements BeanVisitor<T> {

  private final Bean _bean;
  private final BeanDataSink<T> _sink;
  private final StringConvert _stringConvert;

  /* package */ BuildingBeanVisitor(Bean bean, BeanDataSink<T> sink, StringConvert stringConvert) {
    ArgumentChecker.notNull(bean, "bean");
    ArgumentChecker.notNull(sink, "sink");
    ArgumentChecker.notNull(stringConvert, "stringConvert");
    _bean = bean;
    _sink = sink;
    _stringConvert = stringConvert;
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
    if (isConvertible(property.propertyType())) {
      visitProperty(property);
      return;
    }
    _sink.setBeanValue(property.name(), (Bean) property.get(_bean), traverser);
  }

  private Collection<String> convertCollection(MetaProperty<?> property) {
    Collection<?> values = (Collection<?>) property.get(_bean);
    List<String> stringValues = Lists.newArrayList();
    Class<?> collectionType = JodaBeanUtils.collectionType(property, property.declaringType());
    StringConverter<Object> converter =
        (StringConverter<Object>) _stringConvert.findConverter(collectionType);
    for (Object value : values) {
      stringValues.add(converter.convertToString(value));
    }
    return stringValues;
  }

  @Override
  public void visitCollectionProperty(MetaProperty<?> property) {
    _sink.setCollectionValues(property.name(), convertCollection(property));
  }

  @Override
  public void visitSetProperty(MetaProperty<?> property) {
    _sink.setCollectionValues(property.name(), convertCollection(property));
  }

  @Override
  public void visitListProperty(MetaProperty<?> property) {
    _sink.setCollectionValues(property.name(), convertCollection(property));
  }

  @Override
  public void visitMapProperty(MetaProperty<?> property) {
    Class<?> keyType = JodaBeanUtils.mapKeyType(property, property.declaringType());
    Class<?> valueType = JodaBeanUtils.mapValueType(property, property.declaringType());
    StringConverter<Object> keyConverter = (StringConverter<Object>) _stringConvert.findConverter(keyType);
    StringConverter<Object> valueConverter = (StringConverter<Object>) _stringConvert.findConverter(valueType);
    HashMap<String, String> stringMap = Maps.newHashMap();
    Map<?, ?> valueMap = (Map<?, ?>) property.get(_bean);
    for (Map.Entry<?, ?> entry : valueMap.entrySet()) {
      stringMap.put(keyConverter.convertToString(entry.getKey()), valueConverter.convertToString(entry.getValue()));
    }
    _sink.setMapValues(property.name(), stringMap);
  }

  @Override
  public void visitProperty(MetaProperty<?> property) {
    String value = _stringConvert.convertToString((Class<Object>) property.propertyType(), property.get(_bean));
    _sink.setValue(property.name(), value);
  }

  @Override
  public T finish() {
    return _sink.finish();
  }

  private boolean isConvertible(Class<?> type) {
    boolean canConvert;
    try {
      _stringConvert.findConverter(type);
      canConvert = true;
    } catch (Exception e) {
      canConvert = false;
    }
    return canConvert;
  }

}
