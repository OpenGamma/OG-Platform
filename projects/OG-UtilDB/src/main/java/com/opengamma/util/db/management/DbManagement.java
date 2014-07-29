/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db.management;

import java.util.Collection;
import java.util.List;

import org.hibernate.dialect.Dialect;

/**
 * Operations that support the programmatic management of a database.
 * <p>
 * There will typically be a different implementation of this interface for
 * each different database and potentially for different versions.
 */
public interface DbManagement {

  /**
   * Initializes the database management API.
   * 
   * @param dbServerHost  the database server, not null
   * @param user  the user name
   * @param password  the password
   */
  void initialise(String dbServerHost, String user, String password);

  /**
   * Resets the database catalog.
   * <p>
   * This is usually called between tests.
   * 
   * @param catalog  the catalog to reset, not null
   */
  void reset(String catalog);

  /**
   * Shuts down a database catalog.
   * 
   * @param catalog  the catalog to shut, not null
   */
  void shutdown(String catalog);

  /**
   * Gets the dialect used by Hibernate.
   * 
   * @return the dialect, not null
   */
  Dialect getHibernateDialect();

  /**
   * Gets the JDBC driver class.
   * 
   * @return the driver, not null
   */
  Class<?> getJDBCDriverClass();

  /**
   * Gets the simple database name.
   * 
   * @return the database name, all in lower case
   */
  String getDatabaseName();

  /**
   * Creates a database. 
   * <p>
   * If the database already exists, no action occurs.
   * 
   * @param catalog  the catalog (database) name, not null
   * @param schema  the schema name within the database, null means the default schema
   */
  void createSchema(String catalog, String schema);

  /**
   * Drops all tables and sequences in the database. 
   * <p>
   * If the database does not exist, no action occurs.
   * 
   * @param catalog  the catalog (database) name, not null
   * @param schema  the schema name within the database, null means the default schema
   */
  void dropSchema(String catalog, String schema);

  /**
   * Clears all tables in the database without deleting the tables.
   * <p>
   * If the database does not exist, no action occurs.
   * 
   * @param catalog  the catalog (database) name, not null
   * @param schema  the schema name within the database, null means the default schema
   * @param ignoredTables  the tables to ignore, all strings must be lower case, not null
   */
  void clearTables(String catalog, String schema, Collection<String> ignoredTables);

  /**
   * Executes SQL against a database.
   * 
   * @param catalog  the catalog (database) name, not null
   * @param schema  the schema name within the database, null means the default schema
   * @param sql  the SQL to execute, not null.
   */
  void executeSql(String catalog, String schema, String sql);
  
  /**
   * Describes the structure of the database limited to objects which names have given prefix.
   * <p>
   * The returned string is implementation dependent.
   * It may be a set of statements to construct the database or some other representation.
   * Comparison of the string must be the same as structural and content equality.
   * 
   * @param catalog  the catalog (database) name, not null
   * @param prefix the prefix of objects' names                
   * @return a dialect specific string describing the database, not null
   */
  String describeDatabase(final String catalog, final String prefix);

  /**
   * Returns collection of table names.
   *
   * @param catalog the catalog (database) name, not null
   * @return a list of table names, not null
   */
  List<String> listTables(final String catalog);

  /**
   * Describes the structure of the database.
   * <p>
   * The returned string is implementation dependent.
   * It may be a set of statements to construct the database or some other representation.
   * Comparison of the string must be the same as structural and content equality.
   * 
   * @param catalog  the catalog (database) name, not null
   * @return a dialect specific string describing the database, not null
   */
  String describeDatabase(String catalog);
  
  /**
   * Gets the current schema version of a named schema group.
   *  
   * @param catalog  the catalog (database) name, not null
   * @param schema  the schema name within the database, null means the default schema
   * @param schemaGroupName  the name of the schema group whose version to query, not null
   * @return the schema group version, null if not applicable
   */
  Integer getSchemaGroupVersion(String catalog, String schema, String schemaGroupName);

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
  
  /*
   * Gets the connection string for the given catalog
   * 
   * @param catalog the catalog(database) name, not null
   * @return a dialect specific connection string
   */
  String getCatalogToConnectTo(String catalog);

}
