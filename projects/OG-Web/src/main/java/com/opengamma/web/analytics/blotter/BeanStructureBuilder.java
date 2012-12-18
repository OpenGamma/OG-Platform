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
 * Builds an HTML page containing a description of a type's attributes. It's intended to help with the development
 * of the blotter, not to be a long-term feature of the platform (hence the hacky nature of it).
 * Its output is used to populate bean-structure.tfl.
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

  private final Map<String, Object> _beanData = Maps.newHashMap();
  private final BeanHierarchy _beanHierarchy;
  private final List<Map<Object, Object>> _propertyData = Lists.newArrayList();
  private final Map<Class<?>, Class<?>> _underlyingSecurityTypes;
  private final Map<Class<?>, String> _endpoints;

  /* package */ BeanStructureBuilder(Set<MetaBean> metaBeans,
                                     Map<Class<?>, Class<?>> underlyingSecurityTypes,
                                     Map<Class<?>, String> endpoints) {
    _endpoints = endpoints;
    ArgumentChecker.notNull(underlyingSecurityTypes, "underlyingSecurityTypes");
    ArgumentChecker.notNull(metaBeans, "metaBeans");
    ArgumentChecker.notNull(endpoints, "endpoints");
    _underlyingSecurityTypes = underlyingSecurityTypes;
    _beanHierarchy = new BeanHierarchy(metaBeans);
  }

  @Override
  public void visitBean(MetaBean metaBean) {
    _beanData.clear();
    _beanData.put("type", metaBean.beanType().getSimpleName());
    Class<?> underlyingType = _underlyingSecurityTypes.get(metaBean.beanType());
    if (underlyingType != null) {
      _beanData.put("hasUnderlying", true);
      _beanData.put("underlyingTypeInfo", typesFor(underlyingType));
    } else {
      _beanData.put("hasUnderlying", false);
    }
  }

  @Override
  public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
    Class<?> type = property.propertyType();
    if (isConvertible(type)) {
      _propertyData.add(propertyData(property,
                                     "typeInfo", typesFor(type),
                                     "type", "single"));
    } else {
      _propertyData.add(propertyData(property,
                                     "typeInfo", typesFor(type),
                                     "type", "single"));
    }
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
                                   "keyTypeInfo", typesFor(keyType),
                                   "valueTypeInfo", typesFor(valueType),
                                   "type", "map"));
  }

  @Override
  public void visitProperty(MetaProperty<?> property) {
    _propertyData.add(propertyData(property,
                                   "typeInfo", typesFor(property.propertyType()),
                                   "type", "single"));
  }

  @Override
  public Map<String, Object> finish() {
    _beanData.put("properties", _propertyData);
    _beanData.put("now", ZonedDateTime.now(OpenGammaClock.getInstance()));
    return _beanData;
  }

  private Map<Object, Object> arrayType(MetaProperty<?> property) {
    return propertyData(property,
                        "typeInfo", typesFor(property.propertyType()),
                        "type", "array");
  }

  private List<TypeInfo> typesFor(Class<?> type) {
    String typeName = s_types.get(type);
    if (typeName != null) {
      return ImmutableList.of(new TypeInfo(typeName, null, null, false));
    } else {
      boolean canConvert;
      canConvert = isConvertible(type);
      if (canConvert) {
        return ImmutableList.of(new TypeInfo(STRING, type.getSimpleName(), _endpoints.get(type), false));
      } else {
        // TODO deal with (potentially multiple) bean types
        Set<Class<? extends Bean>> subtypes = _beanHierarchy.subtypes(type);
        if (subtypes.isEmpty()) {
          throw new OpenGammaRuntimeException("No type mapping found for class " + type.getName());
        }
        List<TypeInfo> types = Lists.newArrayListWithCapacity(subtypes.size());
        for (Class<? extends Bean> subtype : subtypes) {
          types.add(new TypeInfo(subtype.getSimpleName(), null, _endpoints.get(subtype), true));
        }
        return types;
      }
    }
  }

  private static boolean isConvertible(Class<?> type) {
    boolean canConvert;
    try {
      JodaBeanUtils.stringConverter().findConverter(type);
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

  // TODO proper params, not just map elements
  private static Map<Object, Object> propertyData(MetaProperty<?> property, Object... values) {
    final Map<Object, Object> result = Maps.newHashMap();
    result.put("name", property.name());
    result.put("isOptional", isNullable(property));
    // TODO this is *really* dirty and not supposed to be anything else. fix or remove
    boolean readOnly = property.readWrite() == PropertyReadWrite.READ_ONLY ||
                       property.name().equals("uniqueId") ||
                       property.name().equals("name");
    result.put("isReadOnly", readOnly);
    for (int i = 0; i < values.length / 2; i++) {
      result.put(values[i * 2], values[(i * 2) + 1]);
    }
    return result;
  }

  @SuppressWarnings("UnusedDeclaration") // this is only public so Freemarker can use it
  public static final class TypeInfo {

    private final String _expectedType;
    private final String _actualType;
    private final String _endpoint;
    private final boolean _isBeanType;

    private TypeInfo(String expectedType, String actualType, String endpoint, boolean isBeanType) {
      ArgumentChecker.notNull(expectedType, "expectedType");
      _isBeanType = isBeanType;
      _expectedType = expectedType;
      _actualType = actualType;
      _endpoint = endpoint;
    }

    public String getExpectedType() {
      return _expectedType;
    }

    public String getActualType() {
      return _actualType;
    }

    public String getEndpoint() {
      return _endpoint;
    }

    public boolean getBeanType() {
      return _isBeanType;
    }

    @Override
    public String toString() {
      return "TypeInfo [" +
          "_expectedType='" + _expectedType + '\'' +
          ", _actualType='" + _actualType + '\'' +
          ", _endpoint='" + _endpoint + '\'' +
          ", _isBeanType=" + _isBeanType +
          "]";
    }
  }
}
