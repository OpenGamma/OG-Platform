/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.hibernate.dialect.Dialect;

import com.opengamma.OpenGammaRuntimeException;

/**
 * Implementation of the database dialect for Derby.
 */
public final class DerbyDialect extends AbstractDBDialect {
  
  /**
   * Singleton instance.
   */
  private static final DerbyDialect INSTANCE = new DerbyDialect(); 
  /**
   * The underlying Hibernate dialect.
   */
  private org.hibernate.dialect.DerbyDialect _hibernateDialect;

  /**
   * Restricted constructor.
   */
  private DerbyDialect() {
  }

  /**
   * Gets the singleton instance.
   * @return the instance, not null
   */
  public static DerbyDialect getInstance() {
    return INSTANCE;
  }

  //-------------------------------------------------------------------------
  @Override
  public void reset(String catalog) {
    super.reset(catalog);
    
    // for Derby, we shutdown the database to avoid locking issues
    System.out.println("Closing connection to " + catalog + "...");
    try {
      DriverManager.getConnection("jdbc:derby:;shutdown=true");
    } catch (SQLException e) {
      if (e.getErrorCode() != 50000 || !"XJ015".equals(e.getSQLState())) {
        throw new OpenGammaRuntimeException("Could not shutdown Derby " + e.getErrorCode() + " - " + e.getSQLState() + " - " + e.getMessage(), e);        
      }
    }
  }

  @Override
  public void shutdown(String catalog) {
    super.shutdown(catalog);
    
    System.out.println("Closing connection to " + catalog + "...");
    try {
      DriverManager.getConnection("jdbc:derby:;shutdown=true");
    } catch (SQLException e) {
      if (e.getErrorCode() != 50000 || !"XJ015".equals(e.getSQLState())) {
        throw new OpenGammaRuntimeException("Could not shutdown Derby " + e.getErrorCode() + " - " + e.getSQLState() + " - " + e.getMessage(), e);        
      }
    }
  }

  @Override
  public Class<?> getJDBCDriverClass() {
    return org.apache.derby.jdbc.EmbeddedDriver.class;
  }

  @Override
  public String getDatabaseName() {
    return "derby";
  }

  @Override
  public String getAllSchemasSQL(String catalog) {
    return "SELECT schemaname AS name FROM SYS.SYSSCHEMAS";
  }

  @Override
  public String getAllForeignKeyConstraintsSQL(String catalog, String schema) {
    String sql = "SELECT constraintname AS name, " +
      "tablename AS table_name " +
      "FROM SYS.SYSCONSTRAINTS, SYS.SYSTABLES " +
      "WHERE SYS.SYSTABLES.tableid = SYS.SYSCONSTRAINTS.tableid AND type = 'F'";
    if (schema != null) {
      sql += " AND SYS.SYSCONSTRAINTS.schemaid = (SELECT schemaid FROM SYS.SYSSCHEMAS WHERE schemaname = '" + schema + "')";
    }
    return sql;
  }

  @Override
  public String getAllSequencesSQL(String catalog, String schema) {
    String sql = "SELECT sequencename AS name FROM SYS.SYSSEQUENCES ";
    if (schema != null) {
      sql += " WHERE schemaid = (SELECT schemaid FROM SYS.SYSSCHEMAS WHERE schemaname = '" + schema + "')";
    }
    return sql;
  }

  @Override
  public String getAllTablesSQL(String catalog, String schema) {
    String sql = "SELECT tablename AS name FROM SYS.SYSTABLES WHERE tabletype = 'T'";
    if (schema != null) {
      sql += " AND schemaid = (SELECT schemaid FROM SYS.SYSSCHEMAS WHERE schemaname = '" + schema + "')";
    }
    return sql;
  }
  
  @Override
  public String getAllViewsSQL(String catalog, String schema) {
    String sql = "SELECT tablename AS name FROM SYS.SYSTABLES WHERE tabletype = 'V'";
    if (schema != null) {
      sql += " AND schemaid = (SELECT schemaid FROM SYS.SYSSCHEMAS WHERE schemaname = '" + schema + "')";
    }
    return sql;
  }

  @Override
  public String getAllColumnsSQL(String catalog, String schema, String table) {
    StringBuilder sql = new StringBuilder("SELECT c.columnname AS name,c.columndatatype AS datatype,'' AS allowsnull,c.columndefault AS defaultvalue " +
        "FROM SYS.SYSCOLUMNS AS c INNER JOIN SYS.SYSTABLES AS t ON c.referenceid=t.tableid WHERE t.tablename='");
    sql.append(table).append("'");
    if (schema != null) {
      sql.append(" AND t.schemaid=(SELECT schemaid FROM SYS.SYSSCHEMAS WHERE schemaname='").append(schema).append("')");
    }
    return sql.toString();
  }

  @Override
  public String getCreateSchemaSQL(String catalog, String schema) {
    return "CREATE SCHEMA " + schema;
  }

  @Override
  public synchronized Dialect getHibernateDialect() {
    if (_hibernateDialect == null) {
      // constructed lazily so we don't get log message about 'using dialect' if we're not actually using it
      _hibernateDialect = new org.hibernate.dialect.DerbyDialect();
    }
    return _hibernateDialect;
  }

  @Override
  public CatalogCreationStrategy getCatalogCreationStrategy() {
    return new DerbyCatalogCreationStrategy();
  }

  //-------------------------------------------------------------------------
  /**
   * Strategy for creating a Derby database.
   */
  private class DerbyCatalogCreationStrategy implements CatalogCreationStrategy {

    private File getFile() {
      String dbHost = getDbHost().trim();
      String filePart = dbHost.substring("jdbc:derby:".length());
      return new File(filePart);
    }

    @Override
    public boolean catalogExists(String catalog) {
      File catalogDir = new File(getFile(), catalog);
      return catalogDir.exists();
    }

    @Override
    public void create(String catalog) {
      if (!catalogExists(catalog)) {
        try {
          Connection conn = connect(catalog + ";create=true");
          conn.close();
        } catch (SQLException e) {
          throw new OpenGammaRuntimeException("Cannot create Derby DB", e);
        }
      }
    }
  }

}
