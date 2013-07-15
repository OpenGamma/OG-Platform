/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.management;

import java.io.File;
import java.sql.SQLInvalidAuthorizationSpecException;

import org.apache.commons.io.FileUtils;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;


/**
 * Database management for HSQL databases.
 */
public final class HSQLDbManagement extends AbstractDbManagement {

  /**
   * Singleton instance.
   */
  private static final HSQLDbManagement INSTANCE = new HSQLDbManagement(); 
  /**
   * The underlying Hibernate dialect.
   */
  private HSQLDialect _hibernateDialect;

  /**
   * Restricted constructor.
   */
  private HSQLDbManagement() {
  }

  /**
   * Gets the singleton instance.
   * 
   * @return the instance, not null
   */
  public static HSQLDbManagement getInstance() {
    return INSTANCE;
  }

  //-------------------------------------------------------------------------
  @Override
  public synchronized Dialect getHibernateDialect() {
    if (_hibernateDialect == null) {
      // constructed lazily so we don't get log message about 'using dialect' if we're not actually using it
      _hibernateDialect = new HSQLDialect();
    }
    return _hibernateDialect;
  }

  @Override
  public Class<?> getJDBCDriverClass() {
    return org.hsqldb.jdbcDriver.class;
  }

  @Override
  public String getDatabaseName() {
    return "hsqldb";
  }

  //-------------------------------------------------------------------------
  @Override
  public void shutdown(String catalog) {
    super.shutdown(catalog);
    
    executeSql(catalog, null, "SHUTDOWN");
  }

  //-------------------------------------------------------------------------
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
  public String getCreateSchemaSQL(String catalog, String schema) {
    return "CREATE SCHEMA " + schema;
  }
  
  @Override
  public String getSchemaVersionTable(String schemaGroupName) {
    return (schemaGroupName + SCHEMA_VERSION_TABLE_SUFFIX).toUpperCase();
  }
  
  @Override
  public String getSchemaVersionSQL(String catalog, String schemaGroupName) {
    return "SELECT version_value FROM " + getSchemaVersionTable(schemaGroupName) + " WHERE version_key = 'schema_patch'";
  }

  @Override
  public CatalogCreationStrategy getCatalogCreationStrategy() {
    return new HSQLCatalogCreationStrategy();
  }

  @Override
  public void dropSchema(String catalog, String schema) {
    try {
      super.dropSchema(catalog, schema);
    } catch (RuntimeException ex) {
      // try deleting database
      if (ex.getCause() instanceof SQLInvalidAuthorizationSpecException) {
        FileUtils.deleteQuietly(getFile());
        super.dropSchema(catalog, schema);
      }
    }
    
    /*
     * NOTE jonathan 2013-04-11 -- this should work but for some reason doesn't
    Connection conn = null;
    try {
      if (!getCatalogCreationStrategy().catalogExists(catalog)) {
        System.out.println("Catalog " + catalog + " does not exist");
        return; // nothing to drop
      }

      conn = connect(catalog);
      
      if (schema == null) {
        schema = "PUBLIC";
      }

      Statement statement = conn.createStatement();
      statement.executeUpdate("DROP SCHEMA " + schema + " CASCADE");
      statement.close();
      
    } catch (SQLException e) {
      throw new OpenGammaRuntimeException("Failed to drop schema", e);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
      }
    }
    */
  }

  private File getFile() {
    String dbHost = getDbHost().trim();
    String filePart = dbHost.substring("jdbc:hsqldb:file:".length());
    return new File(filePart);
  }

  //-------------------------------------------------------------------------
  /**
   * Strategy for catalog creation.
   */
  private class HSQLCatalogCreationStrategy implements CatalogCreationStrategy {

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
