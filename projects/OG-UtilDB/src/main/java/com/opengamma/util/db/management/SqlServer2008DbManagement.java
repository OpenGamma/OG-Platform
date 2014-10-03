/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.management;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Table;

import com.google.common.base.Objects;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.tuple.Pair;

/**
 * Database management for Postgres databases.
 */
public final class SqlServer2008DbManagement extends AbstractDbManagement {

  /**
   * SQL to retrieve all the columns.
   */
  private static final String GET_ALL_COLUMNS_SQL =
    "SELECT column_name AS name,data_type AS datatype,is_nullable AS allowsnull,column_default AS defaultvalue FROM information_schema.columns WHERE table_name='";
  /**
   * The default schema.
   */
  private static final String SQLSERVER2008_DEFAULT_SCHEMA = "dbo";
  /**
   * Singleton instance.
   */
  private static final SqlServer2008DbManagement INSTANCE = new SqlServer2008DbManagement();

  /**
   * The underlying Hibernate dialect.
   */
  private SQLServerDialect _hibernateDialect;

  /**
   * Restricted constructor.
   */
  private SqlServer2008DbManagement() {
  }

  /**
   * Gets the singleton instance.
   * 
   * @return the instance, not null
   */
  public static SqlServer2008DbManagement getInstance() {
    return INSTANCE;
  }

  //-------------------------------------------------------------------------
  @Override
  public synchronized Dialect getHibernateDialect() {
    if (_hibernateDialect == null) {
      // constructed lazily so we don't get log message about 'using dialect' if we're not actually using it
      _hibernateDialect = new SQLServerDialect();
    }
    return _hibernateDialect;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Class<?> getJDBCDriverClass() {
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
  public String getDatabaseName() {
    return "sqlserver2008";
  }

  //-------------------------------------------------------------------------
  @Override
  public String getCatalogToConnectTo(String catalog) {
    return getDbHost() + ";databasename=" + catalog;
  }
  
  @Override
  public String getAllSchemasSQL(String catalog) {
    return "SELECT SCHEMA_NAME AS name FROM INFORMATION_SCHEMA.SCHEMATA";
  }

  @Override
  public String getAllForeignKeyConstraintsSQL(String catalog, String schema) {
    String effScheme = Objects.firstNonNull(schema, SQLSERVER2008_DEFAULT_SCHEMA);
    String sql = "SELECT constraint_name AS name, table_name FROM information_schema.table_constraints WHERE " +
      "constraint_catalog = '" + catalog + "' AND constraint_schema = '" + effScheme + "'" + " AND constraint_type = 'FOREIGN KEY'";
    return sql;
  }

  @Override
  public String getAllSequencesSQL(String catalog, String schema) {
    String effScheme = Objects.firstNonNull(schema, SQLSERVER2008_DEFAULT_SCHEMA);
    String sql = "SELECT table_name AS name FROM information_schema.tables WHERE table_name LIKE '%_seq' AND " + 
      "table_catalog = '" + catalog + "'" + " AND table_schema = '" + effScheme + "' AND table_type = 'BASE TABLE'";
    return sql;
  }

  @Override
  public String getAllTablesSQL(String catalog, String schema) {
    String effScheme = Objects.firstNonNull(schema, SQLSERVER2008_DEFAULT_SCHEMA);
    String sql = "SELECT table_name AS name FROM information_schema.tables WHERE NOT table_name LIKE '%_seq' AND " +
      "table_catalog = '" + catalog + "'" + " AND table_schema = '" + effScheme + "' AND table_type = 'BASE TABLE'";
    return sql;
  }

  @Override
  public String getAllViewsSQL(String catalog, String schema) {
    String effScheme = Objects.firstNonNull(schema, SQLSERVER2008_DEFAULT_SCHEMA);
    String sql = "SELECT table_name AS name FROM information_schema.tables WHERE " +
      "table_catalog = '" + catalog + "'" + " AND table_schema = '" + effScheme + "' AND table_type = 'VIEW'";
    return sql;
  }

  @Override
  public String getAllColumnsSQL(String catalog, String schema, String table) {
    String effScheme = Objects.firstNonNull(schema, SQLSERVER2008_DEFAULT_SCHEMA);
    StringBuilder sql = new StringBuilder(GET_ALL_COLUMNS_SQL);
    sql.append(catalog).append("' AND table_schema='").append(effScheme).append("' AND table_name='");
    sql.append(table).append("'");
    return sql.toString();
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
    return new SQLCatalogCreationStrategy(
        this, 
        getUser(), 
        getPassword(), 
        "SELECT name FROM sys.databases WHERE name NOT IN ('master', 'model', 'msdb', 'tempdb')", 
        null);
  }
  
  @Override
  public void dropSchema(String catalog, String schema) {
    // Does not handle triggers or stored procedures yet
    ArrayList<String> script = new ArrayList<String>();
    
    try {
      if (!getCatalogCreationStrategy().catalogExists(catalog)) {
        System.out.println("Catalog " + catalog + " does not exist");
        return; // nothing to drop
      }
      
      try (Connection conn = connect(catalog)) {
        if (schema != null) {
          try (Statement statement = conn.createStatement()) {
            Collection<String> schemas = getAllSchemas(catalog, statement);
            if (!schemas.contains(schema)) {
              System.out.println("Schema " + schema + " does not exist");
              return; // nothing to drop
            }
          }
        }
  
        setActiveSchema(conn, schema);
        try (Statement statement = conn.createStatement()) {
          // Drop constraints SQL
          if (getHibernateDialect().dropConstraints()) {
            for (Pair<String, String> constraint : getAllForeignKeyConstraints(catalog, schema, statement)) {
              String name = constraint.getFirst();
              String table = constraint.getSecond();
              ForeignKey fk = new ForeignKey();
              fk.setName(name);
              fk.setTable(new Table(table));
              
              String dropConstraintSql = fk.sqlDropString(getHibernateDialect(), null, schema);
              script.add(dropConstraintSql);
            }
          }
          
          // Drop views SQL
          for (String name : getAllViews(catalog, schema, statement)) {
            Table table = new Table(name);
            String dropViewStr = table.sqlDropString(getHibernateDialect(), null, schema);
            dropViewStr = dropViewStr.replaceAll("drop table", "drop view");
            script.add(dropViewStr);
          }
          
          // Drop tables SQL
          for (String name : getAllTables(catalog, schema, statement)) {
            Table table = new Table(name);
            String dropTableStr = table.sqlDropString(getHibernateDialect(), null, schema);
            script.add(dropTableStr);
          }
        }
        
        // Now execute it all
        try (Statement statement = conn.createStatement()) {
          for (String sql : script) {
            //System.out.println("Executing \"" + sql + "\"");
            statement.executeUpdate(sql);
          }
        }
        
        // Drop sequences SQL
        try (Statement statement = conn.createStatement()) {
          script.clear();
          for (String name : getAllSequences(catalog, schema, statement)) {
            Table table = new Table(name);
            String dropTableStr = table.sqlDropString(getHibernateDialect(), null, schema);
            script.add(dropTableStr);
          }
        }
        //now execute drop sequence
        try (Statement statement = conn.createStatement()) {
          for (String sql : script) {
            //System.out.println("Executing \"" + sql + "\"");
            statement.executeUpdate(sql);
          }
        }
      }
    
    } catch (SQLException e) {
      throw new OpenGammaRuntimeException("Failed to drop schema", e);
    }
  }

}
