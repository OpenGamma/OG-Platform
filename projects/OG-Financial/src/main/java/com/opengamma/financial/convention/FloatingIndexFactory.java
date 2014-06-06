/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.convention;

import java.util.Iterator;

import org.joda.convert.FromString;

/**
 * Factory to obtain instances of {@code FloatingIndex}.
 */
public final class FloatingIndexFactory extends AbstractNamedInstanceFactory<FloatingIndex> {

  /**
   * Singleton instance.
   */
  public static final FloatingIndexFactory INSTANCE = new FloatingIndexFactory();

  //-------------------------------------------------------------------------
  /**
   * Finds an index by name, ignoring case.
   *
   * @param name the name of the instance to find, not null
   * @return the index, not null
   * @throws IllegalArgumentException if the name is not found
   */
  @FromString
  public static FloatingIndex of(final String name) {
    return INSTANCE.instance(name);
  }

  //-------------------------------------------------------------------------
  /**
   * Restricted constructor, loading the instances.
   */
  private FloatingIndexFactory() {
    super(FloatingIndex.class);
    load();
  }

  // load from code
  //TODO: Move to a properties file
  protected void load() {
    for (FloatingIndex index : FloatingIndex.values()) {
      // add alternative name that replaces - with _ (for backwards compatibility)
      addInstance(index, index.getIsdaName().replace("-", "_"));
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
  public Iterator<FloatingIndex> enumerateAvailableDayCounts() {
    return instanceMap().values().iterator();
  }

}
