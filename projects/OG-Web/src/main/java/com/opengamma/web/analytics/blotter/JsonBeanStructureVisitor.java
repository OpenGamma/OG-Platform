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

import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyDefinition;
import org.joda.convert.StringConvert;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;

/**
 * TODO can this be deleted?
 * TODO how should read-only properties be handled? I guess it depends on the use case of the data
 */
// TODO do this as HTML, easier to consume
/* package */ class JsonBeanStructureVisitor implements BeanVisitor<JSONObject> {

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

  private final Map<String, Object> _json = Maps.newHashMap();
  private final BeanHierarchy _beanHierarchy;
  private final StringConvert _stringConvert;

  /* package */ JsonBeanStructureVisitor(Set<MetaBean> metaBeans) {
    ArgumentChecker.notNull(metaBeans, "metaBeans");
    _beanHierarchy = new BeanHierarchy(metaBeans);
    // TODO parameter for this
    _stringConvert = JodaBeanUtils.stringConverter();
  }

  @Override
  public void visitMetaBean(MetaBean metaBean) {
    // TODO configurable field name
    _json.clear();
    _json.put("type", metaBean.beanType().getSimpleName());
  }

  @Override
  public void visitBeanProperty(MetaProperty<?> property, BeanTraverser traverser) {
    Set<Class<? extends Bean>> argumentTypes = _beanHierarchy.subtypes(property.propertyType());
    if (argumentTypes.isEmpty()) {
      throw new OpenGammaRuntimeException("No bean types are available to satisfy property " + property);
    }
    List<String> beanTypeNames = Lists.newArrayListWithCapacity(argumentTypes.size());
    for (Class<? extends Bean> argumentType : argumentTypes) {
      beanTypeNames.add(argumentType.getSimpleName());
    }
    _json.put(property.name(), optional(property, StringUtils.join(beanTypeNames, "|")));
  }

  @Override
  public void visitCollectionProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _json.put(property.name(), arrayType(property));
  }

  @Override
  public void visitSetProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _json.put(property.name(), arrayType(property));
  }

  @Override
  public void visitListProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _json.put(property.name(), arrayType(property));
  }

  @Override
  public void visitMapProperty(MetaProperty<?> property, BeanTraverser traverser) {
    Class<? extends Bean> beanType = property.metaBean().beanType();
    Class<?> keyType = JodaBeanUtils.mapKeyType(property, beanType);
    Class<?> valueType = JodaBeanUtils.mapValueType(property, beanType);
    _json.put(property.name(), optional(property,  "{" + typeFor(keyType) + ":" + typeFor(valueType) + "}"));
  }

  @Override
  public void visitProperty(MetaProperty<?> property, BeanTraverser traverser) {
    _json.put(property.name(), optional(property, typeFor(property)));
  }

  private static String optional(MetaProperty<?> property, String type) {
    if (nullable(property)) {
      return type + "?";
    } else {
      return type;
    }
  }

  @Override
  public JSONObject finish() {
    return new JSONObject(_json);
  }

  private String arrayType(MetaProperty<?> property) {
    return optional(property, "[" + typeFor(property.propertyType()) + "]");
  }

  private String typeFor(MetaProperty<?> property) {
    return typeFor(property.propertyType());
  }

  private String typeFor(Class<?> type) {
    String typeName = s_types.get(type);
    if (typeName != null) {
      return typeName;
    } else {
      try {
        _stringConvert.findConverter(type);
        return STRING;
      } catch (Exception e) {
        throw new OpenGammaRuntimeException("No type mapping found for class " + type.getName(), e);
      }
    }
  }

  private static boolean nullable(MetaProperty<?> property) {
    if (property.propertyType().isPrimitive()) {
      return false;
    } else {
      PropertyDefinition definitionAnnotation = property.annotation(PropertyDefinition.class);
      return !definitionAnnotation.validate().equals("notNull");
    }
  }
}
