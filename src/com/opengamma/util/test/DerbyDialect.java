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
import org.hibernate.dialect.PostgreSQLDialect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;

/**
 * 
 * 
 * @author pietari
 */
public class DerbyDialect implements DBDialect {

  private static final Logger s_logger = LoggerFactory
      .getLogger(DerbyDialect.class);
  
  private org.hibernate.dialect.DerbyDialect _hibernateDialect;
  
  private String _dbServerHost;
  private String _user;
  private String _password;
  
  @Override
  public Class<?> getJDBCDriverClass() {
    return org.apache.derby.jdbc.EmbeddedDriver.class;
  }
  
  @Override
  public void clearTables(String catalog, String schema) {
    dropSchema(catalog, schema); // not really correct, but as this dialect is used only for testing do this for now...
  }

  @Override
  public void createSchema(String catalog, String schema) {
    File blankDbDir = new File("blank");
    try {
      String connUrl = _dbServerHost + "/" + catalog + ";createFrom=" + blankDbDir.getAbsolutePath();
      Connection conn = DriverManager.getConnection(connUrl, _user,
          _password);
      // this will create a copy of the blank database
      conn.close();  
    } catch (SQLException e) {
      throw new OpenGammaRuntimeException("Cannot create database", e);
    }
  }

  @Override
  public void dropSchema(String catalog, String schema) {
    recursiveDelete(new File("derby-db"));
  }

  @Override
  public void initialise(String dbServerHost, String user, String password) {
    _dbServerHost = dbServerHost;
    _user = user;
    _password = password;
    
    try {
      org.apache.derby.jdbc.EmbeddedDriver.class.newInstance(); // load driver.
    } catch (Exception e) {
      throw new OpenGammaRuntimeException("Cannot load JDBC driver", e);
    }
  }
  
  @Override
  public synchronized Dialect getHibernateDialect() {
    if (_hibernateDialect == null) {
      // constructed lazily so we don't get log message about 'using dialect' if we're not actually using it
      _hibernateDialect = new org.hibernate.dialect.DerbyDialect();
    }
    return _hibernateDialect;
  }

  private static void recursiveDelete(File file) {
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

  }

}
