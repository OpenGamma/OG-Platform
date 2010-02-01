/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

import org.hibernate.id.enhanced.SequenceStructure;
import org.hibernate.mapping.ForeignKey;
import org.hibernate.mapping.Table;

import com.opengamma.OpenGammaRuntimeException;

/**
 * 
 *
 * @author pietari
 */
abstract public class AbstractDBDialect implements DBDialect {
  
  private String _dbServerHost;
  private String _user;
  private String _password;
  
  @Override
  public void initialise(String dbServerHost, String user, String password) {
    _dbServerHost = dbServerHost;
    _user = user;
    _password = password;
    
    try {
        getJDBCDriverClass().newInstance(); // load driver.
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Cannot load JDBC driver", e);
    }
  }

  public String getDbHost() {
    return _dbServerHost;
  }

  public String getUser() {
    return _user;
  }

  public String getPassword() {
    return _password;
  }
  
  public abstract String getAllSchemasSQL(String catalog);
  public abstract String getAllTablesSQL(String catalog, String schema);
  public abstract String getAllSequencesSQL(String catalog, String schema);
  public abstract String getAllForeignKeyConstraintsSQL(String catalog, String schema);
  public abstract String getCreateSchemaSQL(String schema);
  public abstract CatalogCreationStrategy getCatalogCreationStrategy();
  
  protected Connection connect(String catalog) throws SQLException {
    Connection conn = DriverManager.getConnection(_dbServerHost + "/" + catalog, 
        _user, _password);
    conn.setAutoCommit(true);
    return conn;
  }
  
  @Override
  public void clearTables(String catalog, String schema) {
    // Does not take constraints into account as yet
    LinkedList<String> script = new LinkedList<String>();
    
    Connection conn = null;
    try {
      if (!getCatalogCreationStrategy().catalogExists(catalog)) {
        return; // nothing to clear
      }
      
      conn = connect(catalog);
      Statement statement = conn.createStatement();
      
      // Clear tables SQL
      ResultSet rs = statement.executeQuery(getAllTablesSQL(catalog, schema));
      while (rs.next()) {
        String name = rs.getString("name");
        Table table = new Table(name);
        script.add("DELETE FROM " + table.getQualifiedName(getHibernateDialect(), null, schema));
        
        if (table.getName().toLowerCase().indexOf("hibernate_sequence") != -1) { // if it's a sequence table, reset it 
          script.add("INSERT INTO " + table.getQualifiedName(getHibernateDialect(), null, schema) + " values ( 1 )");
        }
      }
      rs.close();
            
      // Now execute it all
      int i = 0;
      int MAX_ATTEMPTS = script.size() * 3;
      SQLException latestException = null;
      while (i < MAX_ATTEMPTS && !script.isEmpty()) {
        String sql = script.remove();
        try {
          statement.executeUpdate(sql);
        } catch (SQLException e) {
          // assume it failed because of a constraint violation
          // try deleting other tables first - make this the new last statement
          latestException = e;
          script.add(sql);                              
        }
        i++;
      }
      statement.close();
      
      if (i == MAX_ATTEMPTS && !script.isEmpty()) {
        throw new OpenGammaRuntimeException("Failed to clear tables - is there a circle in the table dependency graph?", latestException); 
      }
      
      
    } catch (SQLException e) {
      throw new OpenGammaRuntimeException("Failed to clear tables", e);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
      }
    }           
    
  }
  
  @Override
  public void createSchema(String catalog, String schema) {
    Connection conn = null;
    try {
      getCatalogCreationStrategy().create(catalog);
      
      if (schema != null) {
        // Connect to the new catalog and create the schema
        conn = connect(catalog);
        Statement statement = conn.createStatement(); 
      
        ResultSet rs = statement.executeQuery(getAllSchemasSQL(catalog));
        boolean schemaAlreadyExists = false;
        while (rs.next()) {
          String name = rs.getString("name");
          if (name.equals(schema)) {
            schemaAlreadyExists = true;
          }
        }
        rs.close();
        
        if (!schemaAlreadyExists) {
          String createSchemaSql = getCreateSchemaSQL(schema);
          statement.executeUpdate(createSchemaSql);
        }
        
        statement.close();
      }
      
    } catch (SQLException e) {
      throw new OpenGammaRuntimeException("Failed to clear tables", e);
    } finally {
      try {
        if (conn != null) {
          conn.close();
        }
      } catch (SQLException e) {
      }
    }  
    
  }

  @Override
  public void dropSchema(String catalog, String schema) {
    // Does not handle triggers or stored procedures yet
    
    ArrayList<String> script = new ArrayList<String>();
    
    Connection conn = null;
    try {
      if (!getCatalogCreationStrategy().catalogExists(catalog)) {
        return; // nothing to drop
      }

      conn = connect(catalog);
      Statement statement = conn.createStatement();
      
      // Drop constraints SQL
      if (getHibernateDialect().dropConstraints()) {
        ResultSet rs = statement.executeQuery(getAllForeignKeyConstraintsSQL(catalog, schema));
        while (rs.next()) {
          String name = rs.getString("name");
          String table = rs.getString("table_name");
          ForeignKey fk = new ForeignKey();
          fk.setName(name);
          fk.setTable(new Table(table));
          
          String dropConstraintSql = fk.sqlDropString(getHibernateDialect(), null, schema);
          script.add(dropConstraintSql);
        }
        rs.close();
      }
      
      // Drop tables SQL
      ResultSet rs = statement.executeQuery(getAllTablesSQL(catalog, schema));
      while (rs.next()) {
        String name = rs.getString("name");
        Table table = new Table(name);
        String dropTableStr = table.sqlDropString(getHibernateDialect(), null, schema);
        script.add(dropTableStr);
      }
      rs.close();
      
      // Drop sequences SQL
      if (getAllSequencesSQL(catalog, schema) != null) {
        rs = statement.executeQuery(getAllSequencesSQL(catalog, schema));
        while (rs.next()) {
          String name = rs.getString("name");
          final SequenceStructure sequenceStructure = new SequenceStructure(getHibernateDialect(), name, 0, 1);
          String[] dropSequenceStrings = sequenceStructure.sqlDropStrings(getHibernateDialect());
          script.addAll(Arrays.asList(dropSequenceStrings));
        }
        rs.close();
      }
      
      // Now execute it all
      statement.close();
      statement = conn.createStatement();
      for (String sql : script) {
        statement.executeUpdate(sql);
      }
      
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

  @Override
  public void executeSql(String catalog, String sql) {
    
    ArrayList<String> sqlStatements = new ArrayList<String>();
    
    for (String statement : sql.split(";")) {
      String[] lines = statement.split("\r\n|\r|\n");
      StringBuffer fixedSql = new StringBuffer();
      for (String line : lines) {
        if (line.startsWith("//") || line.startsWith("--")) {
          continue; // exclude comment lines
        }
        fixedSql.append(line + " ");        
      }
      
      String fixedSqlStr = fixedSql.toString().trim();
      
      if (!fixedSqlStr.isEmpty()) {      
        sqlStatements.add(fixedSqlStr);
      }
    }
    
    Connection conn = null;
    try {
      conn = connect(catalog);
      
      Statement statement = conn.createStatement();
      for (String sqlStatement : sqlStatements) {
        statement.execute(sqlStatement.toString());
      }
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
