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

  @SuppressWarnings("unchecked")
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

  @SuppressWarnings("unchecked")
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
/* TODO Conveters class, Converter interface
Conveters has the same logic as convert() above but also has a map of Converter instances keyed by metaproperty
except:
Converters are tried first
  before null check? would require them to check for null but also allow them to handle it
  how would they signal the difference bewteen converting to null and not being able to handle a value?
could handle the underlyingId problem by having a converter that is constructed with a value and ignores the input
something similar in BuildingBeanVisitor?
could fill in mandatory properties that will be overwritten later
*/

/* package */ class Converters {

  /**
   * Sentinal value signalling a value cannot be converted. null can't be used to signal conversion failure because
   * null is a valid return value for the conversion operation.
   */
  public static final Object CONVERSION_FAILED = new Object();

  /** Converters keyed by the property whose values they can convert. */
  private final Map<MetaProperty<?>, Converter<?, ?>> _converters;
  /** For converting strings to objects. */
  private final StringConvert _stringConvert;

  /* package */ Converters(Map<MetaProperty<?>, Converter<?, ?>> converters, StringConvert stringConvert) {
    // TODO defensive copying and validation of converters
    ArgumentChecker.notNull(converters, "converters");
    ArgumentChecker.notNull(stringConvert, "stringConvert");
    _converters = converters;
    _stringConvert = stringConvert;
  }

  /**
   *
   * @param value The value to convert, possibly null
   * @param property The property associated with the value (the converter might be converting from a value of the
   * property type or to a value that will be used to set the property).
   * @param type The type to convert from or to
   * @return The converted value, possibly null, {@link #CONVERSION_FAILED} if the value couldn't be converted
   */
  @SuppressWarnings("unchecked")
  Object convert(Object value, MetaProperty<?> property, Class<?> type) {
    Converter<Object, Object> converter = (Converter<Object, Object>) _converters.get(property);
    if (converter != null) {
      return converter.convert(value);
    } else if (value == null) {
      return null;
    } else if (value instanceof String) {
      try {
        StringConverter<Object> stringConverter = (StringConverter<Object>) _stringConvert.findConverter(type);
        return stringConverter.convertFromString(type, (String) value);
      } catch (Exception e) {
        // carry on
      }
    } else {
      try {
        StringConverter<Object> stringConverter = (StringConverter<Object>) _stringConvert.findConverter(type);
        return stringConverter.convertToString(value);
      } catch (Exception e) {
        // carry on
      }
    }
    return CONVERSION_FAILED;
  }
}

/**
 * @param <F> The type to convert from
 * @param <T> The type to convert to
 */
/* package */ interface Converter<F, T> {

  /**
   * Converts an object from one type to another
   * @param f The unconverted object
   * @return The converted object
   */
  T convert(F f);
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

/**
 * Converts an {@link ExternalId} to a string.
 */
/* package */ class RegionIdToStringConverter implements Converter<ExternalId, String> {

  /**
   * Converts an {@link ExternalId} to a string
   * @param regionId The region ID, not null
   * @return {@code regionId}'s value
   */
  @Override
  public String convert(ExternalId regionId) {
    ArgumentChecker.notNull(regionId, "regionId");
    return regionId.getValue();
  }
}
