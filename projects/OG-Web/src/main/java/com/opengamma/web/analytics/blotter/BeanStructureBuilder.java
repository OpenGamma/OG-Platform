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

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.PropertyStyle;
import org.joda.convert.StringConvert;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;

/**
 * Builds an HTML page containing a description of a type's attributes. It's intended to help with the development
 * of the blotter, not to be a long-term feature of the platform (hence the hacky nature of it).
 * Its output is used to populate bean-structure.ftl.
 * TODO decide if this is worth keeping and clean it up or delete it when it's not useful any more
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

  private final StringConvert _stringConvert;
  private final Map<String, Object> _beanData = Maps.newHashMap();
  private final BeanHierarchy _beanHierarchy;
  private final List<Map<String, Object>> _propertyData = Lists.newArrayList();
  private final Map<Class<?>, Class<?>> _underlyingSecurityTypes;
  private final Map<Class<?>, String> _endpoints;

  /* package */ BeanStructureBuilder(Set<MetaBean> metaBeans,
                                     Map<Class<?>, Class<?>> underlyingSecurityTypes,
                                     Map<Class<?>, String> endpoints,
                                     StringConvert stringConvert) {
    ArgumentChecker.notNull(underlyingSecurityTypes, "underlyingSecurityTypes");
    ArgumentChecker.notNull(metaBeans, "metaBeans");
    ArgumentChecker.notNull(endpoints, "endpoints");
    ArgumentChecker.notNull(stringConvert, "stringConvert");
    _endpoints = endpoints;
    _underlyingSecurityTypes = underlyingSecurityTypes;
    _beanHierarchy = new BeanHierarchy(metaBeans);
    _stringConvert = stringConvert;
  }

  @Override
  public void visitMetaBean(MetaBean metaBean) {
    _beanData.clear();
    String typeName = metaBean.beanType().getSimpleName();
    _beanData.put("type", typeName);
    Map<String, Object> typeProperty = Maps.newHashMap();
    typeProperty.put("name", "type");
    typeProperty.put("type", PropertyType.SINGLE.name().toLowerCase());
    typeProperty.put("types", ImmutableList.of(typeInfo("string", null, null, false)));
    typeProperty.put("optional", false);
    typeProperty.put("readOnly", false);
    typeProperty.put("value", typeName);
    _propertyData.add(typeProperty);
    Class<?> underlyingType = _underlyingSecurityTypes.get(metaBean.beanType());
    if (underlyingType != null) {
      _beanData.put("underlyingTypes", typesFor(underlyingType));
    }
  }

  @Override
  public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
    Class<?> type = property.propertyType();
    _propertyData.add(property(property, typesFor(type), null, PropertyType.SINGLE));
  }

  @Override
  public void visitCollectionProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _propertyData.add(arrayType(property));
  }

  @Override
  public void visitSetProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _propertyData.add(arrayType(property));
  }

  @Override
  public void visitListProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _propertyData.add(arrayType(property));
  }

  @Override
  public void visitMapProperty(MetaProperty<?> property, BeanTraverser traverser) {
    Class<? extends Bean> beanType = property.metaBean().beanType();
    Class<?> keyType = JodaBeanUtils.mapKeyType(property, beanType);
    Class<?> valueType = JodaBeanUtils.mapValueType(property, beanType);
    _propertyData.add(property(property, typesFor(keyType), typesFor(valueType), PropertyType.MAP));
  }

  @Override
  public void visitProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _propertyData.add(property(property, typesFor(property.propertyType()), null, PropertyType.SINGLE));
  }

  @Override
  public Map<String, Object> finish() {
    _beanData.put("properties", _propertyData);
    _beanData.put("now", ZonedDateTime.now(OpenGammaClock.getInstance()));
    return _beanData;
  }

  private Map<String, Object> arrayType(MetaProperty<?> property) {
    return property(property, typesFor(property.propertyType()), null, PropertyType.ARRAY);
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

  private static boolean isNullable(MetaProperty<?> property) {
    if (property.propertyType().isPrimitive()) {
      return false;
    } else {
      PropertyDefinition definitionAnnotation = property.annotation(PropertyDefinition.class);
      return !definitionAnnotation.validate().equals("notNull");
    }
  }

  /* package */ List<Map<String, Object>> typesFor(Class<?> type) {
    String typeName = s_types.get(type);
    if (typeName != null) {
      return ImmutableList.of(typeInfo(typeName, null, null, false));
    } else {
      boolean canConvert;
      canConvert = isConvertible(type);
      if (canConvert) {
        return ImmutableList.of(typeInfo(STRING, type.getSimpleName(), _endpoints.get(type), false));
      } else {
        // TODO deal with (potentially multiple) bean types
        Set<Class<? extends Bean>> subtypes = _beanHierarchy.subtypes(type);
        if (subtypes.isEmpty()) {
          throw new OpenGammaRuntimeException("No type mapping found for class " + type.getName());
        }
        List<Map<String, Object>> types = Lists.newArrayListWithCapacity(subtypes.size());
        for (Class<? extends Bean> subtype : subtypes) {
          types.add(typeInfo(subtype.getSimpleName(), null, _endpoints.get(subtype), true));
        }
        return types;
      }
    }
  }

  /* package */ static Map<String, Object> property(MetaProperty<?> property,
                                                    List<Map<String, Object>> types,
                                                    List<Map<String, Object>> valueTypes,
                                                    PropertyType propertyType) {
    Map<String, Object> result = Maps.newHashMap();
    // TODO this is *really* dirty and not supposed to be anything else. fix or remove
    boolean readOnly = property.style() == PropertyStyle.READ_ONLY || property.name().equals("uniqueId");

    // TODO this is obviously a poor choice of names
    result.put("type", propertyType.name().toLowerCase());
    result.put("types", types);
    result.put("name", property.name());
    result.put("optional", isNullable(property));
    result.put("readOnly", readOnly);
    if (valueTypes != null) {
      result.put("valueTypes", types);
    }
    return result;
  }

  // TODO replace with strings
  public enum PropertyType {
    SINGLE, ARRAY, MAP
  }

  /* package */ static Map<String, Object> typeInfo(String expectedType,
                                                    String actualType,
                                                    String endpoint,
                                                    boolean isBeanType) {
    Map<String, Object> results = Maps.newHashMap();
    results.put("expectedType", expectedType);
    results.put("actualType", actualType);
    results.put("endpoint", endpoint);
    results.put("beanType", isBeanType);
    return results;
  }
}
