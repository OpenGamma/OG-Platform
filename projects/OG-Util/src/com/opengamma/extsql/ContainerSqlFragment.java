/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.extsql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Representation of a list of child units.
 */
class ContainerSqlFragment extends SqlFragment {

  /**
   * The fragments.
   */
  private final List<SqlFragment> _fragments = new ArrayList<SqlFragment>();

  /**
   * Creates an empty container.
   */
  ContainerSqlFragment() {
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a fragment to the list in the container.
   * 
   * @param childFragment  the child fragment, not null
   */
  public void addFragment(SqlFragment childFragment) {
    _fragments.add(childFragment);
  }

  /**
   * Gets the list of fragments.
   * 
   * @return the unmodifiable list of fragments, not null
   */
  public List<SqlFragment> getFragments() {
    return Collections.unmodifiableList(_fragments);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void toSQL(StringBuilder buf, ExtSqlBundle bundle, SqlParameterSource paramSource) {
    for (SqlFragment fragment : _fragments) {
      fragment.toSQL(buf, bundle, paramSource);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getFragments().toString();
  }

}
