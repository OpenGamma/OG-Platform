/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.util.Collection;

import org.hibernate.dialect.Dialect;

/**
 * Operations to create and clear databases.  
 *
 */
public interface DBDialect {

  /**
   * Initializes the database.
   * @param dbServerHost  the database server, not null
   * @param user  the user name
   * @param password  the password
   */
  void initialise(String dbServerHost, String user, String password);

  /**
   * Resets a database catalog, usually called between two tests.
   * @param catalog  the catalog to shut, not null
   */
  void reset(String catalog);

  /**
   * Shuts down a database catalog.
   * @param catalog  the catalog to shut, not null
   */
  void shutdown(String catalog);

  /**
   * Gets the Hibernate dialect.
   * @return the dialect, not null
   */
  Dialect getHibernateDialect();

  /**
   * Gets the JDBC driver class.
   * @return the driver, not null
   */
  Class<?> getJDBCDriverClass();

  /**
   * @return Database name, all in lower case (derby, postgres, ...)
   */
  String getDatabaseName();

  /**
   * Creates a database. 
   * <p>
   * If the database already exists, does nothing.
   * 
   * @param catalog Catalog (= database) name. Not null.
   * @param schema Schema name within database. May be null, in which case database default schema is used.
   */
  void createSchema(String catalog, String schema);

  /**
   * Drops all tables and sequences in the database. 
   * <p>
   * If the database does not exist, does nothing.
   * 
   * @param catalog name. Not null.
   * @param schema name. May be null, in which case database default schema is used.
   */
  void dropSchema(String catalog, String schema);

  /**
   * Clears all tables in the database. The tables will still exist after this operation.
   * <p>
   * If the database does not exist, does nothing.
   * 
   * @param catalog name. Not null.
   * @param schema name. May be null, in which case database default schema is used.
   * @param ignoredTables Do not clear tables with these names. All strings
   * must be lower case. Not null.
   */
  void clearTables(String catalog, String schema, Collection<String> ignoredTables);

  /**
   * Executes SQL against a database.
   * 
   * @param catalog Catalog (= database) name. Not null.
   * @param schema Schema name. Optional.
   * @param sql SQL to execute. Not null.
   */
  void executeSql(String catalog, String schema, String sql);

  /**
   * Returns a string describing the structure of the database. It may be a set of statements to construct it or
   * other representation. Comparison of the string must be the same as structural and content equality.
   * 
   * @param catalog Name of the catalog.
   * @return A string describing the database, which is dialect specific.
   */
  String describeDatabase(String catalog);

  /**
   * Gets the database used for tests.
   * 
   * @return the database used for tests
   */
  String getTestCatalog();

  /**
   * Gets the schema used for tests.
   * 
   * @return the schema used for tests
   */
  String getTestSchema();

}
