/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.historicaltimeseries;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.db.DbConnector;
import com.opengamma.util.db.DbDialect;
import com.opengamma.util.db.DbMapSqlParameterSource;

/**
 * A dimension table within a star schema.
 * <p>
 * This class aims to simplify working with a simple dimension table.
 * This kind of table consists of simple deduplicated data, keyed by id.
 * The id is used to reference the data on the main "fact" table.
 * <p>
 * This class uses SQL via JDBC. The SQL may be changed by subclassing the relevant methods.
 */
public class NamedDimensionDbTable {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(NamedDimensionDbTable.class);

  /**
   * The database connector.
   */
  private final DbConnector _dbConnector;
  /**
   * The variable name.
   */
  private final String _variableName;
  /**
   * The table name.
   */
  private final String _tableName;
  /**
   * The sequence used to generate the id.
   */
  private final String _sequenceName;

  /**
   * Creates an instance.
   * 
   * @param dbConnector  the database connector combining all configuration, not null
   * @param variableName  the variable name, used as a placeholder in SQL, not null
   * @param tableName  the table name, not null
   * @param sequenceName  the sequence used to generate the id, may be null
   */
  public NamedDimensionDbTable(final DbConnector dbConnector, final String variableName, final String tableName, final String sequenceName) {
    ArgumentChecker.notNull(dbConnector, "dbConnector");
    ArgumentChecker.notNull(variableName, "variableName");
    ArgumentChecker.notNull(tableName, "tableName");
    _dbConnector = dbConnector;
    _variableName = variableName;
    _tableName = tableName;
    _sequenceName = sequenceName;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database connector.
   * 
   * @return the database connector, not null
   */
  protected DbConnector getDbConnector() {
    return _dbConnector;
  }

  /**
   * Gets the variable name.
   * 
   * @return the variable name, not null
   */
  protected String getVariableName() {
    return _variableName;
  }

  /**
   * Gets the table name.
   * 
   * @return the table name, not null
   */
  protected String getTableName() {
    return _tableName;
  }

  /**
   * Gets the sequence name.
   * 
   * @return the sequence name, may be null
   */
  protected String getSequenceName() {
    return _sequenceName;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the database dialect.
   * 
   * @return the dialect, not null
   */
  protected DbDialect getDialect() {
    return getDbConnector().getDialect();
  }

  /**
   * Gets the next database id.
   * 
   * @return the next database id
   */
  protected long nextId() {
    return getDbConnector().getJdbcOperations().queryForObject(getDialect().sqlNextSequenceValueSelect(_sequenceName), Long.class);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the id for the name matching exactly.
   * 
   * @param name  the name to lookup, not null
   * @return the id, null if not stored
   */
  public Long get(final String name) {
    String select = sqlSelectGet();
    DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue(getVariableName(), name);
    List<Map<String, Object>> result = getDbConnector().getJdbcTemplate().queryForList(select, args);
    if (result.size() == 1) {
      return (Long) result.get(0).get("dim_id");
    }
    return null;
  }

  /**
   * Gets an SQL select statement suitable for finding the name.
   * <p>
   * The SQL requires a parameter of name {@link #getVariableName()}.
   * The statement returns a single column of the id.
   * 
   * @return the SQL, not null
   */
  public String sqlSelectGet() {
    return
      "SELECT dim.id AS dim_id " +
      "FROM " + getTableName() + " dim " +
      "WHERE dim.name = :" + getVariableName() + " ";
  }

  //-------------------------------------------------------------------------
  /**
   * Searches for the id for the name matching any case and using wildcards.
   * 
   * @param name  the name to lookup, not null
   * @return the id, null if not stored
   */
  public Long search(final String name) {
    String select = sqlSelectSearch(name);
    DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue(getVariableName(), getDialect().sqlWildcardAdjustValue(name));
    List<Map<String, Object>> result = getDbConnector().getJdbcTemplate().queryForList(select, args);
    if (result.isEmpty()) {
      return null;
    }
    return (Long) result.get(0).get("dim_id");
  }

  /**
   * Gets an SQL select statement suitable for finding the name.
   * <p>
   * The SQL requires a parameter of name {@link #getVariableName()}.
   * The statement returns a single column of the id.
   * 
   * @param name  the name to lookup, not null
   * @return the SQL, not null
   */
  public String sqlSelectSearch(final String name) {
    return
      "SELECT dim.id AS dim_id " +
      "FROM " + getTableName() + " dim " +
      "WHERE " + getDialect().sqlWildcardQuery("UPPER(dim.name) ", "UPPER(:" + getVariableName() + ")", name);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the id adding it if necessary.
   * 
   * @param name  the name to ensure is present, not null
   * @return the id, null if not stored
   */
  public long ensure(final String name) {
    String select = sqlSelectGet();
    DbMapSqlParameterSource args = new DbMapSqlParameterSource()
      .addValue(getVariableName(), name);
    List<Map<String, Object>> result = getDbConnector().getJdbcTemplate().queryForList(select, args);
    if (result.size() == 1) {
      // different databases return different types, notably BigDecimal and Long
      Object obj = result.get(0).get("dim_id");
      return ((Number) obj).longValue();
    }
    final long id = nextId();
    args.addValue("dim_id", id);
    getDbConnector().getJdbcTemplate().update(sqlInsert(), args);
    s_logger.debug("Inserted new value into {} : {} = {}", new Object[] {getTableName(), id, name});
    return id;
  }

  /**
   * Gets an SQL insert statement suitable for finding the name.
   * <p>
   * The SQL requires a parameter of name {@link #getVariableName()}.
   * 
   * @return the SQL, not null
   */
  public String sqlInsert() {
    return
      "INSERT INTO " + getTableName() + " (id, name) " +
      "VALUES (:dim_id, :" + getVariableName() + ")";
  }

  //-------------------------------------------------------------------------
  /**
   * Lists all the names in the table, sorted alphabetically.
   * 
   * @return the set of names, not null
   */
  public List<String> names() {
    return getDbConnector().getJdbcTemplate().getJdbcOperations().queryForList(sqlSelectNames(), String.class);
  }

  /**
   * Gets an SQL list names.
   * 
   * @return the SQL, not null
   */
  public String sqlSelectNames() {
    return
      "SELECT name FROM " + getTableName() + " ORDER BY name";
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return "Dimension[" + getTableName() + "]";
  }

}
