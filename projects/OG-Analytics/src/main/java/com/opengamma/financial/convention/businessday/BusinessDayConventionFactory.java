/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.businessday;

import java.util.Iterator;

import org.joda.convert.FromString;

import com.opengamma.financial.convention.AbstractNamedInstanceFactory;

/**
 * Factory to obtain instances of {@code BusinessDayConvention}.
 * <p>
 * Convention names are read from a properties file.
 */
public final class BusinessDayConventionFactory
    extends AbstractNamedInstanceFactory<BusinessDayConvention> {

  /**
   * Singleton instance of {@code BusinessDayConventionFactory}.
   */
  public static final BusinessDayConventionFactory INSTANCE = new BusinessDayConventionFactory();

  //-------------------------------------------------------------------------
  /**
   * Finds a convention by name, ignoring case.
   * 
   * @param name  the name of the instance to find, not null
   * @return the convention, not null
   * @throws IllegalArgumentException if the name is not found
   */
  @FromString
  public static BusinessDayConvention of(final String name) {
    return INSTANCE.instance(name);
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor, loading the properties file.
   */
  private BusinessDayConventionFactory() {
    super(BusinessDayConvention.class);
    loadFromProperties();
  }

  //-------------------------------------------------------------------------
  /**
   * Retrieves a named BusinessDayConvention. Note that the lookup is not case sensitive.
   *
   * @param name  the name of the convention to load, not null
   * @return convention with the specified name, null if not found
   * @deprecated Use {@link #of(String)} or {@link #instance(String)}.
   */
  @Deprecated
  public BusinessDayConvention getBusinessDayConvention(final String name) {
    try {
      return instance(name);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  /**
   * Iterates over the available conventions. No particular ordering is specified and conventions may
   * exist in the system not provided by this factory that aren't included as part of this enumeration.
   *
   * @return the available conventions, not null
   * @deprecated use {@link #instanceMap()}
   */
  @Deprecated
  public Iterator<BusinessDayConvention> enumerateAvailableBusinessDayConventions() {
    return instanceMap().values().iterator();
  }

}
