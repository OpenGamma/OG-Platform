/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import java.sql.Driver;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.SQLServerDialect;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.elsql.ElSqlConfig;
import com.opengamma.util.paging.PagingRequest;

/**
 * Database dialect for SQL Server databases.
 * <p>
 * This contains any SQL Server specific SQL.
 */
public class SqlServer2008DbDialect extends DbDialect {

  /**
   * Helper can be treated as a singleton.
   */
  public static final SqlServer2008DbDialect INSTANCE = new SqlServer2008DbDialect();

  /**
   * Restrictive constructor.
   */
  public SqlServer2008DbDialect() {
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public Class<? extends Driver> getJDBCDriverClass() {
    try {
      return (Class<? extends Driver>) Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    } catch (ClassNotFoundException ex) {
      throw new OpenGammaRuntimeException("Could not load the Microsoft JDBC driver: " + ex.getMessage());
    }
    // Use the MS driver...
    // return com.microsoft.sqlserver.jdbc.SQLServerDriver.class;
    // ...or the open-source driver (LGPLed)
    // return net.sourceforge.jtds.jdbc.Driver.class;
  }

  @Override
  protected Dialect createHibernateDialect() {
    return new SQLServerDialect();
  }

  @Override
  protected ElSqlConfig createElSqlConfig() {
    return ElSqlConfig.SQL_SERVER_2008;
  }

  //-------------------------------------------------------------------------
  @Override
  public String sqlApplyPaging(final String sqlSelectFromWhere, final String sqlOrderBy, final PagingRequest paging) {

    if (paging == null || paging.equals(PagingRequest.ALL) || paging.equals(PagingRequest.NONE)) {
      return sqlSelectFromWhere + sqlOrderBy;
    }
    return ElSqlConfig.SQL_SERVER_2008.addPaging(sqlSelectFromWhere, paging.getFirstItem(), paging.getPagingSize()) +
        sqlOrderBy;

  }

  @Override
  public String sqlNextSequenceValueSelect(final String sequenceName) {
    return 
        "DECLARE @NewSeqValue INT; SET NOCOUNT ON; INSERT INTO " + sequenceName + 
        " (SeqVal) VALUES ('a'); SET @NewSeqValue = scope_identity(); DELETE FROM " + sequenceName +
        " WITH (READPAST); SELECT nextval = @NewSeqValue; SET NOCOUNT OFF";
  }

  @Override
  public String sqlNextSequenceValueInline(final String sequenceName) {
    throw new OpenGammaRuntimeException("sqlNextSequenceValueInline is not currently supported in the SQL Server 2008 dialect");
  }

}
