/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.db;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

/**
 * Test PostgreSQLDbHelper.
 */
@Test
public class PostgreSQLDbHelperTest extends DbHelperTest {

  public PostgreSQLDbHelperTest() {
    _helper = PostgreSQLDbHelper.INSTANCE;
  }

  //-------------------------------------------------------------------------
  public void test_getJDBCDriver() {
    assertEquals(org.postgresql.Driver.class, _helper.getJDBCDriverClass());
  }

  public void test_getHibernateDialect() {
    assertEquals(org.hibernate.dialect.PostgreSQLDialect.class, _helper.getHibernateDialect().getClass());
  }

  public void test_getName() {
    assertEquals("PostgreSQL", _helper.getName());
  }

  //-------------------------------------------------------------------------
  public void test_sqlNextSequenceValueSelect() {
    assertEquals("SELECT nextval('MySeq')", _helper.sqlNextSequenceValueSelect("MySeq"));
  }

  public void test_sqlNextSequenceValueInline() {
    assertEquals("nextval('MySeq')", _helper.sqlNextSequenceValueInline("MySeq"));
  }

}
