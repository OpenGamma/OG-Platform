/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.extsql;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

/**
 * Single fragment in the extsql AST.
 */
public abstract class SqlFragment {

  /**
   * Convert this fragment to SQL, appending it to the specified buffer.
   * 
   * @param buf  the buffer to append to, not null
   * @param bundle  the extsql bundle for context, not null
   * @param paramSource  the SQL parameters, not null
   */
  protected abstract void toSQL(StringBuilder buf, ExtSqlBundle bundle, SqlParameterSource paramSource);

}
