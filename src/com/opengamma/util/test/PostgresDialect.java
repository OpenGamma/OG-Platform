/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.PostgreSQLDialect;

/**
 * 
 *
 * @author pietari
 */
public class PostgresDialect extends AbstractDBDialect {
  
  private static final String POSTGRES_DEFAULT_SCHEMA = "public";
  private final static PostgreSQLDialect DIALECT = new PostgreSQLDialect();
  
  @Override
  public Class<?> getJDBCDriverClass() {
    return org.postgresql.Driver.class;
  }
  
  @Override
  public String getAllCatalogsSQL() {
    return "SELECT datname AS name FROM pg_database";
  }
  
  @Override
  public String getAllSchemasSQL(String catalog) {
    return "SELECT nspname AS name from pg_namespace";
  }

  @Override
  public String getAllConstraintsSQL(String catalog, String schema) {
    if (schema == null) {
      schema = POSTGRES_DEFAULT_SCHEMA;
    }
    String sql = "SELECT constraint_name AS name FROM information_schema.table_constraints WHERE " +
      "constraint_catalog = '" + catalog + "' AND constraint_schema = '" + schema + "'";
    return sql;
  }

  @Override
  public String getAllSequencesSQL(String catalog, String schema) {
    if (schema == null) {
      schema = POSTGRES_DEFAULT_SCHEMA;
    }
    String sql = "SELECT sequence_name AS name FROM information_schema.sequences WHERE " +
      "sequence_catalog = '" + catalog + "'" + " AND sequence_schema = '" + schema + "'";
    return sql;
  }

  @Override
  public String getAllTablesSQL(String catalog, String schema) {
    if (schema == null) {
      schema = POSTGRES_DEFAULT_SCHEMA;
    }
    String sql = "SELECT table_name AS name FROM information_schema.tables WHERE " +
      "table_catalog = '" + catalog + "'" + " AND table_schema = '" + schema + "' AND table_type = 'BASE TABLE'";
    return sql;
  }

  @Override
  public String getCreateCatalogSQL(String catalog) {
    return "CREATE DATABASE " + catalog;
  }

  @Override
  public String getCreateSchemaSQL(String schema) {
    return "CREATE SCHEMA " + schema;
  }

  @Override
  public String getBlankCatalog() {
    return "template1";
  }

  @Override
  public Dialect getDialect() {
    return DIALECT;
  }

}
