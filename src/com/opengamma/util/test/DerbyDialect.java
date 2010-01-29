/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.dialect.Dialect;

import com.opengamma.OpenGammaRuntimeException;

/**
 * 
 * 
 * @author pietari
 */
public class DerbyDialect extends AbstractDBDialect {

  private org.hibernate.dialect.DerbyDialect _hibernateDialect;
  
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
    return "SELECT schemaid AS name FROM SYS.SYSSCHEMAS";
  }

  @Override
  public String getAllForeignKeyConstraintsSQL(String catalog, String schema) {
    String sql = "SELECT constraintname AS name, " +
    		"tablename AS table_name " +
    		"FROM SYS.SYSCONSTRAINTS, SYS.SYSTABLES " +
    		"WHERE SYS.SYSTABLES.tableid = SYS.SYSCONSTRAINTS.tableid AND type = 'F'";
    if (schema != null) {
      sql += " AND schemaid = (SELECT schemaid FROM SYS.SYSSCHEMAS WHERE schemaname = '" + schema + "'";      
    }
    return sql;
  }

  @Override
  public String getAllSequencesSQL(String catalog, String schema) {
    return null; // no sequences in Derby
  }

  @Override
  public String getAllTablesSQL(String catalog, String schema) {
    String sql = "SELECT tablename AS name FROM SYS.SYSTABLES WHERE tabletype = 'T'";
    if (schema != null) {
      sql += " AND schemaid = (SELECT schemaid FROM SYS.SYSSCHEMAS WHERE schemaname = '" + schema + "'";
    }
    return sql;
  }

  @Override
  public String getCreateSchemaSQL(String schema) {
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

  /* private static void recursiveDelete(File file) {
    if (file.isDirectory()) {
      File[] list = file.listFiles();
      for (File entry : list) {
        if (entry.isDirectory()) {
          recursiveDelete(entry);
        }
        if (!entry.delete()) {
          s_logger.warn("Could not delete file:" + file.getAbsolutePath());
          // throw new
          // OpenGammaRuntimeException("Could not delete file:"+entry.getAbsolutePath());
        } else {
          System.err.println("Deleted " + entry.getAbsolutePath());
        }
      }
    }
    if (!file.delete()) {
      s_logger.warn("Could not delete file:" + file.getAbsolutePath());
    } else {
      System.err.println("Deleted " + file.getAbsolutePath());
    }
  } */

}
