/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;

/**
 * Builds a bean instance by traversing the {@link MetaBean} and populating the {@link MetaBean#builder() builder}
 * with data from a {@link BeanDataSource}. The {@link BeanBuilder builder} is returned so calling code can update
 * it before building the instance, e.g. to set invariant values that aren't in the data source but are necessary
 * to build a valid object.
 * @param <T> The type of bean to build
 */
/* package */ class BeanBuildingVisitor<T extends Bean> implements BeanVisitor<BeanBuilder<T>> {

  /** The source of data for building the bean */
  private final BeanDataSource _data;
  /** For looking up {@link MetaBean}s for building the bean and any sub-beans */
  private final MetaBeanFactory _metaBeanFactory;
  /** For converting the data to property values */
  private final Converters _converters;

  /** The builder for the instance being built */
  private BeanBuilder<T> _builder;

  /* package */ BeanBuildingVisitor(BeanDataSource data, MetaBeanFactory metaBeanFactory, Converters converters) {
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.notNull(metaBeanFactory, "metaBeanFactory");
    ArgumentChecker.notNull(converters, "converters");
    _converters = converters;
    _metaBeanFactory = metaBeanFactory;
    _data = data;
  }

  @SuppressWarnings("unchecked")
  @Override
  public void visitMetaBean(MetaBean metaBean) {
    _builder = (BeanBuilder<T>) metaBean.builder();
  }

  @Override
  public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
    visitProperty(property, traverser);
  }

  @Override
  public void visitSetProperty(MetaProperty<?> property, BeanTraverser traverser) {
    List<?> dataValues = _data.getCollectionValues(property.name());
    if (dataValues == null) {
      return;
    }
    Set<Object> values = Sets.newHashSetWithExpectedSize(dataValues.size());
    Class<?> collectionType = JodaBeanUtils.collectionType(property, property.declaringType());
    for (Object dataValue : dataValues) {
      values.add(convert(dataValue, property, collectionType, traverser));
    }
    _builder.set(property, values);
  }

  @Override
  public void visitListProperty(MetaProperty<?> property, BeanTraverser traverser) {
    List<?> dataValues = _data.getCollectionValues(property.name());
    if (dataValues == null) {
      return;
    }
    List<Object> values = Lists.newArrayList();
    Class<?> collectionType = JodaBeanUtils.collectionType(property, property.declaringType());
    for (Object dataValue : dataValues) {
      values.add(convert(dataValue, property, collectionType, traverser));
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
    _builder.set(property, convert(_data.getValue(property.name()), property, property.propertyType(), traverser));
  }

  @Override
  public BeanBuilder<T> finish() {
    return _builder;
  }

  private Object convert(Object value, MetaProperty<?> property, Class<?> expectedType, BeanTraverser traverser) {
    Object convertedValue = _converters.convert(value, property, expectedType);
    if (convertedValue != Converters.CONVERSION_FAILED) {
      return convertedValue;
    }
    if (value instanceof BeanDataSource) {
      BeanDataSource beanData = (BeanDataSource) value;
      BeanBuildingVisitor<?> visitor = new BeanBuildingVisitor<>(beanData, _metaBeanFactory, _converters);
      MetaBean metaBean = _metaBeanFactory.beanFor(beanData);
      return ((BeanBuilder<?>) traverser.traverse(metaBean, visitor)).build();
    }
    throw new IllegalArgumentException("Unable to convert " + value + " to " + expectedType.getName());
  }

  private Map<?, ?> buildMap(MetaProperty<?> property, BeanTraverser traverser) {
    Map<?, ?> sourceData = _data.getMapValues(property.name());
    Class<? extends Bean> beanType = property.metaBean().beanType();
    Class<?> keyType = JodaBeanUtils.mapKeyType(property, beanType);
    Class<?> valueType = JodaBeanUtils.mapValueType(property, beanType);
    Map<Object, Object> map = Maps.newHashMapWithExpectedSize(sourceData.size());
    for (Map.Entry<?, ?> entry : sourceData.entrySet()) {
      Object key = convert(entry.getKey(), property, keyType, traverser);
      Object value = convert(entry.getValue(), property, valueType, traverser);
      map.put(key, value);
    }
    return map;
  }
}

/**
 * Converter that ignores its input and always returns {@code FINANCIAL_REGION~GB}. This is for building FX securities
 * which always have the same region.
 */
/* package */ class FXRegionConverter implements Converter<Object, ExternalId> {

  /** Great Britain region */
  private static final ExternalId GB_REGION = ExternalId.of(ExternalSchemes.FINANCIAL, "GB");

  /**
   * Ignores its input, always returns {@code FINANCIAL_REGION~GB}.
   * @param notUsed Not used
   * @return {@code FINANCIAL_REGION~GB}
   */
  @Override
  public ExternalId convert(Object notUsed) {
    return GB_REGION;
  }
}
