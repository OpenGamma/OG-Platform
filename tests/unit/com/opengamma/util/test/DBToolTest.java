/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.test;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import static org.junit.Assert.*;

import org.hibernate.mapping.Table;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * 
 * @author pietari
 */
public class DBToolTest {

  private static final String TEST_TABLE = "db_tool_unit_test_table";
  
  private DBTool _tool;
  private String _dbHost;
  private String _user;
  private String _password;

  @Before
  public void setUp() throws Exception {
    Properties props = new Properties();
    File file = new File("tests.properties");
    props.load(new FileInputStream(file));

    _dbHost = props.getProperty("jdbc.url");
    _user = props.getProperty("jdbc.username");
    _password = props.getProperty("jdbc.password");

    _tool = new DBTool(_dbHost, _user, _password);
  }

  @Test
  public void testDrop() throws Exception {

    _tool.createTestSchema();
    createTestTable();
    _tool.dropTestSchema();

    // The table should no longer be there
    Connection connection = getConnection();
    Statement statement = connection.createStatement();
    try {
      statement.execute("SELECT * FROM " + TEST_TABLE);
      fail();
    } catch (SQLException e) {
      // Ok - no table should be there!
    }
    statement.close();
    connection.close();
  }

  @Test
  public void testClear() throws Exception {

    _tool.createTestSchema();
    createTestTable();

    Connection connection = getConnection();
    Statement statement = connection.createStatement();
    statement
        .execute("INSERT INTO " + TEST_TABLE + " (test_column) VALUES ('test')");
    statement.close();

    _tool.clearTestTables();

    statement = connection.createStatement();
    ResultSet rs = statement
        .executeQuery("SELECT COUNT(*) FROM " + TEST_TABLE);
    if (rs.next()) {
      int count = rs.getInt(1);
      assertEquals(0, count);
    } else {
      fail();
    }

    rs.close();
    statement.close();
    connection.close();

    _tool.dropTestSchema();
  }

  private Connection getConnection() throws SQLException {
    return DriverManager.getConnection(_dbHost + "/" + _tool.getTestCatalog(),
        _user, _password);
  }

  private void createTestTable() throws SQLException {
    Connection connection = getConnection();
    Statement statement = connection.createStatement();
    Table table = new Table(TEST_TABLE);
    try {
      String dropSql = table.sqlDropString(_tool.getHibernateDialect(), null, _tool
          .getTestSchema());
      statement.execute(dropSql);
    } catch (SQLException e) {
      // It might not exist, that's OK
    }
    String createSql = "CREATE TABLE "
        + table.getQualifiedName(_tool.getHibernateDialect(), null, _tool
            .getTestSchema()) + " (test_column char(50))";
    statement.execute(createSql);
    statement.close();
    connection.close();
  }

}
