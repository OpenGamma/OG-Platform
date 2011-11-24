/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.extsql;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Representation of OFFSETFETCH.
 * <p>
 * This outputs an OFFSET-FETCH type clauses.
 */
public final class OffsetFetchSqlFragment extends ContainerSqlFragment {

  /**
   * Creates an instance.
   */
  public OffsetFetchSqlFragment() {
  }

  //-------------------------------------------------------------------------
  @Override
  protected void toSQL(StringBuilder buf, ExtSqlBundle bundle, SqlParameterSource paramSource) {
    int offset = 0;
    int fetchLimit = 0;
    if (paramSource.hasValue("paging_offset")) {
      offset = ((Number) paramSource.getValue("paging_offset")).intValue();
    }
    if (paramSource.hasValue("paging_fetch")) {
      fetchLimit = ((Number) paramSource.getValue("paging_fetch")).intValue();
    }
    buf.append(bundle.getConfig().getPaging(offset, fetchLimit));
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + " " + getFragments();
  }

}
