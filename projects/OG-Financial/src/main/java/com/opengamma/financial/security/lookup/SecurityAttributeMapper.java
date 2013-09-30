/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;

import com.google.common.collect.Maps;
import com.opengamma.core.security.Security;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.util.ArgumentChecker;

/**
 * Maps the properties of {@link SecurityAttribute} to properties in each supported security type.
 */
@SuppressWarnings({"unchecked", "rawtypes" })
public class SecurityAttributeMapper {

  /** Value providers for the blotter columns, keyed by the type of object being displayed in the blotter. */
  private final Map<Class<?>, Map<SecurityAttribute, SecurityValueProvider>> _mappings = Maps.newHashMap();

  /* package */ SecurityAttributeMapper() {
  }

  /* package */ void mapColumn(SecurityAttribute column, MetaProperty<?> metaProp) {
    ArgumentChecker.notNull(column, "column");
    ArgumentChecker.notNull(metaProp, "metaProp");
    Class<? extends ManageableSecurity> securityType = (Class<? extends ManageableSecurity>) metaProp.metaBean().beanType();
    Map<SecurityAttribute, SecurityValueProvider> mappings = mappingsFor(securityType);
    mappings.put(column, propertyProvider(metaProp));
  }

  /* package */  <T extends ManageableSecurity> void mapColumn(SecurityAttribute column,
                                                               Class<T> type,
                                                               SecurityValueProvider<T> provider) {
    ArgumentChecker.notNull(column, "column");
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(provider, "provider");
    Map<SecurityAttribute, SecurityValueProvider> mappings = mappingsFor(type);
    mappings.put(column, provider);
  }

  /* package */ <T extends ManageableSecurity> void mapColumn(SecurityAttribute column, Class<T> type, String value) {
    ArgumentChecker.notNull(column, "column");
    ArgumentChecker.notNull(type, "type");
    ArgumentChecker.notNull(value, "value");
    Map<SecurityAttribute, SecurityValueProvider> mappings = mappingsFor(type);
    mappings.put(column, new StaticValueProvider(value));
  }

  private <T extends ManageableSecurity> Map<SecurityAttribute, SecurityValueProvider> mappingsFor(Class<T> type) {
    Map<SecurityAttribute, SecurityValueProvider> securityMappings = _mappings.get(type);
    if (securityMappings != null) {
      return securityMappings;
    } else {
      Map<SecurityAttribute, SecurityValueProvider> newMappings = Maps.newHashMap();
      _mappings.put(type, newMappings);
      return newMappings;
    }
  }

  private SecurityValueProvider propertyProvider(MetaProperty<?> property) {
    ArgumentChecker.notNull(property, "property");
    return new PropertyValueProvider(property);
  }

  /**
   * Returns the value to display for a security in a blotter column.
   * @param column The blotter column
   * @param security The security, possibly null (for rows that represent a portfolio node)
   * @return The value to display in the column, not null
   */
  public Object valueFor(SecurityAttribute column, Object security) {
    // position rows have no security
    if (security == null) {
      return "";
    }
    return getValue(column, security, security.getClass());
  }

  private Object getValue(SecurityAttribute column, Object security, Class<?> type) {
    Map<SecurityAttribute, SecurityValueProvider> providerMap = getMappingsForType(type);
    if (providerMap == null) {
      return "";
    } else {
      SecurityValueProvider valueProvider = providerMap.get(column);
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

  private Map<SecurityAttribute, SecurityValueProvider> getMappingsForType(Class<?> type) {
    Map<SecurityAttribute, SecurityValueProvider> providerMap = _mappings.get(type);
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
  private static final class PropertyValueProvider<T extends Security> implements SecurityValueProvider<T> {

    /** The property used to get the value from the security. */
    private final MetaProperty<?> _property;

    private PropertyValueProvider(MetaProperty<?> property) {
      ArgumentChecker.notNull(property, "property");
      _property = property;
    }

    @Override
    public Object getValue(T security) {
      return _property.get((Bean) security);
    }
  }

  /**
   * Provides a fixed value for a cell.
   */
  private static final class StaticValueProvider implements SecurityValueProvider {

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

