/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import org.hibernate.dialect.Dialect;

/**
 * Operations to create and clear databases.  
 *
 * @author pietari
 */
public interface DBDialect {
  
  public void initialise(String dbServerHost, String user, String password);
  public void shutdown();
  
  public Dialect getHibernateDialect();
  
  public Class<?> getJDBCDriverClass();
  
  /**
   * @return Database name, all in lower case (derby, postgres, ...)
   */
  public String getDatabaseName();

  
  /**
   * Creates a database. 
   * <p>
   * If the database already exists, does nothing.
   * 
   * @param catalog Catalog (= database) name. Not null.
   * @param schema Schema name within database. May be null, in which case database default schema is used.
   */
  public void createSchema(String catalog, String schema);
  
  /**
   * Drops all tables and sequences in the database. 
   * <p>
   * If the database does not exist, does nothing.
   * 
   * @param Catalog name. Not null.
   * @param Schema name. May be null, in which case database default schema is used.
   */
  public void dropSchema(String catalog, String schema);
  
  /**
   * Clears all tables in the database. The tables will still exist after this operation.
   * <p>
   * If the database does not exist, does nothing.
   * 
   * @param Catalog name. Not null.
   * @param Schema name. May be null, in which case database default schema is used.
   */
  public void clearTables(String catalog, String schema);
  
  /**
   * Executes SQL against a database.
   * 
   * @param catalog Catalog (= database) name. Not null.
   * @param sql SQL to execute. Not null.
   */
  public void executeSql(String catalog, String sql);
  

}
