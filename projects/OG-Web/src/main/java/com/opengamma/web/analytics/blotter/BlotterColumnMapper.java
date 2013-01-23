/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Map;

import org.joda.beans.MetaProperty;

import com.google.common.collect.Maps;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ClassMap;

/**
 * Maps the properties of each blotter column to properties in each supported security type.
 */
@SuppressWarnings("unchecked")
public class BlotterColumnMapper {

  private final Map<Class<?>, Map<BlotterColumn, ValueProvider>> _mappings = new ClassMap<>();

  /* package */ BlotterColumnMapper() {
  }

  /* package */ void mapColumn(BlotterColumn column, MetaProperty<?> metaProp) {
    ArgumentChecker.notNull(column, "column");
    ArgumentChecker.notNull(metaProp, "metaProp");
    Class<? extends ManageableSecurity> securityType = (Class<? extends ManageableSecurity>) metaProp.metaBean().beanType();
    Map<BlotterColumn, ValueProvider> mappings = mappingsFor(securityType);
    mappings.put(column, propertyProvider(metaProp));
  }

  /* package */  <T extends ManageableSecurity> void mapColumn(BlotterColumn column,
                                                               Class<T> type,
                                                               ValueProvider<T> provider) {
    ArgumentChecker.notNull(column, "column");
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(provider, "provider");
    Map<BlotterColumn, ValueProvider> mappings = mappingsFor(type);
    mappings.put(column, provider);
  }

  /* package */ <T extends ManageableSecurity> void mapColumn(BlotterColumn column, Class<T> type, String value) {
    ArgumentChecker.notNull(column, "column");
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(value, "value");
    Map<BlotterColumn, ValueProvider> mappings = mappingsFor(type);
    mappings.put(column, new StaticValueProvider(value));
  }

  private <T extends ManageableSecurity> Map<BlotterColumn, ValueProvider> mappingsFor(Class<T> type) {
    Map<BlotterColumn, ValueProvider> securityMappings = _mappings.get(type);
    if (securityMappings != null) {
      return securityMappings;
    } else {
      Map<BlotterColumn, ValueProvider> newMappings = Maps.newHashMap();
      _mappings.put(type, newMappings);
      return newMappings;
    }
  }

  private ValueProvider propertyProvider(MetaProperty<?> property) {
    if (property == null) { // for securities where it doesn't make sense to populate a particular column
      return new StaticValueProvider("");
    } else {
      return new PropertyValueProvider(property);
    }
  }

  public Object valueFor(BlotterColumn column, ManageableSecurity security) {
    // position rows have no security
    if (security == null) {
      return "";
    }
    return getValue(column, security, security.getClass());
  }

  private Object getValue(BlotterColumn column, ManageableSecurity security, Class<?> type) {
    Map<BlotterColumn, ValueProvider> providerMap = getMappingsForType(type);
    if (providerMap == null) {
      return "";
    } else {
      ValueProvider valueProvider = providerMap.get(column);
      if (valueProvider != null) {
        return valueProvider.getValue(security);
      } else {
        Class<?> superclass = type.getSuperclass();
        if (superclass == null) {
          return "";
        } else {
          return getValue(column, security, superclass);
        }
      }
    }
  }

  private Map<BlotterColumn, ValueProvider> getMappingsForType(Class<?> type) {
    Map<BlotterColumn, ValueProvider> providerMap = _mappings.get(type);
    if (providerMap != null) {
      return providerMap;
    } else {
      Class<?> superclass = type.getSuperclass();
      if (superclass == null) {
        return null;
      } else {
        return getMappingsForType(superclass);
      }
    }
  }

  private static class PropertyValueProvider<T extends ManageableSecurity> implements ValueProvider<T> {

    private final MetaProperty<?> _property;

    private PropertyValueProvider(MetaProperty<?> property) {
      ArgumentChecker.notNull(property, "property");
      _property = property;
    }

    @Override
    public Object getValue(T security) {
      return _property.get(security);
    }
  }

  private static class StaticValueProvider implements ValueProvider {

    private final Object _value;

    private StaticValueProvider(Object value) {
      ArgumentChecker.notNull(value, "value");
      _value = value;
    }

    @Override
    public Object getValue(ManageableSecurity security) {
      return _value;
    }
  }
}

