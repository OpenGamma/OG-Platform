/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.tool;

import org.apache.commons.lang.Validate;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.test.TestGroup;

import static com.opengamma.component.factory.tool.DbToolContextComponentFactory.*;

/** Test. */
@Test(groups = TestGroup.UNIT)
public class DbToolContextComponentFactoryTest {

  final static String MSSQL_URL_1 = "jdbc:sqlserver://someserver:1433;integratedSecurity=true;databaseName=someDatabase";
  final static String MSSQL_URL_2 = "jdbc:sqlserver://someserver:1433;databaseName=someDatabase;integratedSecurity=true";
  final static String MSSQL_URL_3 = "jdbc:sqlserver://someserver:1433;databaseName=someDatabase";
  final static String HSQL_URL = "jdbc:hsqldb:file:data/hsqldb/og-fin";
  final static String POSTGRES_URL = "jdbc:postgresql://localhost/og_financial";

  final static String MSSQL_DB = "someDatabase";
  final static String HSQL_DB = "og-fin";
  final static String POSTGRES_DB = "og_financial";

  final static String MSSQL_BAD_INVALID_SLASH = "jdbc:sqlserver://someserver:1433;/databaseName=someDatabase";
  final static String MSSQL_BAD_NO_DB_NAME = "jdbc:sqlserver://someserver:1433;integratedSecurity=true;databaseName=";
  final static String MSSQL_BAD_NO_DB_ATALL = "jdbc:sqlserver://someserver:1433;integratedSecurity=true";

  final static String COMPLETE_GARBAGE = "abcdefgh";


  @Test
  public void test_RecognizeMSSQL() {
    Validate.isTrue(getMSSQLCatalog(MSSQL_URL_1).equals(MSSQL_DB), "url1 did not work");
    Validate.isTrue(getMSSQLCatalog(MSSQL_URL_2).equals(MSSQL_DB), "url2 did not work");
    Validate.isTrue(getMSSQLCatalog(MSSQL_URL_3).equals(MSSQL_DB), "url3 did not work");
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void test_noInvalidSlash() {
    getMSSQLCatalog(MSSQL_BAD_INVALID_SLASH);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void test_noDbName() {
    getMSSQLCatalog(MSSQL_BAD_NO_DB_NAME);
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void test_noDbAtAll() {
    getMSSQLCatalog(MSSQL_BAD_NO_DB_ATALL);
  }

  @Test
  public void test_RecognizeHSQL() {
    Validate.isTrue(getStandardCatalog(HSQL_URL).equals(HSQL_DB), "url did not work");
  }

  @Test
  public void test_RecognizePostgres() {
    Validate.isTrue(getStandardCatalog(POSTGRES_URL).equals(POSTGRES_DB), "url did not work");
  }

  @Test
  public void test_All() {
    Validate.isTrue(getCatalog(MSSQL_URL_1).equals(MSSQL_DB), "url1 did not work");
    Validate.isTrue(getCatalog(MSSQL_URL_2).equals(MSSQL_DB), "url2 did not work");
    Validate.isTrue(getCatalog(MSSQL_URL_3).equals(MSSQL_DB), "url3 did not work");
    Validate.isTrue(getCatalog(HSQL_URL).equals(HSQL_DB), "url did not work");
    Validate.isTrue(getCatalog(POSTGRES_URL).equals(POSTGRES_DB), "url did not work");
  }

  @Test
  public void test_handleNull() {
    Validate.isTrue((getCatalog(null) == null), "null did not work");
  }

  @Test(expectedExceptions = OpenGammaRuntimeException.class)
  public void test_completeGarbage() {
    getCatalog(COMPLETE_GARBAGE);
  }

}
