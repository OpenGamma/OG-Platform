/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class PostgresDbDialectTest extends DbDialectTest {

  public PostgresDbDialectTest() {
    _dialect = PostgresDbDialect.INSTANCE;
  }

  //-------------------------------------------------------------------------
  public void test_getJDBCDriver() {
    assertEquals(org.postgresql.Driver.class, _dialect.getJDBCDriverClass());
  }

  public void test_getHibernateDialect() {
    assertEquals(org.hibernate.dialect.PostgreSQLDialect.class, _dialect.getHibernateDialect().getClass());
  }

  public void test_getName() {
    assertEquals("Postgres", _dialect.getName());
  }

  //-------------------------------------------------------------------------
  public void test_sqlNextSequenceValueSelect() {
    assertEquals("SELECT nextval('MySeq')", _dialect.sqlNextSequenceValueSelect("MySeq"));
  }

  public void test_sqlNextSequenceValueInline() {
    assertEquals("nextval('MySeq')", _dialect.sqlNextSequenceValueInline("MySeq"));
  }

}
