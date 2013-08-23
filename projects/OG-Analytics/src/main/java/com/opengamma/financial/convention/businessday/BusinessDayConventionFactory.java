/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;

import org.joda.convert.FromString;

import com.google.common.collect.Iterators;
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
  private final Map<String, BusinessDayConvention> _conventionMap = new HashMap<>();

  /**
   * All convention instances.
   */
  private final Collection<BusinessDayConvention> _conventions;

  //-------------------------------------------------------------------------
  /**
   * Gets a convention by name.
   * Matching is case insensitive.
   *
   * @param name  the name, not null
   * @return the convention, not null
   * @throws IllegalArgumentException if not found
   */
  @FromString
  public static BusinessDayConvention of(final String name) {
    final BusinessDayConvention result = BusinessDayConventionFactory.INSTANCE.getBusinessDayConvention(name);
    if (result == null) {
      throw new IllegalArgumentException("Unknown BusinessDayConvention: " + name);
    }
    return result;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the factory.
   */
  private BusinessDayConventionFactory() {
    final ResourceBundle conventions = ResourceBundle.getBundle(BusinessDayConvention.class.getName());
    final Map<String, BusinessDayConvention> instances = new HashMap<>();
    for (final String convention : conventions.keySet()) {
      final String clazz = conventions.getString(convention);
      BusinessDayConvention instance = instances.get(clazz);
      if (instance == null) {
        try {
          instance = (BusinessDayConvention) Class.forName(clazz).newInstance();
          instances.put(clazz, instance);
        } catch (final InstantiationException ex) {
          throw new OpenGammaRuntimeException("Error initialising BusinessDay conventions", ex);
        } catch (final IllegalAccessException ex) {
          throw new OpenGammaRuntimeException("Error initialising BusinessDay conventions", ex);
        } catch (final ClassNotFoundException ex) {
          throw new OpenGammaRuntimeException("Error initialising BusinessDay conventions", ex);
        }
      }
      _conventionMap.put(convention.toLowerCase(), instance);
    }
    _conventions = new ArrayList<>(instances.values());
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

  /**
   * Iterates over the available conventions. No particular ordering is specified and conventions may
   * exist in the system not provided by this factory that aren't included as part of this enumeration.
   *
   * @return the available conventions, not null
   */
  public Iterator<BusinessDayConvention> enumerateAvailableBusinessDayConventions() {
    return Iterators.unmodifiableIterator(_conventions.iterator());
  }

}
