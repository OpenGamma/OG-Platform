/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.Maps;
import com.opengamma.core.security.Security;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Maps the properties of each blotter column to properties in each supported security type.
 */
@SuppressWarnings("unchecked")
public class BlotterColumnMapper {

  /** Value providers for the blotter columns, keyed by the type of object being displayed in the blotter. */
  private final Map<Class<?>, Map<BlotterColumn, CellValueProvider>> _mappings = Maps.newHashMap();

  /* package */ BlotterColumnMapper() {
  }

  /* package */ void mapColumn(BlotterColumn column, MetaProperty<?> metaProp) {
    ArgumentChecker.notNull(column, "column");
    ArgumentChecker.notNull(metaProp, "metaProp");
    Class<? extends ManageableSecurity> securityType = (Class<? extends ManageableSecurity>) metaProp.metaBean().beanType();
    Map<BlotterColumn, CellValueProvider> mappings = mappingsFor(securityType);
    mappings.put(column, propertyProvider(metaProp));
  }

  /* package */  <T extends ManageableSecurity> void mapColumn(BlotterColumn column,
                                                               Class<T> type,
                                                               CellValueProvider<T> provider) {
    ArgumentChecker.notNull(column, "column");
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(provider, "provider");
    Map<BlotterColumn, CellValueProvider> mappings = mappingsFor(type);
    mappings.put(column, provider);
  }

  /* package */ <T extends ManageableSecurity> void mapColumn(BlotterColumn column, Class<T> type, String value) {
    ArgumentChecker.notNull(column, "column");
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(value, "value");
    Map<BlotterColumn, CellValueProvider> mappings = mappingsFor(type);
    mappings.put(column, new StaticValueProvider(value));
  }

  private <T extends ManageableSecurity> Map<BlotterColumn, CellValueProvider> mappingsFor(Class<T> type) {
    Map<BlotterColumn, CellValueProvider> securityMappings = _mappings.get(type);
    if (securityMappings != null) {
      return securityMappings;
    } else {
      Map<BlotterColumn, CellValueProvider> newMappings = Maps.newHashMap();
      _mappings.put(type, newMappings);
      return newMappings;
    }
  }

  private CellValueProvider propertyProvider(MetaProperty<?> property) {
    ArgumentChecker.notNull(property, "property");
    return new PropertyValueProvider(property);
  }

  /**
   * Returns the value to display for a security in a blotter column.
   * @param column The blotter column
   * @param security The security, possibly null (for rows that represent a portfolio node)
   * @return The value to display in the column, not null
   */
  public Object valueFor(BlotterColumn column, Object security) {
    // position rows have no security
    if (security == null) {
      return "";
    }
    return getValue(column, security, security.getClass());
  }

  private Object getValue(BlotterColumn column, Object security, Class<?> type) {
    Map<BlotterColumn, CellValueProvider> providerMap = getMappingsForType(type);
    if (providerMap == null) {
      return "";
    } else {
      CellValueProvider valueProvider = providerMap.get(column);
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

  private Map<BlotterColumn, CellValueProvider> getMappingsForType(Class<?> type) {
    Map<BlotterColumn, CellValueProvider> providerMap = _mappings.get(type);
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

  /**
   * Looks up and returns values from {@link Bean} instances using a {@link MetaProperty}.
   * @param <T>
   */
  private static final class PropertyValueProvider<T extends Security & Bean> implements CellValueProvider<T> {

    /** The property used to get the value from the security. */
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

  /**
   * Provides a fixed value for a cell.
   */
  private static final class StaticValueProvider implements CellValueProvider {

    /** The value that is always returned from {@link #getValue} */
    private final Object _value;

    private StaticValueProvider(Object value) {
      ArgumentChecker.notNull(value, "value");
      _value = value;
    }

    @Override
    public Object getValue(Object security) {
      return _value;
    }
  }
}

