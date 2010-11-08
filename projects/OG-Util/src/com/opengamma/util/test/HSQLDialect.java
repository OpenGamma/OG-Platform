/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.File;

import org.hibernate.dialect.Dialect;

/**
 * Implementation of the database dialect for HSQL.
 */
public final class HSQLDialect extends AbstractDBDialect {

  /**
   * Singleton instance.
   */
  private static final HSQLDialect INSTANCE = new HSQLDialect(); 
  /**
   * The underlying Hibernate dialect.
   */
  private org.hibernate.dialect.HSQLDialect _hibernateDialect;

  /**
   * Restricted constructor.
   */
  private HSQLDialect() {
  }

  /**
   * Gets the singleton instance.
   * @return the instance, not null
   */
  public static HSQLDialect getInstance() {
    return INSTANCE;
  }

  //-------------------------------------------------------------------------
  @Override
  public void shutdown(String catalog) {
    super.shutdown(catalog);
    
    executeSql(catalog, "SHUTDOWN");
  }

  @Override
  public Class<?> getJDBCDriverClass() {
    return org.hsqldb.jdbcDriver.class;
  }

  @Override
  public String getDatabaseName() {
    return "hsqldb";
  }

  @Override
  public String getAllSchemasSQL(String catalog) {
    return "SELECT TABLE_SCHEM AS name FROM INFORMATION_SCHEMA.SYSTEM_SCHEMAS";
  }

  @Override
  public String getAllForeignKeyConstraintsSQL(String catalog, String schema) {
    String sql = "SELECT FK_NAME AS name, " +
      "FKTABLE_NAME AS table_name " +
      "FROM INFORMATION_SCHEMA.SYSTEM_CROSSREFERENCE";
    if (schema != null) {
      sql += " WHERE FKTABLE_SCHEM = '" + schema + "'";
    }
    return sql;
  }

  @Override
  public String getAllSequencesSQL(String catalog, String schema) {
    String sql = "SELECT SEQUENCE_NAME AS name FROM INFORMATION_SCHEMA.SYSTEM_SEQUENCES " +
      " WHERE SEQUENCE_NAME <> 'LOB_ID'";
    if (schema != null) {
      sql += " AND SEQUENCE_SCHEMA =  '" + schema + "'";
    }
    return sql;
  }

  @Override
  public String getAllTablesSQL(String catalog, String schema) {
    String sql = "SELECT TABLE_NAME AS name FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE TABLE_TYPE = 'TABLE'";
    if (schema != null) {
      sql += " AND TABLE_SCHEM = '" + schema + "'";
    }
    return sql;
  }
  
  @Override
  public String getAllViewsSQL(String catalog, String schema) {
    String sql = "SELECT TABLE_NAME AS name FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE TABLE_TYPE = 'VIEW'";
    if (schema != null) {
      sql += " AND TABLE_SCHEM = '" + schema + "'";
    }
    return sql;
  }

  @Override
  public String getAllColumnsSQL(String catalog, String schema, String table) {
    StringBuilder sql = new StringBuilder("SELECT COLUMN_NAME AS name, DATA_TYPE AS datatype, IS_NULLABLE AS allowsnull, COLUMN_DEF AS defaultvalue " +
        "FROM INFORMATION_SCHEMA.SYSTEM_COLUMNS WHERE TABLE_NAME='");
    sql.append(table).append("'");
    if (schema != null) {
      sql.append(" AND TABLE_SCHEM='").append(schema).append("'");
    }
    return sql.toString();
  }

  @Override
  public String getCreateSchemaSQL(String schema) {
    return "CREATE SCHEMA " + schema;
  }

  @Override
  public synchronized Dialect getHibernateDialect() {
    if (_hibernateDialect == null) {
      // constructed lazily so we don't get log message about 'using dialect' if we're not actually using it
      _hibernateDialect = new org.hibernate.dialect.HSQLDialect();
    }
    return _hibernateDialect;
  }

  @Override
  public CatalogCreationStrategy getCatalogCreationStrategy() {
    return new HSQLCatalogCreationStrategy();
  }

  //-------------------------------------------------------------------------
  /**
   * Strategy for catalog creation.
   */
  private class HSQLCatalogCreationStrategy implements CatalogCreationStrategy {

    private File getFile() {
      String dbHost = getDbHost().trim();
      String filePart = dbHost.substring("jdbc:hsqldb:file:".length());
      return new File(filePart);
    }

    @Override
    public boolean catalogExists(String catalog) {
      if (getDbHost().toLowerCase().indexOf("jdbc:hsqldb:file:") != 1) {
        return true;
      }
      File catalogDir = new File(getFile(), catalog);
      return catalogDir.exists();
    }

    @Override
    public void create(String catalog) {
      return; // HSQLDB creates DB automatically on first connect
    }
  }

}
