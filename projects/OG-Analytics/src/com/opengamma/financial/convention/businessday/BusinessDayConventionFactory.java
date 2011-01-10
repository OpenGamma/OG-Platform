/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Factory to obtain instances of {@code BusinessDayConvention}.
 * <p>
 * Convention names are read from a properties file.
 */
public final class BusinessDayConventionFactory {

  /**
   * Singleton instance of {@code BusinessDayConventionFactory}.
   */
  public static final BusinessDayConventionFactory INSTANCE = new BusinessDayConventionFactory();

  /**
   * Map of convention name to convention.
   */
  private final Map<String, BusinessDayConvention> _conventionMap = new HashMap<String, BusinessDayConvention>();

  /**
   * Creates the factory.
   */
  private BusinessDayConventionFactory() {
    final ResourceBundle conventions = ResourceBundle.getBundle(BusinessDayConvention.class.getName());
    final Map<String, BusinessDayConvention> instances = new HashMap<String, BusinessDayConvention>();
    for (final String convention : conventions.keySet()) {
      final String clazz = conventions.getString(convention);
      BusinessDayConvention instance = instances.get(clazz);
      if (instance == null) {
        try {
          instance = (BusinessDayConvention) Class.forName(clazz).newInstance();
          instances.put(clazz, instance);
        } catch (InstantiationException ex) {
          throw new OpenGammaRuntimeException("Error initialising BusinessDay conventions", ex);
        } catch (IllegalAccessException ex) {
          throw new OpenGammaRuntimeException("Error initialising BusinessDay conventions", ex);
        } catch (ClassNotFoundException ex) {
          throw new OpenGammaRuntimeException("Error initialising BusinessDay conventions", ex);
        }
      }
      _conventionMap.put(convention.toLowerCase(), instance);
    }
  }

  /**
   * Retrieves a named BusinessDayConvention. Note that the lookup is not case sensitive.
   * 
   * @param name name of the convention to load.
   * @return convention with the specified name.
   */
  public BusinessDayConvention getBusinessDayConvention(final String name) {
    return _conventionMap.get(name.toLowerCase());
  }

}
