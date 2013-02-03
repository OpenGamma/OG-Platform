/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.convert.StringConvert;
import org.joda.convert.StringConverter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
/* package */ class BeanBuildingVisitor<T extends Bean> implements BeanVisitor<BeanBuilder<T>> {

  private final BeanDataSource _data;
  private final MetaBeanFactory _metaBeanFactory;
  private final StringConvert _stringConvert;

  private BeanBuilder<T> _builder;

  @SuppressWarnings("unchecked")
  /* package */ BeanBuildingVisitor(BeanDataSource data, MetaBeanFactory metaBeanFactory, StringConvert stringConvert) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(metaBeanFactory, "metaBeanFactory");
    ArgumentChecker.notNull(stringConvert, "stringConvert");
    _metaBeanFactory = metaBeanFactory;
    _data = data;
    _stringConvert = stringConvert;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void visitMetaBean(MetaBean metaBean) {
    _builder = (BeanBuilder<T>) metaBean.builder();
  }

  @SuppressWarnings("unchecked")
  @Override
  public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
    visitProperty(property, traverser);
  }

  @Override
  public void visitSetProperty(MetaProperty<?> property, BeanTraverser traverser) {
    // TODO implement visitSetProperty()
    throw new UnsupportedOperationException("visitSetProperty not implemented");
  }

  @Override
  public void visitListProperty(MetaProperty<?> property, BeanTraverser traverser) {
    List<?> dataValues = _data.getCollectionValues(property.name());
    List<Object> values = Lists.newArrayList();
    Class<?> collectionType = JodaBeanUtils.collectionType(property, property.declaringType());
    for (Object dataValue : dataValues) {
      values.add(convert(dataValue, collectionType, traverser));
    }
    _builder.set(property, values);
  }

  @Override
  public void visitCollectionProperty(MetaProperty<?> property, BeanTraverser traverser) {
    visitListProperty(property, traverser);
  }

  @Override
  public void visitMapProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _builder.set(property, buildMap(property, traverser));
  }

  @Override
  public void visitProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _builder.set(property, convert(_data.getValue(property.name()), property.propertyType(), traverser));
  }

  @Override
  public BeanBuilder<T> finish() {
    return _builder;
  }

  @SuppressWarnings("unchecked")
  private Object convert(Object value, Class<?> type, BeanTraverser traverser) {
    if (value == null) {
      return null;
    } else if (type.isAssignableFrom(value.getClass())) {
      return value;
    } else if (value instanceof String) {
      try {
        StringConverter<Object> converter = (StringConverter<Object>) _stringConvert.findConverter(type);
        return converter.convertFromString(type, (String) value);
      } catch (Exception e) {
        // carry on and try something else
      }
    } else if (value instanceof BeanDataSource) {
      BeanDataSource beanData = (BeanDataSource) value;
      BeanBuildingVisitor<?> visitor = new BeanBuildingVisitor<>(beanData, _metaBeanFactory, _stringConvert);
      MetaBean metaBean = _metaBeanFactory.beanFor(beanData);
      return ((BeanBuilder<?>) traverser.traverse(metaBean, visitor)).build();
    }
    throw new IllegalArgumentException("Unable to convert " + value + " to " + type);
  }

  private Map<?, ?> buildMap(MetaProperty<?> property, BeanTraverser traverser) {
    Map<?, ?> sourceData = _data.getMapValues(property.name());
    Class<? extends Bean> beanType = property.metaBean().beanType();
    Class<?> keyType = JodaBeanUtils.mapKeyType(property, beanType);
    Class<?> valueType = JodaBeanUtils.mapValueType(property, beanType);
    Map<Object, Object> map = Maps.newHashMapWithExpectedSize(sourceData.size());
    for (Map.Entry<?, ?> entry : sourceData.entrySet()) {
      Object key = convert(entry.getKey(), keyType, traverser);
      Object value = convert(entry.getValue(), valueType, traverser);
      map.put(key, value);
    }
    return map;
  }
}
