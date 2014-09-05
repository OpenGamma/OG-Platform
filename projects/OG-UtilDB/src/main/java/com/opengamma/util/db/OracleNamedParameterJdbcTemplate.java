/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.KeyHolder;

/**
 * An extension of {@link NamedParameterJdbcTemplate} to handle Oracle.
 * <p>
 * This class adjusts string named parameters to a format suitable for Oracle database.
 * Specifically, it handle empty strings by converting them to a single whitespace.
 */
final class OracleNamedParameterJdbcTemplate extends NamedParameterJdbcTemplate {

  /**
   * Creates an instance.
   * 
   * @param dataSource  the JDBC DataSource to access
   */
  public OracleNamedParameterJdbcTemplate(DataSource dataSource) {
    this(new OracleJdbcTemplate(dataSource));
  }

  /**
   * Creates an instance.
   * 
   * @param classicJdbcTemplate  the classic Spring JdbcTemplate to wrap
   */
  public OracleNamedParameterJdbcTemplate(JdbcOperations classicJdbcTemplate) {
    super(classicJdbcTemplate);
  }

  //-------------------------------------------------------------------------
  public int update(
      String sql, SqlParameterSource paramSource,
      KeyHolder generatedKeyHolder, String[] keyColumnNames) throws DataAccessException {
    SqlParameterSource decorated = new OracleSqlParameterSource(paramSource);
    return super.update(sql, decorated, generatedKeyHolder, keyColumnNames);
  }

  public int[] batchUpdate(String sql, SqlParameterSource[] batchArgs) {
    SqlParameterSource[] decorated = new SqlParameterSource[batchArgs.length];
    for (int i = 0; i < batchArgs.length; i++) {
      decorated[i] = new OracleSqlParameterSource(batchArgs[i]);
    }
    return super.batchUpdate(sql, decorated);
  }

  @Override
  protected PreparedStatementCreator getPreparedStatementCreator(String sql, SqlParameterSource paramSource) {
    SqlParameterSource decorated = new OracleSqlParameterSource(paramSource);
    return super.getPreparedStatementCreator(sql, decorated);
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "OracleNamedParameterJdbcTemplate:" + super.toString();
  }

  //-------------------------------------------------------------------------
  /**
   * Parameter source adding Oracle string conversion.
   */
  static final class OracleSqlParameterSource implements SqlParameterSource {

    // the underlying source
    private final SqlParameterSource _underlying;

    public OracleSqlParameterSource(SqlParameterSource underlying) {
      _underlying = underlying;
    }

    @Override
    public boolean hasValue(String paramName) {
      return _underlying.hasValue(paramName);
    }

    @Override
    public Object getValue(String paramName) throws IllegalArgumentException {
      Object value = _underlying.getValue(paramName);
      return toDatabaseFormat(value);
    }

    // only need to handle SqlParameterValue and String
    private static Object toDatabaseFormat(Object value) {
      if (value instanceof SqlParameterValue) {
        SqlParameterValue param = (SqlParameterValue) value;
        if (param.getValue() instanceof String) {
          String dbStr = Oracle11gDbDialect.INSTANCE.toDatabaseString((String) param.getValue()); 
          value = new SqlParameterValue(param.getSqlType(), dbStr);
        }
      } else if (value instanceof String) {
        value = Oracle11gDbDialect.INSTANCE.toDatabaseString((String) value); 
      }
      return value;
    }

    @Override
    public int getSqlType(String paramName) {
      return _underlying.getSqlType(paramName);
    }

    @Override
    public String getTypeName(String paramName) {
      return _underlying.getTypeName(paramName);
    }
  }

}
