/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.management;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ReflectionUtils;

/**
 * Database management for Vertica databases.
 * <p>
 * Because Vertica does not support running multiple databases on the same host,
 * database and schema are combined into one string, catalog___schema,
 * which is then used as the schema name.
 */
public final class VerticaDbManagement extends AbstractDbManagement {

  /**
   * Singleton instance.
   */
  private static final VerticaDbManagement INSTANCE = new VerticaDbManagement();

  /**
   * The underlying Hibernate dialect. Use Postgres here for now.
   */
  private PostgreSQLDialect _hibernateDialect;

  /**
   * Restricted constructor.
   */
  private VerticaDbManagement() {
  }

  /**
   * Gets the singleton instance.
   * 
   * @return the instance, not null
   */
  public static VerticaDbManagement getInstance() {
    return INSTANCE;
  }

  //-------------------------------------------------------------------------
  @Override
  public synchronized Dialect getHibernateDialect() {
    if (_hibernateDialect == null) {
      _hibernateDialect = new PostgreSQLDialect(); // use Postgres as closest match
    }
    return _hibernateDialect;
  }

  @Override
  public Class<?> getJDBCDriverClass() {
    return ReflectionUtils.loadClass("com.vertica.Driver");
  }

  @Override
  public String getDatabaseName() {
    return "vertica";
  }

  //-------------------------------------------------------------------------
  @Override
  public String getCatalogToConnectTo(String catalog) {
    return getDbHost();
  }

  @Override
  public void setActiveSchema(Connection connection, String schema) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.executeUpdate("SET SEARCH_PATH TO " + schema);
    }
  }

  @Override
  public String getTestCatalog() {
    return null;
  }

  @Override
  public String getTestSchema() {
    return super.getTestCatalog();    
  }

  //-------------------------------------------------------------------------
  @Override
  public String getAllSchemasSQL(String catalog) {
    return "SELECT quote_ident(name) as name from v_internal.vs_schemata";
  }

  @Override
  public String getAllForeignKeyConstraintsSQL(String catalog, String schema) {
    String sql = "SELECT quote_ident(constraint_name) AS name, table_name FROM v_catalog.foreign_keys WHERE table_schema = '" 
      + schema + "'";
    return sql;
  }

  @Override
  public String getAllSequencesSQL(String catalog, String schema) {
    String sql = "SELECT quote_ident(v_internal.vs_sequences.name) as name FROM v_internal.vs_sequences " 
      + "join v_internal.vs_schemata on v_internal.vs_sequences.schema = v_internal.vs_schemata.oid " 
      + "WHERE v_internal.vs_schemata.name = '" 
      + schema + "'";
    return sql;
  }

  @Override
  public String getAllTablesSQL(String catalog, String schema) {
    String sql = "SELECT quote_ident(table_name) AS name FROM v_catalog.tables WHERE IS_SYSTEM_TABLE = 'f'"
      + " AND table_schema = '" + schema + "'";
    return sql;
  }

  @Override
  public String getAllViewsSQL(String catalog, String schema) {
    String sql = "SELECT quote_ident(table_name) AS name FROM v_catalog.views WHERE IS_SYSTEM_VIEW = 'f'"
      + " AND table_schema = '" + schema + "'";
    return sql;
  }

  @Override
  public String getAllColumnsSQL(String catalog, String schema, String table) {
    String sql = "SELECT " 
        + "column_name AS name, " 
        + "data_type AS datatype, " 
        + "is_nullable AS allowsnull, "
        + "column_default AS defaultvalue "
        + "FROM v_catalog.columns " 
        + "WHERE table_name = '"
        + table
        + "' AND table_schema = '" 
        + schema
        + "'";
    return sql;
  }

  @Override
  public String getCreateSchemaSQL(String catalog, String schema) {
    return "CREATE SCHEMA " + schema;
  }
 
  @Override
  public String getSchemaVersionTable(String schemaGroupName) {
    return (schemaGroupName + SCHEMA_VERSION_TABLE_SUFFIX).toLowerCase();
  }

  @Override
  public String getSchemaVersionSQL(String catalog, String schemaGroupName) {
    return "SELECT version_value FROM " + getSchemaVersionTable(schemaGroupName) + " WHERE version_key = 'schema_patch'";
  }
  
  @Override
  public CatalogCreationStrategy getCatalogCreationStrategy() {
    return new VerticaCatalogCreationStrategy();
  }

  //-------------------------------------------------------------------------
  /**
   * Strategy for catalog creation.
   */
  private class VerticaCatalogCreationStrategy implements CatalogCreationStrategy {

    @Override
    public boolean catalogExists(String catalog) {
      try (Connection connection = connect(catalog)) {
        Statement statement = connection.createStatement();
        String sql = getAllSchemasSQL(catalog);
        boolean exists;
        try (ResultSet rs = statement.executeQuery(sql)) {
          exists = rs.next();                                
        }
        return exists;
      } catch (SQLException e) {
        throw new OpenGammaRuntimeException("Failed to check catalog existence", e);      
      }        
    }

    @Override
    public void create(String catalog) {
      return; // no possibility in Vertica to programmatically create databases. Instead schemas are encoded to contain database name as well.
    }
  }

}
