/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.ZonedDateTime;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.PropertyReadWrite;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;

/**
 * TODO handle underlying differently depending on the class
 */
/* package */ class BeanStructureBuilder implements BeanVisitor<Map<String, Object>> {

  private static final Map<Class<?>, String> s_types = Maps.newHashMap();
  private static final String NUMBER = "number";
  private static final String BOOLEAN = "boolean";
  private static final String STRING = "string";

  static {
    s_types.put(Double.TYPE, NUMBER);
    s_types.put(Double.class, NUMBER);
    s_types.put(Float.TYPE, NUMBER);
    s_types.put(Float.class, NUMBER);
    s_types.put(Long.TYPE, NUMBER);
    s_types.put(Long.class, NUMBER);
    s_types.put(Short.TYPE, NUMBER);
    s_types.put(Short.class, NUMBER);
    s_types.put(Integer.TYPE, NUMBER);
    s_types.put(Integer.class, NUMBER);
    s_types.put(Byte.TYPE, NUMBER);
    s_types.put(Byte.class, NUMBER);
    s_types.put(BigDecimal.class, NUMBER);
    s_types.put(Boolean.TYPE, BOOLEAN);
    s_types.put(Boolean.class, BOOLEAN);
    s_types.put(Character.TYPE, STRING);
    s_types.put(Character.class, STRING);
    s_types.put(String.class, STRING);
  }

  private final Map<String, Object> _beanData = Maps.newHashMap();
  private final BeanHierarchy _beanHierarchy;
  private final List<Map<Object, Object>> _propertyData = Lists.newArrayList();
  private final Map<Class<?>, Class<?>> _underlyingSecurityTypes;

  /* package */ BeanStructureBuilder(Set<MetaBean> metaBeans, Map<Class<?>, Class<?>> underlyingSecurityTypes) {
    ArgumentChecker.notNull(underlyingSecurityTypes, "underlyingSecurityTypes");
    ArgumentChecker.notNull(metaBeans, "metaBeans");
    _underlyingSecurityTypes = underlyingSecurityTypes;
    _beanHierarchy = new BeanHierarchy(metaBeans);
  }

  @Override
  public void visitBean(MetaBean metaBean) {
    // TODO configurable type field name
    _beanData.clear();
    _beanData.put("type", metaBean.beanType().getSimpleName());
    Class<?> underlyingType = _underlyingSecurityTypes.get(metaBean.beanType());
    if (underlyingType != null) {
      _beanData.put("hasUnderlying", true);
      _beanData.put("underlyingTypeNames", beanSubtypeNames(underlyingType));
    } else {
      _beanData.put("hasUnderlying", false);
    }
  }

  @Override
  public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _propertyData.add(propertyData(property,
                                   "typeNames", beanSubtypeNames(property.propertyType()),
                                   "isBean", true,
                                   "type", "single"));
  }

  private List<String> beanSubtypeNames(Class<?> type) {
    Set<Class<? extends Bean>> argumentTypes = _beanHierarchy.subtypes(type);
    if (argumentTypes.isEmpty()) {
      throw new OpenGammaRuntimeException("No bean types are available to satisfy property to type " + type);
    }
    List<String> beanTypeNames = Lists.newArrayListWithCapacity(argumentTypes.size());
    for (Class<? extends Bean> argumentType : argumentTypes) {
      beanTypeNames.add(argumentType.getSimpleName());
    }
    return beanTypeNames;
  }

  @Override
  public void visitCollectionProperty(MetaProperty<?> property) {
    _propertyData.add(arrayType(property));
  }

  @Override
  public void visitSetProperty(MetaProperty<?> property) {
    _propertyData.add(arrayType(property));
  }

  @Override
  public void visitListProperty(MetaProperty<?> property) {
    _propertyData.add(arrayType(property));
  }

  @Override
  public void visitMapProperty(MetaProperty<?> property) {
    Class<? extends Bean> beanType = property.metaBean().beanType();
    Class<?> keyType = JodaBeanUtils.mapKeyType(property, beanType);
    Class<?> valueType = JodaBeanUtils.mapValueType(property, beanType);
    _propertyData.add(propertyData(property,
                                   "keyTypeName", typeFor(keyType),
                                   "valueTypeName", typeFor(valueType), // TODO what if values are beans?
                                   "type", "map"));
  }

  @Override
  public void visitProperty(MetaProperty<?> property) {
    _propertyData.add(propertyData(property,
                                   "typeNames", ImmutableList.of(typeFor(property.propertyType())),
                                   "isBean", false,
                                   "isOptional", isNullable(property),
                                   "type", "single"));
  }

  @Override
  public Map<String, Object> finish() {
    _beanData.put("properties", _propertyData);
    _beanData.put("now", ZonedDateTime.now(OpenGammaClock.getInstance()));
    return _beanData;
  }

  private static Map<Object, Object> arrayType(MetaProperty<?> property) {
    return propertyData(property,
                        "typeNames", ImmutableList.of(typeFor(property.propertyType())),
                        "isBean", false,
                        "type", "array");
  }

  /*private static String typeFor(MetaProperty<?> property) {
    return typeFor(property.propertyType());
  }*/

  // TODO what if type is a bean or can be one of several beans? need to return an object
  private static String typeFor(Class<?> type) {
    String typeName = s_types.get(type);
    if (typeName != null) {
      return typeName;
    } else {
      try {
        JodaBeanUtils.stringConverter().findConverter(type);
        // TODO if the type has an endpoint for available values (e.g. day count, frequency) include that
        return STRING + " (" + type.getSimpleName() + ")";
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("No type mapping found for class " + type.getName(), e);
      }
    }
  }

  private static boolean isNullable(MetaProperty<?> property) {
    if (property.propertyType().isPrimitive()) {
      return false;
    } else {
      PropertyDefinition definitionAnnotation = property.annotation(PropertyDefinition.class);
      return !definitionAnnotation.validate().equals("notNull");
    }
  }

  private static Map<Object, Object> propertyData(MetaProperty<?> property, Object... values) {
    final Map<Object, Object> result = Maps.newHashMap();
    result.put("name", property.name());
    result.put("isOptional", isNullable(property));
    result.put("isReadOnly", property.readWrite() == PropertyReadWrite.READ_ONLY);
    for (int i = 0; i < values.length / 2; i++) {
      result.put(values[i * 2], values[(i * 2) + 1]);
    }
    return result;
  }
}
