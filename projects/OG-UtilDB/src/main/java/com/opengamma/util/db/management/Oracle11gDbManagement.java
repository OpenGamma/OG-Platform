/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.management;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.Oracle10gDialect;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.TestProperties;

/**
 * Database management for Postgres databases.
 */
public final class Oracle11gDbManagement extends AbstractDbManagement {

  /**
   * The default schema.
   */
  private static final String ORACLE11G_DEFAULT_SCHEMA = "dbo";
  /**
   * Singleton instance.
   */
  private static final Oracle11gDbManagement INSTANCE = new Oracle11gDbManagement();

  /**
   * The underlying Hibernate dialect.
   */
  private Oracle10gDialect _hibernateDialect;

  /**
   * Restricted constructor.
   */
  private Oracle11gDbManagement() {
  }

  /**
   * Gets the singleton instance.
   * 
   * @return the instance, not null
   */
  public static Oracle11gDbManagement getInstance() {
    return INSTANCE;
  }

  //-------------------------------------------------------------------------
  @Override
  public synchronized Dialect getHibernateDialect() {
    if (_hibernateDialect == null) {
      // constructed lazily so we don't get log message about 'using dialect' if we're not actually using it
      _hibernateDialect = new Oracle10gDialect();
    }
    return _hibernateDialect;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<?> getJDBCDriverClass() {
    try {
      return Class.forName("oracle.jdbc.driver.OracleDriver");
    } catch (ClassNotFoundException ex) {
      throw new OpenGammaRuntimeException("Could not load the Oracle JDBC driver: " + ex.getMessage());
    }
    // Use the Oracle driver...
    // return oracle.jdbc.driver.OracleDriver.class;
  }

  @Override
  public String getDatabaseName() {
    return "oracle11g";
  }

  //-------------------------------------------------------------------------
  @Override
  public String getCatalogToConnectTo(String catalog) {
    return getDbHost();
  }

  @Override
  public String getAllSchemasSQL(String catalog) {
    return "SELECT username as name FROM ALL_USERS";
  }

  @Override
  public String getAllForeignKeyConstraintsSQL(String catalog, String schema) {
    String sql = "SELECT constraint_name AS name, table_name FROM USER_CONSTRAINTS WHERE constraint_type = 'R' AND owner='" + getUser() + "'";
    return sql;
  }

  @Override
  public String getAllSequencesSQL(String catalog, String schema) {
    return "SELECT object_name as name FROM ALL_OBJECTS where OBJECT_TYPE = 'SEQUENCE' AND owner='" + getUser() + "'";
  }

  @Override
  public String getAllTablesSQL(String catalog, String schema) {
    return "SELECT table_name as name FROM USER_TABLES";
  }

  @Override
  public String getAllViewsSQL(String catalog, String schema) {
    return "SELECT object_name as name FROM ALL_OBJECTS where OBJECT_TYPE = 'VIEW' AND owner='" + getUser() + "'";
  }

  @Override
  public String getAllColumnsSQL(String catalog, String schema, String table) {
    StringBuilder sql = new StringBuilder("SELECT column_name AS name, data_type AS datatype, " +
        "nullable AS allowsnull, data_default AS defaultvalue FROM ALL_TAB_COLUMNS WHERE owner = '" + getUser() +
        "' AND table_name = '" + table + "'");
    return sql.toString();
  }

  @Override
  public String getCreateSchemaSQL(String catalog, String schema) {
    return "";
  }

  @Override
  public String getSchemaVersionTable(String schemaGroupName) {
    return (schemaGroupName + SCHEMA_VERSION_TABLE_SUFFIX).toLowerCase();
  }

  @Override
  public String getSchemaVersionSQL(String catalog, String schemaGroupName) {
    return "SELECT version_value FROM " + getSchemaVersionTable(schemaGroupName) + " WHERE version_key = 'schema_patch'";
  }

  private String getSystemUser() {
    String user = System.getProperty("system.user");
    if (user == null) {
      user = TestProperties.getTestProperties().getProperty("oracle11g.jdbc.system.username");
    }
    return user;
  }

  private String getSystemPassword() {
    String pw = System.getProperty("system.password");
    if (pw == null) {
      pw = TestProperties.getTestProperties().getProperty("oracle11g.jdbc.system.password");
    }
    return pw;
  }

  @Override
  public CatalogCreationStrategy getCatalogCreationStrategy() {
    return new Oracle11gCatalogCreationStrategy(
        this,
        getUser(),
        getPassword(),
        getSystemUser(),
        getSystemPassword(),
        "SELECT username as name FROM ALL_USERS",
        null);
  }

  @Override
  public void dropSchema(String catalog, String schema) {
    // Does not handle triggers or stored procedures yet
    ArrayList<String> script = new ArrayList<String>();

    Connection conn = null;
    try {
      if (!getCatalogCreationStrategy().catalogExists(catalog)) {
        System.out.println("Catalog " + catalog + " does not exist");
        return; // nothing to drop
      }

      conn = DriverManager.getConnection(getCatalogToConnectTo(catalog), getSystemUser(), getSystemPassword());

      if (schema != null) {
        Statement statement = conn.createStatement();
        Collection<String> schemas = getAllSchemas(catalog, statement);
        statement.close();

        if (!schemas.contains(schema)) {
          System.out.println("Schema " + schema + " does not exist");
          return; // nothing to drop
        }
      }

      setActiveSchema(conn, schema);
      Statement statement = conn.createStatement();

      statement.addBatch("DROP USER " + getUser() + " CASCADE");
      statement.executeBatch();
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
  }

}
