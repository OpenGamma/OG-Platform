/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.extsql;

/**
 * Representation of NAME(key).
 * <p>
 * This is the top level named SQL fragment.
 */
public final class NameSqlFragment extends ContainerSqlFragment {

  private final String _name;

  NameSqlFragment(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Name must be specified");
    }
    _name = name;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the name of the fragment.
   * 
   * @return the name, not null
   */
  public String getName() {
    return _name;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + ":" + _name + " " + getFragments();
  }

}
